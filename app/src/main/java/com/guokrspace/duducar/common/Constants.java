package com.guokrspace.duducar.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daddyfang on 15/11/19.
 */
public class Constants {

    public static final String SERVICE_ACTION_RELOGIN = "SERVICE_ACTION_RELOGIN";
    public static final String ACTION_REFRESH_DRAWERVIEW = "action.refreshDrawerview";
    public static final String SERVICE_BROADCAST = "SERVICE_BROADCAST";
    public static final String UPDATE_MAP_CENTER = "UPDATE_MAP_CENTER";
    public static final String SERVICE_ACTION_MESAGE = "GOT_A_NEW_MESSAGE";
    public static final String PASSENGER_ROLE = "2";
    public static final int ORDER_PAGE_NUM = 10;
    public static final int MESSAGE_PER_PAGE = 40;

    public static final String citys = "长沙市常德市汨罗市长沙县望城县";

    public static final int ORDER_STATUS_INITATION = 1;//创建订单
    public static final int ORDER_STATUS_ACCEPT = 2;//已接单
    public static final int ORDER_STATUS_START = 3;//乘客上车
    public static final int ORDER_STATUS_END = 4;//行程结束
    public static final int ORDER_STATUS_PAID = 5;//已支付
    public static final int ORDER_STATUS_CANCEL = -1;//用户取消


    private static Map<Integer, String> certifyStatusMap = new HashMap<>(4);

    static {
        // 认证状态
        certifyStatusMap.put(0, "未认证");
        certifyStatusMap.put(1, "认证中");
        certifyStatusMap.put(2, "认证失败，请重新认证");
        certifyStatusMap.put(3, "已认证");
    }

    /**
     * 更具状态码获取对应状态的描述
     * @param key
     * @return
     */
    public static String getCertifyStatusDes(int key) {
        if (certifyStatusMap.keySet().contains(key)) {
            return  certifyStatusMap.get(key);
        }
        return "";
    }




}
