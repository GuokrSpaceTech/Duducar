package com.guokrspace.dududriver.model;

/**
 * Created by hyman on 15/10/31.
 */
public class RecordListItem extends BaseModel {

    public String date;

    public String origin;

    public String destination;

    public String status;

    public RecordListItem(String date, String origin, String destination, String status) {
        this.date = date;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
    }
}
