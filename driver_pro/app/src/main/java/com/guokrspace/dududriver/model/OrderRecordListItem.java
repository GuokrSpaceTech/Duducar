package com.guokrspace.dududriver.model;

/**
 * Created by hyman on 15/10/31.
 */
public class OrderRecordListItem extends BaseModel {

    public String id;

    public String date;

    public String origin;

    public String destination;

    public String status;

    public OrderRecordListItem(String id, String date, String origin, String destination, String status) {
        this.id = id;
        this.date = date;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
    }
}
