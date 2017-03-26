package com.guokrspace.duducar.communication;

import android.os.Message;
import android.util.Log;

import com.guokrspace.duducar.common.CommonUtil;
import com.guokrspace.duducar.communication.message.MessageTag;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Prashant Adesara, Kai Yang
 * Handle the TCPClient with Socket Server. 
 * */

public class SocketClient {

    private static SocketClient s_socketClient;
    private String serverMessage;
    private int messageid;
    private boolean socketConnected = false;
    /**
     * Specify the Server Ip Address here. Whereas our Socket Server is started.
     * */
    public static final String SERVERIP = "120.24.237.15"; // your computer IP address
    public static final int SERVERPORT = 8282;
    private boolean mRun = false;

    private PrintWriter out = null;
    private BufferedReader in = null;
    private Socket socket = null;

    public ResponseHandler messageParsor;

    public static SocketClient getInstance() {
        if (s_socketClient == null) {
            new SocketClient();
        }
        return s_socketClient;
    }
    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public SocketClient()
    {
//        mMessageListener = listener;
        s_socketClient = this;
        messageid = 0;
    }

    public Socket getSocket(){
        return socket;
    }


    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    private int blockMessage;
    public synchronized int sendMessage(final JSONObject message, final ResponseHandler handler, final int timeout){
        int ret = -1; //Default is error
        Log.e("daddy", "message" + message.toString());
//        Log.e("daddy", out.toString());
        //Send the message
        try {
            message.put("message_id", messageid);

            if (out != null && !out.checkError()) {
                Log.e("SENT TO SERVER", "S: Sent Message: '" + message.toString() + "'");
                out.println(message.toString());
                out.flush();

                //Start a timer
                blockMessage = 0;

            } else { //发送失败
                if (++blockMessage > 4) { //多次连续发送失败, 阻塞重连
                    SocketClient.getInstance().run();
                    blockMessage = 0;
                }
            }

            final Runnable timerRunnable = new Runnable() {
                int counter=0;
                @Override
                public void run() {
                    counter ++;
                    if(counter < timeout) {
                        handler.postRunnableDelay(this, 1000); //1s interval
                    } else {
                        handler.stopRunnable(this);
                        Message msg = handler.obtainMessage(ResponseHandler.TIMEOUT_MESSAGE,message.toString());
                        handler.sendMessage(msg);
                    }
                }
            };
            handler.postRunnable(timerRunnable);

            //Enqueue
            MessageDispatcher messageDispatcher = new MessageDispatcher(messageid, message, timerRunnable, handler);
            messageDispatchQueue.put(messageid, messageDispatcher);

            ret = messageid;
            messageid++;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public boolean isSocketConnected() {
        return socketConnected;
    }

    public void setSocketConnected(boolean socketConnected) {
        this.socketConnected = socketConnected;
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP SI Client", "SI: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVERPORT);
//            socket.setSoTimeout(30000);
            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP SI Client", "SI: Sent.");
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun && !socket.isClosed() && socket.isConnected()) {
                    serverMessage = in.readLine();

                    if (serverMessage != null) {

                        JSONObject jsonObject = new JSONObject(serverMessage);
                        if (jsonObject.has("message_id")) {
                            int messageid = (int) jsonObject.get("message_id");
                            MessageDispatcher dispatcher = messageDispatchQueue.get(messageid);

                            /*
                             * Client Originated Message
                             */
                            if (dispatcher != null) {
                                dispatcher.setResponse(jsonObject);
                                ResponseHandler handler = dispatcher.target;
                                handler.sendResponse(serverMessage);
                                handler.stopRunnable(dispatcher.timer); //Cancel the timer
                                messageDispatchQueue.remove(messageid); //dequeue
                            }
                            /*
                            * Server Originated Message
                            */
                        } else if(jsonObject.has("cmd")){
                            String cmd = (String) jsonObject.get("cmd");
                            int messageTag = MessageTag.getInstance().Tag(cmd);
                            Log.d("SocketClient ", "messageTag"+messageTag);
                            ResponseHandler target = serverMessageDispatchMap.get(messageTag);
                            if (target != null) {
                                target.sendResponse(serverMessage);
                            } else {
                                String errorMsg = String.format("Unregisted Message: %s", serverMessage);
                            }
                        }
                        Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
                    }
                    serverMessage = null;
                }
                if(mRun){ // 正在执行 ,socket失败
                    SocketClient.getInstance().run();
                }
            }
            catch (Exception e)
            {
                Log.e("TCP SI Error", "SI: Error", e);
                e.printStackTrace();
            }
            finally
            {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP SI Error", "SI: Error", e);
        }
    }


    public int sendHeartBeat(DuduService.HeartBeatMessage heartBeatMessage, String role, ResponseHandler handler){
        int ret = -1;
        JSONObject heartbeat = new JSONObject();
        try {
            heartbeat.put("cmd", heartBeatMessage.getCmd());
            heartbeat.put("role", role);
            heartbeat.put("lat",heartBeatMessage.getLat());
            heartbeat.put("lng", heartBeatMessage.getLng());
            ret = sendMessage(heartbeat, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    HashMap<Integer, MessageDispatcher> messageDispatchQueue = new HashMap<>();
    public class MessageDispatcher{
        int messageid;
        JSONObject request;
        JSONObject response;
        ResponseHandler target;
        Runnable timer;

        public MessageDispatcher(int messageid, JSONObject messagebody, Runnable timer, ResponseHandler target) {
            this.messageid = messageid;
            this.request = messagebody;
            this.target = target;
            this.timer = timer;
        }

        public void setResponse(JSONObject response) {
            this.response = response;
        }
    }

    static HashMap<Integer, ResponseHandler> serverMessageDispatchMap = new HashMap<>();

    public void registerServerMessageHandler( int cmd, ResponseHandler target)
    {
        serverMessageDispatchMap.put(cmd, target);
    }

    public void unregisterServerMessageHandler( int cmd)
    {
        serverMessageDispatchMap.remove(cmd);
    }

    /*
     * Send message
     */
    public int sendRegcodeRequst(String mobile, String role, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject regcodeReq = new JSONObject();
        try {
            regcodeReq.put("cmd","register");
            regcodeReq.put("role",role);
            regcodeReq.put("mobile",mobile);
            ret = sendMessage(regcodeReq, handler, 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }


    public int sendVerifyRequst(String mobile, String role, String regcode, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject verify = new JSONObject();
        try {
            verify.put("cmd", "verify");
            verify.put("role",role);
            verify.put("mobile",mobile);
            verify.put("verifycode",regcode);
            verify.put("mac", CommonUtil.getMAC());
            verify.put("imei", CommonUtil.getIMEI());
            verify.put("imsi", CommonUtil.getIMSI());
            ret = sendMessage(verify, handler, 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }


    public int sendLoginReguest(String mobile, String role, String token, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject loginmsg = new JSONObject();
        try {
            loginmsg.put("cmd", "login");
            loginmsg.put("role",role);
            loginmsg.put("mobile",mobile);
            loginmsg.put("token",token);
            loginmsg.put("mac", CommonUtil.getMAC());
            loginmsg.put("imei", CommonUtil.getIMEI());
            loginmsg.put("imsi", CommonUtil.getIMSI());
            loginmsg.put("lat", CommonUtil.getCurLat());
            loginmsg.put("lng", CommonUtil.getCurLng());
            ret = sendMessage(loginmsg, handler, 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int pullNotPaidOrder(String role, ResponseHandler handler){
        int ret = -1;

        JSONObject notPaid = new JSONObject();
        try {
            notPaid.put("cmd", "not_paid_order");
            notPaid.put("role", role);
            ret = sendMessage(notPaid, handler, 5);
        } catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public int sendOrderCancel(String role, ResponseHandler handler){

        int ret = -1;

        JSONObject cancelmsg = new JSONObject();
        try {
            cancelmsg.put("cmd", "cancel_order");
            cancelmsg.put("role", role);
            ret = sendMessage(cancelmsg, handler, 5);
        } catch (JSONException e){
            e.printStackTrace();
        }

        return ret;
    }

    public int sendCarRequest(String role, String start, String dest, Double start_lat,
                              Double start_lng, Double dest_lat, Double dest_lng,
                              String distance, String price, String car,ResponseHandler handler)
    {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", "create_order");
            carmsg.put("role",role);
            carmsg.put("start",start);
            carmsg.put("destination",dest);
            carmsg.put("start_lat", start_lat);
            carmsg.put("start_lng",start_lng);
            carmsg.put("destination_lat",dest_lat);
            carmsg.put("destination_lng",dest_lng);
            carmsg.put("pre_mileage",distance);
            carmsg.put("pre_price",price);
            carmsg.put("car_type",car);
            carmsg.put("mac", CommonUtil.getMAC());
            carmsg.put("imei", CommonUtil.getIMEI());
            carmsg.put("imsi", CommonUtil.getIMSI());
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int cancelCarRequest(String role, ResponseHandler handler) {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", "cancel_order");
            carmsg.put("role", role);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }


    public int sendNearByCarRequestTest(Double lat, Double lng, String cartype, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.GET_NEAR_CAR_REQ));
            carmsg.put("role","2");
            carmsg.put("lat",lat);
            carmsg.put("lng",lng);
            carmsg.put("car_type",cartype);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int sendRatingRequest(int orderId, int rating, String comments, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.RATING_SERVICE));
            carmsg.put("role","2");
            carmsg.put("comments", comments);
            carmsg.put("order_id",orderId);
            carmsg.put("rating",rating);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int sendComplain(int orderId, int driverId, int complainId, ResponseHandler handler) {
        int ret = -1;
        JSONObject complain = new JSONObject();
        try{
            complain.put("cmd", MessageTag.getInstance().Command(MessageTag.COMPLAIN_SERVICE));
            complain.put("role", "2");
            complain.put("order_id", orderId);
            complain.put("driver_id", driverId);
            complain.put("complain_id", complainId);
            ret = sendMessage(complain, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }
//
//    order_id
//    * 		pay_price 实际支付金额
//    * 		pay_time  支付时间
//    * 		pay_type  支付方式 1-支付宝 2-微信 3-银联
    public int sendPayOverRequest( int orderid ,Long timestamp, String price, int type, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.PAY_OVER));
            carmsg.put("role","2");
            carmsg.put("pay_price",price);
            carmsg.put("pay_time",timestamp);
            carmsg.put("pay_type",type);
            carmsg.put("order_id", orderid);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }
    // 检测账单是否支付

    public int checkIfPaid(int orderid, ResponseHandler handler){
        int ret = -1;
        JSONObject check = new JSONObject();

        try {
            check.put("cmd", "pay_check");
            check.put("order_id", orderid);
            check.put("role", "2");

            ret = sendMessage(check, handler, 5);
        } catch (JSONException e){
            e.printStackTrace();
        }

        return ret;
    }


    /*
     * 获取乘客端基本信息，
     * @author hyman
     * @param role
     * @param handler
     * @return
     */
    public int getBaseInfoRequest(String role, ResponseHandler handler) {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.BASEINFO));
            carmsg.put("role", role);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }


    /* 乘客在等待司机，还没上车的这个阶段取消订单，要提交取消原因
     * @author hyman
     * @param reason_id
     * @param role
     * @param handler
     * @return
     */
    public int sendCancelOrderRequest(int reason_id, String role, ResponseHandler handler) {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.CANCEL_ORDER));
            carmsg.put("role", role);
            carmsg.put("reason_id", reason_id);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }


    /*
     * 获取乘客端历史订单
     * @author hyman
     *
     * @param type
     * @param number
     * @param order_id
     * @param handler
     * @return
     */
    public int getHistoryOrders(String type, int number, Long order_id, ResponseHandler handler) {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.HISTORY_ORDERS));
            carmsg.put("role", "2");
            carmsg.put("type", type);
            carmsg.put("number", number);
            carmsg.put("order_id", order_id);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }


    /**
     * 拉取消息
     * @param type
     * @param num
     * @param msgId
     * @param handler
     * @return
     */
    public int pullMessages(String type, int num, int msgId, ResponseHandler handler){
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", "get_message");
            params.put("role", "2");
            params.put("type", type);
            params.put("number", num);
            params.put("base_message_id", msgId);
            ret = sendMessage(params, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();;
        }
        return ret;
    }

    /**
     * 上传反馈信息
     * @param feedback
     * @param handler
     * @return
     */
    public int publishFeedback(String feedback, ResponseHandler handler) {
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", MessageTag.getInstance().Command(MessageTag.PUBLISH_FEEDBACK));
            params.put("role", "2");
            params.put("feedback", feedback);
            ret = sendMessage(params, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 获取乘客个人信息
     * @param handler
     * @return
     */
    public int getPersonalInfo(ResponseHandler handler) {
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", MessageTag.getInstance().Command(MessageTag.GET_PERSONAL_INFO));
            params.put("role", "2");
            ret = sendMessage(params, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 更新乘客个人信息
     * @param map
     * @param handler
     * @return
     */
    public int updatePersonalInfo(Map<String, String> map, ResponseHandler handler) {
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", MessageTag.getInstance().Command(MessageTag.UPDATE_PERSONAL_INFO));
            params.put("role", "2");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }
            ret = sendMessage(params, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }


    /**
     * 乘客实名认证
     * @param realname
     * @param idnumber
     * @param handler
     * @return
     */
    public int certifyRealname(String realname, String idnumber, ResponseHandler handler) {
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", MessageTag.getInstance().Command(MessageTag.CERTIFY_REALNAME));
            params.put("role", "2");
            params.put("realname",realname);
            params.put("idnumber", idnumber);
            ret = sendMessage(params, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 查询认证状态（包括实名认证和车主认证）
     * @param handler
     * @return
     */
    public int checkCertifyStatus(ResponseHandler handler) {
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", MessageTag.getInstance().Command(MessageTag.CHECK_CERTIFY_STATUS));
            params.put("role", "2");
            ret = sendMessage(params, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }


}