package com.guokrspace.duducar.communication.http.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hyman on 15/12/13.
 */
public class BaseInfo extends BaseModel implements Serializable{

    private String cmd;

    private Integer status;

    private Integer message_id;

    private List<IdAndValueModel> comments;

    private List<IdAndValueModel> complaint;

    private List<IdAndValueModel> cancel_order_reason;

    private WebViewUrls webview;

    public BaseInfo() {
        super();
    }

    public BaseInfo(String cmd, Integer status, Integer message_id, List<IdAndValueModel> comments, List<IdAndValueModel> complaint, List<IdAndValueModel> cancel_order_reason, WebViewUrls webview) {
        super();
        this.cmd = cmd;
        this.status = status;
        this.message_id = message_id;
        this.comments = comments;
        this.complaint = complaint;
        this.cancel_order_reason = cancel_order_reason;
        this.webview = webview;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Integer message_id) {
        this.message_id = message_id;
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

    public WebViewUrls getWebview() {
        return webview;
    }

    public void setWebview(WebViewUrls webview) {
        this.webview = webview;
    }
}
