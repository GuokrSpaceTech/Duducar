package com.guokrspace.duducar.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Prashant Adesara
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
    private OnMessageReceived mMessageListener = null;
    private ResponseListener  mResponseListener = null;
    private boolean mRun = false;

    private PrintWriter out = null;
    private BufferedReader in = null;

    public MessageParsor messageParsor;

    public static SocketClient getInstance() {
        return s_socketClient;
    }
    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public SocketClient(final OnMessageReceived listener)
    {
        mMessageListener = listener;
        s_socketClient = this;
        messageid = 0;
        messageParsor = new MessageParsor();
    }


    public void setmResponseListener(ResponseListener mResponseListener) {
        this.mResponseListener = mResponseListener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public int sendMessage(final JSONObject message, final Handler handler, final int timeout){
        int ret = -1; //Default is error

        final Runnable timerRunnable = new Runnable() {
            int counter=0;
            @Override
            public void run() {
                counter ++;
                if(counter < timeout) {
                    handler.postDelayed(this, 1000); //1s interval
                } else {
                    Message msg = handler.obtainMessage();
                    msg.what = HandlerMessageTag.MESSAGE_TIMEOUT;
                    msg.obj  = message;
                    handler.sendMessage(msg);
                }
            }
        };

        try {
            message.put("message_id", messageid);

            if (out != null && !out.checkError()) {
                Log.e("SENT TO SERVER", "S: Sent Message: '" + message.toString() + "'");
                out.println(message.toString());
                out.flush();

                MessageDispatcher messageDispatcher = new MessageDispatcher(messageid, message, timerRunnable, handler);
                messageDispatchQueue.put(messageid, messageDispatcher);
                handler.postDelayed(timerRunnable, 0);
            }

            ret = messageid;

            messageid++;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(JSONObject message){

        try {
            message.put("message_id", String.valueOf(messageid));
            messageid++;

            if (out != null && !out.checkError()) {
                System.out.println("message: "+ message);
                out.println(message);
                out.flush();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived in MainActivity class
                        JSONObject jsonObject = new JSONObject(serverMessage);
                        if(jsonObject.has("message_id")) {

                            int messageid = (int)jsonObject.get("message_id");

                            MessageDispatcher dispatcher = messageDispatchQueue.get(messageid);

                            if (dispatcher != null) {
                                Handler handler = dispatcher.target;
                                Message message = handler.obtainMessage();
                                message.what = 0x6001;
                                message.obj = jsonObject;
                                handler.sendMessage(message);
                                dispatcher.target.removeCallbacks(dispatcher.timer);
                                messageDispatchQueue.remove(messageid);
                            } else {
                                //Todo: Generic message handler
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
        Handler target;
        Runnable timer;

        public MessageDispatcher(int messageid, JSONObject messagebody, Runnable timer, Handler target) {
            this.messageid = messageid;
            this.request = messagebody;
            this.target = target;
        }

        public void setResponse(JSONObject response) {
            this.response = response;
        }
    }

    public class MessageParsor {

        public MessageParsor(){}

        public int messageid(JSONObject message)
        {
            int messageid = -1;
            if(message.has("message_id"))
                try {
                    messageid = (int)message.get("message_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return messageid;
        }

        public int retcode(JSONObject message)
        {
            int ret = -1;
            if(message.has("status"))
                try {
                    ret = (int)message.get("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return ret;
        }

        public String description(JSONObject message)
        {
            String ret = "";
            if(message.has("message"))
                try {
                    ret = (String)message.get("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return ret;
        }

        public String token(JSONObject message)
        {
            String ret = "";
            if(message.has("token"))
                try {
                    ret = (String)message.get("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return ret;
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MainActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    //Handle the received messaged based on its content
    public interface ResponseListener{
        public void onResponseReceived(JSONObject response);
    }

    public int sendRegcodeRequst(String mobile, String role, Handler handler)
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


    public int sendVerifyRequst(String mobile, String role, String regcode, Handler handler)
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


    public int sendLoginReguest(String mobile, String role, String token, Handler handler)
    {
        int ret = -1;
        JSONObject loginmsg = new JSONObject();
        try {
            loginmsg.put("cmd", "login");
            loginmsg.put("role",role);
            loginmsg.put("mobile",mobile);
            loginmsg.put("token",token);
            ret = sendMessage(loginmsg, handler, 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int sendCarRequest(String role, String start, String dest, Double start_lat,
                              Double start_lng, Double dest_lat, Double dest_lng,
                              String distance, String price, String car,Handler handler)
    {
        int ret = -1;
        JSONObject carmsg = new JSONObject();
        try {
            carmsg.put("cmd", "create_order");
            carmsg.put("role",role);
            carmsg.put("start",start);
            carmsg.put("destination",dest);
            carmsg.put("start_lat",start_lat);
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

//    public void receiveRegcodeResponse(String message)
//    {
////        JSONObject jsonObject =
//
//    }
//
//    public void sendHeartBeat(String lat, String lng, String status)
//    {
//        JSONObject loginRequest = new JSONObject();
//        try {
//            loginRequest.put("cmd","register");
//            sendMessage(loginRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendBaseInfoRequest()
//    {
//        JSONObject loginRequest = new JSONObject();
//        try {
//            loginRequest.put("cmd","register");
//            sendMessage(loginRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void receiveBaseInfoRequest()
//    {
//        JSONObject loginRequest = new JSONObject();
//        try {
//            loginRequest.put("cmd","register");
//            sendMessage(loginRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//    public void sendVerifyRequest(String mobile, String sms, String role)
//    {
//        JSONObject verifyRequest = new JSONObject();
//        try {
//            verifyRequest.put("cmd","verify");
//            verifyRequest.put("regcode",sms);
//            verifyRequest.put("mobile",mobile);
//            verifyRequest.put("role",role);
//            sendMessage(verifyRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void receiveVerifyRequest(String mobile, String sms, String role)
//    {
//        JSONObject verifyRequest = new JSONObject();
//        try {
//            verifyRequest.put("cmd","verify");
//            verifyRequest.put("regcode",sms);
//            verifyRequest.put("mobile",mobile);
//            verifyRequest.put("role",role);
//            sendMessage(verifyRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendLoginRequest(String token, String role)
//    {
//        JSONObject loginRequest = new JSONObject();
//        try {
//            loginRequest.put("cmd","login");
//            loginRequest.put("token",token);
//            loginRequest.put("role",role);
//            sendMessage(loginRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void receiveLoginResponse(String token, String role)
//    {
//        JSONObject loginRequest = new JSONObject();
//        try {
//            loginRequest.put("cmd","login");
//            loginRequest.put("token",token);
//            loginRequest.put("role",role);
//            sendMessage(loginRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendBaseInfoRequest(String session, String role)
//    {
//        JSONObject baseinfoRequest = new JSONObject();
//        try {
//            baseinfoRequest.put("cmd","baseinfo");
//            baseinfoRequest.put("session",session);
//            baseinfoRequest.put("role",role);
//            sendMessage(baseinfoRequest.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

}