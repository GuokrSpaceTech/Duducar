package com.guokrspace.duducar.communication.message;

import com.guokrspace.duducar.PreOrderActivity;

/**
 * Created by kai on 10/29/15.
 */
public class TripOver {
    String cmd;
    OrderDetail order;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public OrderDetail getOrder() {
        return order;
    }

    public void setOrder(OrderDetail order) {
        this.order = order;
    }
}

