package com.guokrspace.dududriver.model;

/**
 * Created by hyman on 15/10/24.
 */
public class OrderListItem extends BaseModel {

    public String date;

    public String description;

    public OrderListItem(String date, String description) {
        this.date = date;
        this.description = description;
    }
}
