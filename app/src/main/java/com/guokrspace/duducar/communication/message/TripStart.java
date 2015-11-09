package com.guokrspace.duducar.communication.message;

/**
 * Created by kai on 10/29/15.
 */
public class TripStart {
    String cmd;
    TripStartOrder order;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public TripStartOrder getOrder() {
        return order;
    }

    public void setOrder(TripStartOrder order) {
        this.order = order;
    }
}

