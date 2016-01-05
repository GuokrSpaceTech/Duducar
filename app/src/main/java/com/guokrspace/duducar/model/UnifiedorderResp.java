package com.guokrspace.duducar.model;

/**
 * Created by hyman on 15/11/23.
 */
public class UnifiedorderResp extends BaseModel{

    public int status;//1，成功；-1失败

    public String msg;

    public String prepayid;

    public String sign;

    public String noncestr;

    public String timestamp;

    public String sid;

}
