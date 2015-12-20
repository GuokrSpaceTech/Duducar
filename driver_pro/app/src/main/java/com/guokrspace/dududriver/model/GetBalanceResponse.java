package com.guokrspace.dududriver.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/12/19.
 */
public class GetBalanceResponse extends BaseModel implements Serializable {

    private Integer status;

    /*
     * 可用余额
     */
    private String available;

    private Integer message_id;

    private String cmd;

    public GetBalanceResponse() {
        super();
    }

    public GetBalanceResponse(Integer status, String available, Integer message_id, String cmd) {
        super();
        this.status = status;
        this.available = available;
        this.message_id = message_id;
        this.cmd = cmd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public Integer getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Integer message_id) {
        this.message_id = message_id;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
