package com.guokrspace.duducar.communication.http.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/12/14.
 */
public class Driver implements Serializable {

    private Long id;
    private String name;
    private String mobile;
    private String avatar;
    private String plate;
    private String picture;
    private String description;

    public Driver() {
        super();
    }

    public Driver(Long id, String name, String mobile, String avatar, String plate, String picture, String description) {
        super();
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.avatar = avatar;
        this.plate = plate;
        this.picture = picture;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
