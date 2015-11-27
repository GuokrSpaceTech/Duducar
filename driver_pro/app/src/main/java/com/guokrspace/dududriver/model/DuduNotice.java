package com.guokrspace.dududriver.model;

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

    public DuduNotice(String message){
        try{
            JSONObject object = new JSONObject(message);
            title = (String)object.get("title");
            url = (String)object.get("url");
            content = (String)object.get("content");
            time = (String)object.get("time");
            notice_id = (String)object.get("notice_id");
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
