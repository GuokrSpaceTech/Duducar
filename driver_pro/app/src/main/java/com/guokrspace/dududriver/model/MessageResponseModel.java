package com.guokrspace.dududriver.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by daddyfang on 15/11/27.
 */
public class MessageResponseModel implements Serializable {

    private String cmd;

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

    public List<DuduMessage> getMessage_list() {
        return message_list;
    }

    public void setDuduMessage_list(List<DuduMessage> message_list) {
        this.message_list = message_list;
    }

    private String status;

    private List<DuduMessage> message_list;
}
