package com.guokrspace.dududriver.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public abstract class ResponseHandler implements ResponseHandleInterface {

    private static ResponseHandler s_parser;
    private Handler handler;
    private Looper looper = null;
    private String responseString;

    @Override
    public Object sendResponse(String response) {
        Log.e("daddy", "response" + response);
        preParseResponse(response);
        return "1";
    }

    protected static final int SUCCESS_MESSAGE = 0;
    protected static final int FAILURE_MESSAGE = 1;
    protected static final int TIMEOUT_MESSAGE = 2;


    public static ResponseHandler getInstance(){
        return s_parser;
    }

    public HashMap<String, Integer> MessageTags = new HashMap<>();     //Map between String type of message command - Int type of message tag
//    public HashMap<Integer, Response> responseParserMap = new HashMap<>(); //Map of varous message parsor


    //Constructor
    public ResponseHandler() {
        this(null);
    }


    public ResponseHandler(Looper looper) {
        this.looper = looper == null ? Looper.myLooper() : looper;
        handler = new ResponderHandler(this, looper);
    }

    private void preParseResponse(String responseString)
    {
        try {
            JSONObject jsonObject = new JSONObject(responseString);

            //Server Originated Message
            if(!jsonObject.has("message_id")) {
                Log.e("daddy", "JSONobject" + jsonObject.toString());
                sendSuccessMessage(responseString);

            } else { //Client Originated Message
                if (jsonObject.getInt("status") == 1) {
                    sendSuccessMessage(responseString);
                } else {
                    sendFailureMessage(responseString);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendSuccessMessage(String messagebody)
    {
        sendMessage(obtainMessage(SUCCESS_MESSAGE,messagebody));
    }

    private void sendFailureMessage(String messagebody)
    {
        sendMessage(obtainMessage(FAILURE_MESSAGE,messagebody));
    }

    protected Message obtainMessage(int responseMessageId, Object responseMessageData) {
        return Message.obtain(handler, responseMessageId, responseMessageData);
    }

    protected void sendMessage(Message msg) {
        if (handler == null) {
            handleMessage(msg);
        } else if (
//                !Thread.currentThread().isInterrupted()) {
                !handler.getLooper().getThread().isInterrupted()) { // do not send messages if request has been cancelled
            handler.sendMessage(msg);
        }
    }

    /**
     * Helper method to send runnable into local handler loop
     *
     * @param runnable runnable instance, can be null
     */
    protected void postRunnable(Runnable runnable) {
        if (runnable != null) {
            if (handler == null) {
                // This response handler is synchronous, run on current thread
                runnable.run();
            } else {
                // Otherwise, run on provided handler
                handler.post(runnable);
            }
        }
    }

    /**
     * Helper method to send runnable into local handler loop
     *
     * @param runnable runnable instance, can be null
     */
    protected void postRunnableDelay(Runnable runnable, int delay) {
        if (runnable != null) {
            if (handler == null) {
                // This response handler is synchronous, run on current thread
                runnable.run();
            } else {
                // Otherwise, run on provided handler
                handler.postDelayed(runnable, delay);
            }
        }
    }

    protected void stopRunnable(Runnable runnable)
    {
        if(runnable != null && handler != null)
        {
            handler.removeCallbacks(runnable);
        }
    }

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
        else
            ret = 1;
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



    public abstract void onSuccess(String messageBody);

    public abstract void onFailure(String error);

    public abstract void onTimeout();


    /**
     * Avoid leaks by using a non-anonymous handler class.
     */
    private static class ResponderHandler extends Handler {
        private final ResponseHandler mResponder;

        ResponderHandler(ResponseHandler mResponder, Looper looper) {
            super(looper);
            this.mResponder = mResponder;
        }

        @Override
        public void handleMessage(Message msg) {
            mResponder.handleMessage(msg);
        }
    }

    protected void handleMessage(Message message) {

        try {
            switch (message.what) {
                case SUCCESS_MESSAGE:
                    String messagebody = (String) message.obj;
                    onSuccess(messagebody);
                    break;
                case FAILURE_MESSAGE:
                    messagebody = (String) message.obj;
                    onFailure(messagebody);
                    break;
                case TIMEOUT_MESSAGE:
                    onTimeout();
                    break;
                default:
                    break;
            }

        } catch (Throwable error) {

        }
    }
}