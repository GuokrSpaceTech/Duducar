package com.guokrspace.dududriver.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by daddyfang on 15/11/27.
 */
public class DuduNotice implements BaseNoticeItem {

    public String title;
    public String url;
    public String content;
    public String time;
    public String notice_id;
    public boolean cancel = false;

    public DuduNotice(String message){
        if(message == null || message.length() == 0){
           return;
        }
        try{
            Log.e("daddy notice", "message" + message);

            JSONObject object = new JSONObject(message);
            title = object.get("title") == null ? "嘟嘟播报" : (String) object.get("title");
            url = object.get("url") == null ? "" : "";
            content = object.get("content") == null ? "嘟嘟欢迎您" : (String)object.get("content");
            time = object.get("time") == null ? (System.currentTimeMillis() + 1000*60*60*24) +"" : object.get("time").toString();
            notice_id = object.get("notice_id").toString();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public DuduNotice(){
        title = "欢迎您成为一位嘟嘟合作司机";
        url = "";
        content = "嘟嘟在你身边";
        time = System.currentTimeMillis()+"";
        notice_id = "0";
    }

}
