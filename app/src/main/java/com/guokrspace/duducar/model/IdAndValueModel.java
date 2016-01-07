package com.guokrspace.duducar.model;

/**
 * Created by hyman on 15/12/12.
 * 这个model用于评论、取消原因、投诉
 */
public class IdAndValueModel extends BaseModel {


    /*
     *原因、评论、投诉id
     */
    public Integer id;

    /*
     * 原因、评论、投诉的描述
     */
    public String value;


    public IdAndValueModel(Integer id, String value) {
        this.id = id;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
