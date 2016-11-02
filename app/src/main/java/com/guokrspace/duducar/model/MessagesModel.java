package com.guokrspace.duducar.model;

import com.guokrspace.duducar.database.MessageInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hyman on 16/8/16.
 */
public class MessagesModel implements Serializable {

    private String cmd;
    private String status;

    private List<MessageInfo> message_list;

    public MessagesModel() {
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

    public List<MessageInfo> getMessage_list() {
        return message_list;
    }

    public void setMessage_list(List<MessageInfo> message_list) {
        this.message_list = message_list;
    }
}
