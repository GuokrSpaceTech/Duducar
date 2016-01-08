package com.guokrspace.dududriver.net;

import android.os.Message;
import android.util.Log;

import com.guokrspace.dududriver.net.message.HeartBeatMessage;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Prashant Adesara, Kai Yang
 * Handle the TCPClient with Socket Server. 
 * */

public class SocketClient {

    private static SocketClient s_socketClient;
    private String serverMessage;
    private AtomicInteger messageid = new AtomicInteger();
    private int blockMessage = 0;
    /**
     * Specify the Server Ip Address here. Whereas our Socket Server is started.
     * */
    public static final String SERVERIP = "120.24.237.15"; // your computer IP address
    public static final int SERVERPORT = 8282;
    private boolean mRun = false;

    private Socket socket;
    private PrintWriter out = null;
    private BufferedReader in = null;

    public ResponseHandler messageParsor;

    public static SocketClient getInstance() {
        if (s_socketClient == null) {
            synchronized (SocketClient.class) {
                if (s_socketClient == null) {
                    s_socketClient = new SocketClient();
                }
            }
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
        messageid.getAndSet(0);
    }


    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public int sendMessage(final JSONObject message, final ResponseHandler handler, final int timeout){
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

                Log.e("DADDY " , mRun + "d");
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
            MessageDispatcher messageDispatcher = new MessageDispatcher(messageid.get(), message, timerRunnable, handler);
            messageDispatchQueue.put(messageid.get(), messageDispatcher);

            ret = messageid.get();
            messageid.getAndIncrement();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }


    public void stopClient(){
        mRun = false;
    }

    public Socket getSocket(){
        return socket;
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

//                        serverMessage = removeBOM(serverMessage);
                        //call the method messageReceived in MainActivity class
//                        serverMessage = new String(serverMessage.getBytes("UTF-8"), "UTF-8");
//                        serverMessage = convertStandardJSONString(serverMessage);
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
                            if (target != null)
                                target.sendResponse(serverMessage);
                            else {
                                String errorMsg = String.format("Unregisted Message: %s", serverMessage);
                                Log.i("DuduCar", errorMsg);
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
                Log.e("TCP SI Error Inner", "SI: Error", e);
                e.printStackTrace();
//                SocketClient.getInstance().run();
            }
            finally
            {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP SI Error Outer", "SI: Error", e);
//            SocketClient.getInstance().run();
        }
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

    HashMap<Integer, ResponseHandler> serverMessageDispatchMap = new HashMap<>();

    public void registerServerMessageHandler( int cmd, ResponseHandler target)
    {
        Log.e("daddy", "register  " + cmd);
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
            ret = sendMessage(regcodeReq, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int sendVerifyRequst(String mobile, String role, String pwd, String regcode, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject verify = new JSONObject();
        try {
            verify.put("cmd", "verify");
            verify.put("role", role);
            verify.put("mobile", mobile);
            verify.put("verifycode", regcode);
            verify.put("password", pwd);
            ret = sendMessage(verify, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int autoLoginRequest(String mobile, String role, String token, ResponseHandler handler) {
        int ret = -1;
        JSONObject loginRequest = new JSONObject();
        try {
            loginRequest.put("cmd", "login");
            loginRequest.put("mobile", mobile);
            loginRequest.put("role", role);
            loginRequest.put("token", token);
            ret = sendMessage(loginRequest, handler, 5);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int autoLoginRequestWithPwd(String mobile, String role, String pwd, ResponseHandler handler) {
        int ret = -1;
        JSONObject loginRequest = new JSONObject();
        try {
            loginRequest.put("cmd", "login");
            loginRequest.put("mobile", mobile);
            loginRequest.put("role", role);
            loginRequest.put("password", pwd);
            ret = sendMessage(loginRequest, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int orderOrder(String order_no, ResponseHandler handler){
        int ret = -1;
        JSONObject orderOrder = new JSONObject();
        try{
            orderOrder.put("cmd", "accept");
            orderOrder.put("role", "1");
            orderOrder.put("order_id", order_no);

            ret = sendMessage(orderOrder, handler, 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int startOrder(String order_no, ResponseHandler handler){
        int ret  = -1;
        JSONObject stOrder = new JSONObject();
        try {
            stOrder.put("cmd", "order_start");
            stOrder.put("role", "1");
            stOrder.put("lat", CommonUtil.getCurLat());
            stOrder.put("lng", CommonUtil.getCurLng());
            stOrder.put("order_no", order_no);
            ret = sendMessage(stOrder, handler, 5);
        } catch (JSONException e){
            e.printStackTrace();
        }

        return ret;
    }

    public int endOrder(String price, String mileage, String lowSpeed, String add1, String add2, String add3, ResponseHandler handler){
        int ret = -1;
        JSONObject edOrder = new JSONObject();
        try {
            edOrder.put("cmd", "order_end");
            edOrder.put("price", price);
            edOrder.put("mileage", mileage);
            edOrder.put("add_price1", add1);
            edOrder.put("add_price2", add2);
            edOrder.put("add_price3", add3);
            edOrder.put("low_speed_time", lowSpeed);

            edOrder.put("lat", CommonUtil.getCurLat());
            edOrder.put("lng", CommonUtil.getCurLng());
            edOrder.put("role", "1");
            ret = sendMessage(edOrder, handler, 5);
        } catch (JSONException e){
            e.printStackTrace();
        }

        return ret;
    }

    public int checkIfPaid(int orderid, ResponseHandler handler){
        int ret = -1;
        JSONObject check = new JSONObject();

        try {
            check.put("cmd", "pay_check");
            check.put("order_id", orderid);
            check.put("role", "1");

            ret = sendMessage(check, handler, 5);
        } catch (JSONException e){
            e.printStackTrace();
        }

        return ret;
    }

    public int endOrderSelfPay(int orderid, String price, String mileage, ResponseHandler handler){
        int ret = -1;
        JSONObject edOrder = new JSONObject();
        try {
            edOrder.put("cmd", "driver_pay");
            edOrder.put("order_id", orderid);
            edOrder.put("price", price);
            edOrder.put("mileage", mileage);
            edOrder.put("lat", CommonUtil.getCurLat());
            edOrder.put("lng", CommonUtil.getCurLng());
            edOrder.put("role", "1");
            ret = sendMessage(edOrder, handler, 5);
        } catch (JSONException e){
            e.printStackTrace();
        }

        return ret;
    }

    public int pullBaseInfo(ResponseHandler handler){
        int ret = -1;
        JSONObject baseInfo = new JSONObject();
        try {

            baseInfo.put("cmd", "baseinfo");
//            baseInfo.put("cmd", MessageTag.getInstance().Command(MessageTag.HISTORY_ORDERS));
            baseInfo.put("role", "1");
            ret = sendMessage(baseInfo, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int sendCurrentChargeDetail(double curCharge, double curmile, int lowspeed, ResponseHandler handler){
        int ret = -1;
        JSONObject chargeDetail = new JSONObject();
        try {
            curmile = new BigDecimal(curmile).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            curCharge = new BigDecimal(curCharge).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            chargeDetail.put("cmd", "current_charge");
            chargeDetail.put("role", "1");
            chargeDetail.put("current_mile", curmile+"");
            chargeDetail.put("current_charge", curCharge+"");
            chargeDetail.put("low_speed_time", lowspeed + "");
            chargeDetail.put("current_lat", CommonUtil.getCurLat()+"");
            chargeDetail.put("current_lng", CommonUtil.getCurLng()+"");
            chargeDetail.put("current_time", System.currentTimeMillis()+"");
            ret = sendMessage(chargeDetail, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int sendHeartBeat(HeartBeatMessage heartBeatMessage,ResponseHandler handler)
    {
        int ret = -1;
        JSONObject heartbeat = new JSONObject();
        try {
            heartbeat.put("cmd", heartBeatMessage.getCmd());
            heartbeat.put("role", "1");
            heartbeat.put("status", CommonUtil.getCurrentStatus() == null ? "0" : CommonUtil.getCurrentStatus());
            heartbeat.put("lat",heartBeatMessage.getLat());
            heartbeat.put("lng", heartBeatMessage.getLng());
            heartbeat.put("speed",heartBeatMessage.getSpeed());
            ret = sendMessage(heartbeat, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int getHistoryOrders (String type, int num, Long orderId, ResponseHandler handler) {
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", MessageTag.getInstance().Command(MessageTag.HISTORY_ORDERS));
            params.put("role", "1");
            params.put("type", type);
            params.put("number", num);
            params.put("order_id", orderId);
            ret = sendMessage(params, handler, 5);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    public int pullMessages(String type, int num, int messageId, ResponseHandler handler){
        int ret = -1;
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", "get_message");
            params.put("role", "1");
            params.put("type", type);
            params.put("number", num);
            params.put("base_message_id", messageId);
            ret = sendMessage(params, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();;
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
            carmsg.put("role","1");
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


    /*
     * 获取司机端余额和交易明细列表
     * @author hyman
     *
     * @param type
     * @param number
     * @param bill_id
     * @param handler
     * @return
     */
    public int getBillsRequest(String type, int number, Long bill_id, ResponseHandler handler) {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.GET_BILLS));
            carmsg.put("role", "1");
            carmsg.put("type", type);
            carmsg.put("number", number);
            carmsg.put("bill_id", bill_id);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /*
     * 获取可提现额度
     * @author hyman
     *
     * @param role
     * @param handler
     * @return
     */
    public int sendGetBalanceRequest(String role, ResponseHandler handler) {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.GET_BALANCE));
            carmsg.put("role", role);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /*
     * 发起提现请求
     * @author hyman
     *
     * @param role
     * @param cash
     * @param handler
     * @return
     */
    public int sendWithdrawRequest(String role, double cash, ResponseHandler handler ) {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", MessageTag.getInstance().Command(MessageTag.WITHDRAW_CASH));
            carmsg.put("role", role);
            carmsg.put("cash", cash);
            ret = sendMessage(carmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }


}