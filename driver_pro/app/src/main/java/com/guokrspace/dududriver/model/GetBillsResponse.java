package com.guokrspace.dududriver.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hyman on 15/12/18.
 */
public class GetBillsResponse extends BaseModel implements Serializable {

    private Integer status;

    private String balance;

    private List<BillItem> bill_list;

    private Integer message_id;

    private String cmd;

    public GetBillsResponse() {
        super();
    }

    public GetBillsResponse(Integer status, String balance, List<BillItem> bill_list, Integer message_id, String cmd) {
        super();
        this.status = status;
        this.balance = balance;
        this.bill_list = bill_list;
        this.message_id = message_id;
        this.cmd = cmd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public List<BillItem> getBill_list() {
        return bill_list;
    }

    public void setBill_list(List<BillItem> bill_list) {
        this.bill_list = bill_list;
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
