package com.guokrspace.dududriver.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/12/19.
 */
public class WithdrawResponse extends BaseModel implements Serializable {

    /*
     * 1    成功 , -1 非提现日, -2 余额不够, -3 本周已提现一次（一周只能提现一次）
     */
    private Integer status;

    private Double cash;

    private Integer message_id;

    private String cmd;

    public WithdrawResponse() {
        super();
    }

    public WithdrawResponse(Integer status, Double cash, Integer message_id, String cmd) {
        this.status = status;
        this.cash = cash;
        this.message_id = message_id;
        this.cmd = cmd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
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
