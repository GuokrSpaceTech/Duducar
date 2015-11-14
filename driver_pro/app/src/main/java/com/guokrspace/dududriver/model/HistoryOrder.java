package com.guokrspace.dududriver.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/11/14.
 */
public class HistoryOrder implements Serializable{

    private String id;

    private String passenger_mobile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String start;

    private String destination;

    private String start_lat;

    private String start_lng;

    private String destination_lat;

    private String destination_lng;

    private String sumprice;

    private String start_time;

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getPassenger_mobile() {
        return passenger_mobile;
    }

    public void setPassenger_mobile(String passenger_mobile) {
        this.passenger_mobile = passenger_mobile;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(String start_lat) {
        this.start_lat = start_lat;
    }

    public String getStart_lng() {
        return start_lng;
    }

    public void setStart_lng(String start_lng) {
        this.start_lng = start_lng;
    }

    public String getDestination_lat() {
        return destination_lat;
    }

    public void setDestination_lat(String destination_lat) {
        this.destination_lat = destination_lat;
    }

    public String getDestination_lng() {
        return destination_lng;
    }

    public void setDestination_lng(String destination_lng) {
        this.destination_lng = destination_lng;
    }

    public String getSumprice() {
        return sumprice;
    }

    public void setSumprice(String sumprice) {
        this.sumprice = sumprice;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    private String end_time;

    public HistoryOrder(String id, String passenger_mobile, String start, String destination, String start_lat, String start_lng, String destination_lat, String destination_lng, String sumprice, String start_time, String end_time) {
        this.id = id;
        this.passenger_mobile = passenger_mobile;
        this.start = start;
        this.destination = destination;
        this.start_lat = start_lat;
        this.start_lng = start_lng;
        this.destination_lat = destination_lat;
        this.destination_lng = destination_lng;
        this.sumprice = sumprice;
        this.start_time = start_time;
        this.end_time = end_time;
    }
}
