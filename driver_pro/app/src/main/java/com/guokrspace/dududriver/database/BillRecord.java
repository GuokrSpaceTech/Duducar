package com.guokrspace.dududriver.database;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table BILL_RECORD.
 */
public class BillRecord {

    private Long id;
    private String money;
    private String description;
    private String time;
    private Integer type;
    private String opposite;

    public BillRecord() {
    }

    public BillRecord(Long id) {
        this.id = id;
    }

    public BillRecord(Long id, String money, String description, String time, Integer type, String opposite) {
        this.id = id;
        this.money = money;
        this.description = description;
        this.time = time;
        this.type = type;
        this.opposite = opposite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getOpposite() {
        return opposite;
    }

    public void setOpposite(String opposite) {
        this.opposite = opposite;
    }

}
