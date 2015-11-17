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
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * @author Prashant Adesara, Kai Yang
 * Handle the TCPClient with Socket Server. 
 * */

public class SocketClient {

    private static SocketClient s_socketClient;
    private String serverMessage;
    private int messageid;
    /**
     * Specify the Server Ip Address here. Whereas our Socket Server is started.
     * */
    public static final String SERVERIP = "120.24.237.15"; // your computer IP address
    public static final int SERVERPORT = 8282;
    private boolean mRun = false;

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
        messageid = 0;
    }


    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public int sendMessage(final JSONObject message, final ResponseHandler handler, final int timeout){
        int ret = -1; //Default is error
        Log.e("daddy   ", "eeee"+ out.toString());
        //Send the message
        try {
            message.put("message_id", messageid);

            if (out != null && !out.checkError()) {
                Log.e("SENT TO SERVER", "S: Sent Message: '" + message.toString() + "'");
                out.println(message.toString());
                out.flush();

                Log.e("DADDY " , mRun + "d");
                //Start a timer
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
            }
            Log.e("daddy", "out" + (out == null ) + (out.checkError()));
            ret = messageid;

            messageid++;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
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
            Socket socket = new Socket(serverAddr, SERVERPORT);
//            socket.setSoTimeout(30000);
            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP SI Client", "SI: Sent.");
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
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


    public int sendVerifyRequst(String mobile, String role, String regcode, ResponseHandler handler)
    {
        int ret = -1;
        JSONObject verify = new JSONObject();
        try {
            verify.put("cmd", "verify");
            verify.put("role",role);
            verify.put("mobile",mobile);
            verify.put("verifycode",regcode);
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

    public int endOrder(String price, String mileage, ResponseHandler handler){
        int ret = -1;
        JSONObject edOrder = new JSONObject();
        try {
            edOrder.put("cmd", "order_end");
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
            baseInfo.put("role", "1");
            ret = sendMessage(baseInfo, handler, 5);
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
            heartbeat.put("status", CommonUtil.getCurrentStatus());
            heartbeat.put("lat",heartBeatMessage.getLat());
            heartbeat.put("lng", heartBeatMessage.getLng());
            heartbeat.put("speed",heartBeatMessage.getSpeed());
            ret = sendMessage(heartbeat, handler, 5);
        } catch (JSONException e) {
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

    public int getHistoryOrders (String type, int num, int orderId, ResponseHandler handler) {
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


    private String convertStandardJSONString(String data_json) {
        data_json = data_json.replaceAll("\\\\r\\\\n", "");
        data_json = data_json.replaceAll("\\\\", "");
        data_json = data_json.replace("\"{", "{");
        data_json = data_json.replace("}\",", "},");
        data_json = data_json.replace("}\"", "}");
        return data_json;
    }

}