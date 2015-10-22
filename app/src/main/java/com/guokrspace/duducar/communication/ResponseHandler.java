package com.guokrspace.duducar.communication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.guokrspace.duducar.communication.fastjson.FastJsonTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public abstract class ResponseHandler {

    private static ResponseHandler s_parser;

    public static ResponseHandler getInstance(){
        return s_parser;
    }

    public HashMap<String, Integer> MessageTags = new HashMap<>();     //Map between String type of message command - Int type of message tag
    public HashMap<Integer, Response> responseParserMap = new HashMap<>(); //Map of varous message parsor

    public ResponseHandler(){

        s_parser = this;

        responseParserMap.put(MessageTag.GET_NEAR_CAR_RESP, new Response() {
            @Override
            public Object parseResponse(String response) {
                return FastJsonTools.getObject(response, NearByCars.class);
            }
        });

        MessageTags.put("get_near_car_req", MessageTag.GET_NEAR_CAR_REQ);
        MessageTags.put("get_near_car_resp", MessageTag.GET_NEAR_CAR_RESP);
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

    public abstract void onSuccess();

    public abstract void onFailure();

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

    /**
     * Created by kai on 10/22/15.
     */
    public static class NearByCars {

        int message_id;
        String status;
        String cmd;
        List<CarLocation> cars;

        class CarLocation {
            double lat;
            double lng;
            String car_grade;
        }
    }
}