package com.guokrspace.dududriver.common;

/**
 * Created by hyman on 15/10/20.
 */
public class Constants {

    public static String CMD_REGISTER = "register";
    public static String CMD_VERIFY = "verify";
    public static String CMD_LOGIN = "login";
    public static String CMD_CREATE_ORDER ="create_order";

    public static String FIELD_MESSAGE_ID = "message_id";
    public static String FIELD_STATUS = "status";
    public static String FIELD_MESSAGE = "message";
    public static String FIELD_TOKEN = "token";

    public static String STATUS_HOLD = "0";
    public static String STATUS_WAIT = "1";
    public static String STATUS_DEAL = "2";
    public static String STATUS_GET  = "3";
    public static String STATUS_RUN  = "4";

    public static final String SERVICE_BROADCAST = "SERVICE_BROADCAST";
    public static final String SERVICE_ACTION_RELOGIN = "LOG_IN_ERROR_TRY_AGAIN";
    public static final String SERVICE_ACTION_MESAGE = "GOT_A_NEW_MESSAGE";

    public static final String ACTION_NEW_ORDER = "GOT_A_NEW_ORDER";
    public static final int MESSAGE_NEW_ORDER = 0x5009;

    public static final int ORDER_PAGE_NUM = 10;
    public static final int MESSAGE_PER_PAGE = 40;


}
