package com.guokrspace.dududriver.net.message;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by macbook on 15/10/16.
 */
public class MessageTag {

    private static MessageTag s_instance;

    //Pre Order Activity
    public static final int MESSAGE_SUCCESS = 0x7001;
    public static final int MESSAGE_TIMEOUT = 0x7002;
    public static final int MESSAGE_FAILURE = 0x7003;

    public static final int MSG_TOGGLE_CONFIRMVIEW = 0x1001;

    public static final int MESSAGE_GET_NEARBY_CAR_FAILURE = 0x7004;
    public static final int MESSAGE_GET_NEARBY_CAR_SUCCESS = 0x7005;

    public static final int MESSAGE_CREATE_ORDER_SUCCESS = 0x8001;
    public static final int MESSAGE_CREATE_ORDER_FAILURE = 0x8002;
    public static final int MESSAGE_CREATE_ORDER_TIMEOUT = 0x8003;

    //Post Order Activity
    public static final int MESSAGE_ORDER_DISPATCHED = 0x9001;
    public static final int MESSAGE_ORDER_DISPATCH_TIMEOUT = 0x9002;

    public static final int MESSAGE_CAR_ARRIVED = 0x900A;

    public static final int MESSAGE_ORDER_CANCEL_CONFIRMED = 0x9003;
    public static final int MESSAGE_ORDER_CANCEL_TIMEOUT = 0x9004;
    public static final int MESSAGE_ORDER_COMPLETED = 0x900b;

    public static final int MESSAGE_PASSENGER_IN_CAR = 0x9005;

    public static final int MESSAGE_PAYMENT_REQUESTED = 0x9006;
    public static final int MESSAGE_PAYMENT_REQUEST_TIMEOUT = 0x9006;

    public static final int MESSAGE_PAYMENT_CONFIRM_TIMEOUT = 0x9008;
    public static final int MESSAGE_PAYMENT_COMPLETED = 0x9008;

    public static final int REGISTER_REQ = 0x100A;
    public static final int REGISTER_RESP = 0x100B;
    public static final int VERIFY_REQ = 0x100C;
    public static final int VERIFY_RESP = 0x100D;
    public static final int GET_NEAR_CAR_REQ = 0x1001;
    public static final int GET_NEAR_CAR_RESP = 0x1002;
    public static final int CREATE_ORDER_REQ = 0x1003;
    public static final int CREATE_ORDER_RESP = 0x1004;
    public static final int CANCEL_ORDER_REQ = 0x1005;
    public static final int CANCEL_ORDER_RESP = 0x1006;
    public static final int TRIP_START = 0x1007;
    public static final int TRIP_OVER = 0x1008;

    //daddy
    public static final int PATCH_ORDER = 0x1009;
    public static final int ORDER_CANCEL = 0x1012;
    public static final int PAY_OVER = 0x1011;

//    public static final int ORDER_CONFIRM = 0X1010;

    public static MessageTag getInstance(){
        if(s_instance == null) new MessageTag();

        return s_instance;
    }

    public HashMap<String, Integer> MessageTag = new HashMap<>();

    public MessageTag() {
        s_instance = this;
        MessageTag.put("get_near_car", GET_NEAR_CAR_REQ);
        MessageTag.put("get_near_car_resp", GET_NEAR_CAR_RESP);
        MessageTag.put("create_order",CREATE_ORDER_REQ);
        MessageTag.put("create_order_resp",CREATE_ORDER_RESP);
        MessageTag.put("cancel_order",CANCEL_ORDER_REQ);
        MessageTag.put("cancel_order_resp",CANCEL_ORDER_RESP);
        MessageTag.put("trip_start",TRIP_START);
        MessageTag.put("trip_over",TRIP_OVER);
        MessageTag.put("register",REGISTER_REQ);
        MessageTag.put("register_resp",REGISTER_RESP);
        MessageTag.put("verify",VERIFY_REQ);
        MessageTag.put("verify_resp",VERIFY_RESP);

        //daddy
        MessageTag.put("order", PATCH_ORDER);
        MessageTag.put("user_cancel_order", ORDER_CANCEL);
        MessageTag.put("pay_over", PAY_OVER);
    }

    public int Tag(String command)
    {
        return MessageTag.get(command);
    }

    public String Command(int tag)
    {
        String retCommand  = "";
        if( MessageTag.containsValue(tag) )
        {
            for(Map.Entry<String, Integer> entry : MessageTag.entrySet())
            {
                if(entry.getValue().equals(tag))
                    return entry.getKey();
            }
        }

        return retCommand;
    }
}
