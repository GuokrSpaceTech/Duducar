package com.guokrspace.duducar.communication.message;

/**
 * Created by kai on 10/23/15.
 */
public class DriverInfo {

    int status;
    String cmd;
    int message_id;
    DriverDetail driver;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public DriverDetail getDriver() {
        return driver;
    }

    public void setDriver(DriverDetail driver) {
        this.driver = driver;
    }
}

