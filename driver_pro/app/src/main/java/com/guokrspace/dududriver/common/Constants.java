package com.guokrspace.dududriver.common;

/**
 * Created by hyman on 15/10/20.
 */
public class Constants {

    public static final int SOUND_TIME = 1;
    public static final String ENTER_FROM_LOGIN = "enter_from_login";
    public static String CMD_REGISTER = "register";
    public static String CMD_VERIFY = "verify";
    public static String CMD_LOGIN = "login";
    public static String CMD_CREATE_ORDER ="create_order";

    public static String FIELD_MESSAGE_ID = "message_id";
    public static String FIELD_STATUS = "status";
    public static String FIELD_MESSAGE = "message";
    public static String FIELD_TOKEN = "token";

    public static String STATUS_HOLD = "100";
    public static String STATUS_WAIT = "101";
    public static String STATUS_DEAL = "102";
    public static String STATUS_GET  = "103";
    public static String STATUS_RUN  = "104";
    public static String STATUS_REACH = "105";
    public static String STATUS_END = "6";
    public static String STATUS_PAY = "7";

    public static final String SERVICE_BROADCAST = "SERVICE_BROADCAST";
    public static final String SERVICE_ACTION_RELOGIN = "LOG_IN_ERROR_TRY_AGAIN";
    public static final String SERVICE_ACTION_MESAGE = "GOT_A_NEW_MESSAGE";
    public static final String SERVICE_ACTION_NEW_ORDER = "SERVICE_GOT_A_NEW_ORDER";
    public static final String SERVICE_ACTION_NETWORK_OUT = "SERVICE_NETWORK_OUT";
    public static final String SERVICE_ACTION_NEWWORK_RECONNET = "SERVICE_NETWORK_RECONNECT";

    public static final String ACTION_NEW_ORDER = "GOT_A_NEW_ORDER";
    public static final String SERVICE_ACTION_UPDATE_CHARGE = "UPDATE_CHARGE";
    public static final String SERVICE_ACTION_ORDER_NOT_EXISTS = "ORDER_NOT_EXISTS";
    public static final String ACTION_ORDER_CANCEL = "ORDER_CANCELED";

    public static final int MESSAGE_NEW_ORDER = 0x5009;

    public static final int ORDER_PAGE_NUM = 10;
    public static final int MESSAGE_PER_PAGE = 40;

    public static final double LOWSPEEDDISTANACE = 333.3; // m/min
    public static final double STANDDISTANCE = 170; //m/5s
    public static final double STRANGEDISTANCE = 2.3 * 1000.0; // m/s

    public static final String PREFERENCE_KEY_DRIVER_NAME = "DRIVER_NAME";
    public static final String PREFERENCE_KEY_DRIVER_AVATAR = "DRIVER_AVATAR";
    public static final String PREFERENCE_KEY_DRIVER_PLATE = "DRIVER_PLATE";
    public static final String PREFERNECE_KEY_DRIVER_RATING = "DRIVER_RATING";
    public static final String PREFERENCE_KEY_DRIVER_TOTAL_ORDER = "DRIVER_TOTAL_ORDER";
    public static final String PREFERENCE_KEY_DRIVER_BALANCE = "DRIVER_BALANCE";
    public static final String PREFERENCE_KEY_DRIVER_FAVORABLE_RATE = "DRIVER_FAVORABLE_RATE";
    public static final String PREFERENCE_KEY_DRIVER_KM_PRICE = "DRIVER_KM_PRICE";
    public static final String PREFERENCE_KEY_ORDER_STATUS = "ORDER_STATUS";

    public static final String PREFERENCE_KEY_WEBVIEW_ABOUT = "WEB_ABOUT";
    public static final String PREFERENCE_KEY_WEBVIEW_JOIN = "WEB_JOIN";
    public static final String PREFERENCE_KEY_WEBVIEW_CONTACT = "WEB_CONTACT";
    public static final String PREFERENCE_KEY_WEBVIEW_CLAUSE = "WEB_CLAUSE";
    public static final String WEBVIEW_NOTICE = "WEB_NOTICE";
    public static final String PREFERENCE_KEY_WEBVIEW_PERSONAL = "WEB_PERSONAL";
    public static final String PREFERENCE_KEY_WEBVIEW_MONEY = "WEB_MONEY";
    public static final String PREFERENCE_KEY_WEBVIEW_PERFORMANCE = "WEB_PERFORMANCE";
    public static final String PREFERENCE_KEY_WEBVIEW_BILLS = "WEB_BILLS";


    public static final String PREFERENCE_KEY_TODAY = "TODAY";
    public static final String PREFERENCE_KEY_TODAY_DONE_WORK = "TODAY_DONE_WORK";
    public static final String PREFERENCE_KEY_TODAY_ALL_WORK = "TODAY_ALL_WORK";
    public static final String PREFERENCE_KEY_TODAY_CASH = "TODAY_CASH";

    public static final String PACKAGE_NAME = "com.guokrspace.dududriver";

}
