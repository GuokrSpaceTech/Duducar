package com.guokrspace.duducar.communication.http.model;

import java.lang.reflect.Field;

/**
 * Created by hyman on 15/11/26.
 */
public class TradeResult extends BaseModel{

    public String sumprice;

    public int status;//1，成功；-1失败

    public String msg;

}
