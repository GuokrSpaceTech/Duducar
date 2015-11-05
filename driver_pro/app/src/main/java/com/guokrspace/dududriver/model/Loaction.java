package com.guokrspace.dududriver.model;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by daddyfang on 15/11/2.
 */
public class Loaction {

    Double lat;
    Double lng;
    String address;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocation()
    {
        return new LatLng(lat, lng);
    }

}
