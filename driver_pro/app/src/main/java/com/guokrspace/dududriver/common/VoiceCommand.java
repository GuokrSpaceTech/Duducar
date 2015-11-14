package com.guokrspace.dududriver.common;

/**
 * Created by daddyfang on 15/11/12.
 */
public class VoiceCommand {

    public static final String CONNECT_SERVER = "正在连接服务器..";
    public static final String WAIT_FOR_ORDER = "开始听单";
    public static final String HOLD_CAR = "收车";
    public static final String CONTINUE_WAIT = "继续听单";
    public static final String FINISH_LISTENERING = "结束听单";
    public static final String NEW_ORDER_ARRIVE = "收到新的订单";
    public static final String ORDER_AUTO_CANCEL= "超时, 自动放弃订单,等待重新派单";
    public static final String ORDER_ACCEPT = "接单成功,请前往乘客上车地点";
    public static final String ORDER_FAILER = "接单失败,等待重新派单";
    public static final String ORDER_REJECT = "订单已拒绝, 等待重新派单";
    public static final String TIME_OUT_ALERT = "连接超时,请检查网络状况";
    public static final String EXCEPTION = "订单出现异常,无法继续进行,等待重新派单";
    public static final String PICKUP_DONE = "乘客已上车,正在规划线路并开始计费";
    public static final String ORDER_CANCEL = "乘客取消订单, 等待重新派单";
    public static final String CALL_PASSENEGER = "正在联系乘客";
    public static final String ORDER_FINISHED = "订单结束,停止计费,请确认账单";
    public static final String WAIT_FOR_PAY = "账单已发送,等待乘客支付或您代付";
    public static final String PAY_OVER = "收款成功, 请选择继续听单或收车";
    public static final String DRIVER_PAY = "请您尽快完成代付";
}
