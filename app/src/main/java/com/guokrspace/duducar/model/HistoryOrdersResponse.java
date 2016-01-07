package com.guokrspace.duducar.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hyman on 15/12/14.
 */
 public class HistoryOrdersResponse extends BaseModel implements Serializable{

    private String cmd;

    private int status;

    private List<Order> order_list;

    public HistoryOrdersResponse() {
        super();
    }

    public HistoryOrdersResponse(String cmd, int status, List<Order> order_list) {
        super();
        this.cmd = cmd;
        this.status = status;
        this.order_list = order_list;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Order> getOrder_list() {
        return order_list;
    }

    public void setOrder_list(List<Order> order_list) {
        this.order_list = order_list;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
