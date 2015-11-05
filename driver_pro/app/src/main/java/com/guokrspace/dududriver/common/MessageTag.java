package com.guokrspace.dududriver.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by macbook on 15/10/16.
 */
public class MessageTag {

    private static com.guokrspace.dududriver.common.MessageTag s_instance;

//    public static final int

    //Pre Order Activity
    public static final int MESSAGE_SUCCESS = 0x7001;
    public static final int MESSAGE_TIMEOUT = 0x7002;
    public static final int MESSAGE_FAILURE = 0x7003;

    public static final int MESSAGE_GET_NEARBY_CAR_FAILURE = 0x7004;
    public static final int MESSAGE_GET_NEARBY_CAR_SUCCESS = 0x7005;

    public static final int MESSAGE_CREATE_ORDER_SUCCESS = 0x8001;
    public static final int MESSAGE_CREATE_ORDER_FAILURE = 0x8002;
    public static final int MESSAGE_CREATE_ORDER_TIMEOUT = 0x8003;

    //Post Order Activity
    public static final int MESSAGE_ORDER_DISPATCHED = 0x9001;
    public static final int MESSAGE_ORDER_DISPATCH_TIMEOUT = 0x9002;

    public static final int MESSAGE_ORDER_CANCEL_CONFIRMED = 0x9003;
    public static final int MESSAGE_ORDER_CANCEL_TIMEOUT = 0x9004;

    public static final int MESSAGE_PASSENGER_IN_CAR = 0x9005;

    public static final int MESSAGE_PAYMENT_REQUESTED = 0x9006;
    public static final int MESSAGE_PAYMENT_REQUEST_TIMEOUT = 0x9006;

    public static final int MESSAGE_PAYMENT_CONFIRM_TIMEOUT = 0x9008;
    public static final int MESSAGE_PAYMENT_COMPLETED = 0x9008;

    public static final int GET_NEAR_CAR_REQ = 0x1001;
    public static final int GET_NEAR_CAR_RESP = 0x1002;

    public static MessageTag getInstance(){
        if(s_instance == null) new MessageTag();

        return s_instance;
    }

    public HashMap<String, Integer> MessageTag = new HashMap<>();

    public MessageTag() {
        s_instance = this;
        MessageTag.put("get_near_car", GET_NEAR_CAR_REQ);
        MessageTag.put("get_near_car_resp", GET_NEAR_CAR_RESP);
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
