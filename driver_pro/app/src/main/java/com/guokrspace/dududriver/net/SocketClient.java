package com.guokrspace.dududriver.net;

import android.os.Message;
import android.util.Log;


import com.guokrspace.dududriver.common.MessageTag;

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
import java.util.Iterator;
import java.util.Map;

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
    public static final int TIME_OUT = 5;

    private boolean mRun = false;

    private PrintWriter out = null;
    private BufferedReader in = null;

    public ResponseHandler messageParsor;



    public static SocketClient getInstance() {
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


        //Send the message
        try {
            message.put("message_id", messageid);

            if (out != null && !out.checkError()) {
                Log.e("SENT TO SERVER", "S: Sent Message: '" + message.toString() + "'");
                out.println(message.toString());
                out.flush();

                //Start a timer
                final Runnable timerRunnable = new Runnable() {
                    int counter=0;
                    @Override
                    public void run() {
                        counter ++;
                        if(counter < timeout) {
                            handler.postRunnableDelay(this, 1000); //1s interval
                        } else {
                            Message msg = handler.obtainMessage(MessageTag.MESSAGE_TIMEOUT,message.toString());
                            handler.sendMessage(msg);
                        }
                    }
                };

                //Enqueue
                MessageDispatcher messageDispatcher = new MessageDispatcher(messageid, message, timerRunnable, handler);
                messageDispatchQueue.put(messageid, messageDispatcher);
//                handler.postDelayed(timerRunnable, 0);
            }

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
                        //call the method messageReceived in MainActivity class
                        JSONObject jsonObject = new JSONObject(serverMessage);
                        if(jsonObject.has("message_id")) {

                            int messageid = (int)jsonObject.get("message_id");
                            String command = (String)jsonObject.get("cmd");

                            MessageDispatcher dispatcher = messageDispatchQueue.get(messageid);
                            dispatcher.setResponse(jsonObject);


                            if(dispatcher != null)
                            {
                                ResponseHandler handlerTest = dispatcher.target;
                                handlerTest.sendResponse(serverMessage);
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
        }

        public void setResponse(JSONObject response) {
            this.response = response;
        }
    }

    /**
     *  a common method for send request
     *
     *  Author:hyman
     *  Date:15/10/20
     *
     */
    public int sendRequest(Map<String, String> params, ResponseHandler handler) {
        int _messageId = -1;
        JSONObject requestBody = new JSONObject();
        if (params != null) {
            Iterator<String> keyIterator = params.keySet().iterator();
            try {
                for (; keyIterator.hasNext();) {
                    String paramName = keyIterator.next();
                    requestBody.put(paramName, params.get(paramName));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        _messageId = sendMessage(requestBody, handler, TIME_OUT);
        return _messageId;
    }

}