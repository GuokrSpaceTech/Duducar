package com.guokrspace.dududriver.database;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table BASE_NOTICE.
 */
public class BaseNotice {

    private Long id;
    private String date;
    private String type;
    private String messageBody;
    private Boolean outOfTime;
    private Integer noticeId;

    public BaseNotice() {
    }

    public BaseNotice(Long id) {
        this.id = id;
    }

    public BaseNotice(Long id, String date, String type, String messageBody, Boolean outOfTime, Integer noticeId) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.messageBody = messageBody;
        this.outOfTime = outOfTime;
        this.noticeId = noticeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public Boolean getOutOfTime() {
        return outOfTime;
    }

    public void setOutOfTime(Boolean outOfTime) {
        this.outOfTime = outOfTime;
    }

    public Integer getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Integer noticeId) {
        this.noticeId = noticeId;
    }

}
