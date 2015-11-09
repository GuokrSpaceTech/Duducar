package com.guokrspace.duducar.communication.message;

/**
 * Created by kai on 10/29/15.
 */
public class TripOver {
    String cmd;
    TripOverOrder tripOverOrder;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public TripOverOrder getTripOverOrder() {
        return tripOverOrder;
    }

    public void setTripOverOrder(TripOverOrder tripOverOrder) {
        this.tripOverOrder = tripOverOrder;
    }
}

