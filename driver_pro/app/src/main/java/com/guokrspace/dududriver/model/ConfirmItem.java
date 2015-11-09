package com.guokrspace.dududriver.model;

/**
 * Created by daddyfang on 15/11/3.
 */
public class ConfirmItem {

    public String getCMD() {
        return CMD;
    }

    public void setCMD(String CMD) {
        this.CMD = CMD;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String CMD;
    private String order_no;
    private String status;

}
