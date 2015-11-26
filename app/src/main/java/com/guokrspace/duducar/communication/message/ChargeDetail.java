package com.guokrspace.duducar.communication.message;

/**
 * Created by daddyfang on 15/11/25.
 */
public class ChargeDetail {

    String cmd;
    String current_mile;
    String current_charge;
    String low_speed_time;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCurrent_mile() {
        return current_mile;
    }

    public void setCurrent_mile(String current_mile) {
        this.current_mile = current_mile;
    }

    public String getCurrent_charge() {
        return current_charge;
    }

    public void setCurrent_charge(String current_charge) {
        this.current_charge = current_charge;
    }

    public String getLow_speed_time() {
        return low_speed_time;
    }

    public void setLow_speed_time(String low_speed_time) {
        this.low_speed_time = low_speed_time;
    }
}
