package com.guokrspace.duducar.communication.http.model;

import java.util.List;

/**
 * Created by hyman on 15/12/13.
 */
public class BaseInfo extends BaseModel {

    private String cmd;

    private String status;

    private List<IdAndValueModel> comments;

    private List<IdAndValueModel> complaint;

    private List<IdAndValueModel> cancel_order_reason;

    public BaseInfo(String cmd, String status, List<IdAndValueModel> comments, List<IdAndValueModel> complaint, List<IdAndValueModel> cancel_order_reason) {
        this.cmd = cmd;
        this.status = status;
        this.comments = comments;
        this.complaint = complaint;
        this.cancel_order_reason = cancel_order_reason;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<IdAndValueModel> getComments() {
        return comments;
    }

    public void setComments(List<IdAndValueModel> comments) {
        this.comments = comments;
    }

    public List<IdAndValueModel> getComplaint() {
        return complaint;
    }

    public void setComplaint(List<IdAndValueModel> complaint) {
        this.complaint = complaint;
    }

    public List<IdAndValueModel> getCancel_order_reason() {
        return cancel_order_reason;
    }

    public void setCancel_order_reason(List<IdAndValueModel> cancel_order_reason) {
        this.cancel_order_reason = cancel_order_reason;
    }
}
