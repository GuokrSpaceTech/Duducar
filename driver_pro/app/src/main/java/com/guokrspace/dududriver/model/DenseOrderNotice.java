package com.guokrspace.dududriver.model;

/**
 * Created by hyman on 15/11/13.
 */
public class DenseOrderNotice implements BaseNoticeItem {

    public String date;

    public String description;

    public DenseOrderNotice(String date, String description) {
        this.date = date;
        this.description = description;
    }
}
