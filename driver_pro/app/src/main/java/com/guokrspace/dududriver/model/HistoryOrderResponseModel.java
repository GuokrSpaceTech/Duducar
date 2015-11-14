package com.guokrspace.dududriver.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hyman on 15/11/14.
 */
public class HistoryOrderResponseModel implements Serializable{

    private String cmd;

    private String message_id;

    private String status;

    private List<HistoryOrder> order_list;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<HistoryOrder> getOrder_list() {
        return order_list;
    }

    public void setOrder_list(List<HistoryOrder> order_list) {
        this.order_list = order_list;
    }
}
