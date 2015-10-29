package com.guokrspace.dududriver.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
        preParseResponse(response);
        return "1";
    }

    protected static final int SUCCESS_MESSAGE = 0;
    protected static final int FAILURE_MESSAGE = 1;
    protected static final int TIMEOUT_MESSAGE = 2;


    public static ResponseHandler getInstance() {
        return s_parser;
    }

    public HashMap<String, Integer> MessageTags = new HashMap<>();     //Map between String type of message command - Int type of message tag
//    public HashMap<Integer, Response> responseParserMap = new HashMap<>(); //Map of varous message parsor


    //Constructor
    public ResponseHandler() {
        this(null);
        this.responseString = responseString;
    }


    public ResponseHandler(Looper looper) {
        this.looper = looper == null ? Looper.myLooper() : looper;

    }

    private void preParseResponse(String responseString) {
        try {
            JSONObject jsonObject = new JSONObject(responseString);

            String cmd = (String) jsonObject.get("cmd");

            if (jsonObject.getInt("status") == 1) {
                sendSuccessMessage(responseString);
            } else {
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendSuccessMessage(String messagebody) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, messagebody));
    }

    private void sendFailureMessage() {

    }

    protected Message obtainMessage(int responseMessageId, Object responseMessageData) {
        return Message.obtain(handler, responseMessageId, responseMessageData);
    }

    protected void sendMessage(Message msg) {
        if (handler == null) {
            handleMessage(msg);
        } else if (!Thread.currentThread().isInterrupted()) { // do not send messages if request has been cancelled
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

    /**
     * a common method for obtaining a field value
     * Author: hyman
     * Date: 15/10/20
     *
     * @param message
     * @param fieldName
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getFieldVal(JSONObject message, String fieldName, Class<T> type) {
        Object res = null;
        String className = type.getSimpleName();
        try {
            if ("Integer".equals(className)) {
                res = message.has(fieldName) ? message.getInt(fieldName) : -1;
            } else if ("String".equals(className)) {
                res = message.has(fieldName) ? message.getString(fieldName) : "";
            } else if ("Boolean".equals(className)) {
                res = message.has(fieldName) ? message.getBoolean(fieldName) : Boolean.FALSE;
            } else if ("Double".equals(fieldName)) {
                res = message.has(fieldName) ? message.getDouble(fieldName) : -1;
            } else if ("Long".equals(fieldName)) {
                res = message.has(fieldName) ? message.getLong(fieldName) : -1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return (T) res;
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