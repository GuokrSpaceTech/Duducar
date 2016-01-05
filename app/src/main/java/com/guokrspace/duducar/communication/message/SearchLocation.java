package com.guokrspace.duducar.communication.message;

import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

/**
 * Created by kai on 10/23/15.
 */
public class SearchLocation implements Serializable {

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
        if(lat!=null && lng !=null)
           return new LatLng(lat, lng);
        else
            return null;
    }
}
