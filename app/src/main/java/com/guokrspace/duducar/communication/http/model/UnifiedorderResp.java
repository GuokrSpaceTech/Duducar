package com.guokrspace.duducar.communication.http.model;

/**
 * Created by hyman on 15/11/23.
 */
public class UnifiedorderResp {

    public int status;//1，成功；-1失败

    public String msg;

    public String prepayid;

    public String sign;

    public String noncestr;

    public String timestamp;

    @Override
    public String toString() {
        return "status: " + status
                + "msg: " + msg
                + "sign: " + sign
                + "nonceStr: " + noncestr
                + "timestamp: " + timestamp;
    }
}
