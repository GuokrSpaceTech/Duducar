package com.guokrspace.dududriver.model;

import java.io.Serializable;

/**
 * Created by daddyfang on 15/11/27.
 */
public class DuduMessage implements Serializable {

    private String message_id;
    private String message_type; //1支付通知，2系统通知， 3 热力地图
    private String message_body;

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getMessage_body() {
        return message_body;
    }

    public void setMessage_body(String message_body) {
        this.message_body = message_body;
    }
}
