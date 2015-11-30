package com.guokrspace.dududriver.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/11/14.
 */
public class HistoryOrder extends BaseModel implements Serializable{

    private String id;

    private String orderNum;

    private String driver_id;

    private String passenger_id;

    private String passenger_mobile;

    private String start;

    private String destination;

    private String start_lat;

    private String start_lng;

    private String destination_lat;

    private String destination_lng;

    private String start_time;

    private String end_time;

    private String car_type;

    private String rent_type;//0-包车 1-拼车

    private String additional_price;

    private String mileage;

    private String sumprice;

    private String org_price;

    private String create_time;

    private String pay_time;

    private String pay_role;//支付角色 1-司机 2-乘客

    private String status;//1-订单初始化 2-接单 3-开始 4-结束 5-取消

    private String rating;//0-未评价  ，1-5 评价等级

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getPassenger_id() {
        return passenger_id;
    }

    public void setPassenger_id(String passenger_id) {
        this.passenger_id = passenger_id;
    }

    public String getCar_type() {
        return car_type;
    }

    public void setCar_type(String car_type) {
        this.car_type = car_type;
    }

    public String getRent_type() {
        return rent_type;
    }

    public void setRent_type(String rent_type) {
        this.rent_type = rent_type;
    }

    public String getAdditional_price() {
        return additional_price;
    }

    public void setAdditional_price(String additional_price) {
        this.additional_price = additional_price;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getOrg_price() {
        return org_price;
    }

    public void setOrg_price(String org_price) {
        this.org_price = org_price;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public String getPay_role() {
        return pay_role;
    }

    public void setPay_role(String pay_role) {
        this.pay_role = pay_role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public HistoryOrder(String id, String orderNum, String driver_id, String passenger_id, String passenger_mobile, String start, String destination, String start_lat, String start_lng, String destination_lat, String destination_lng, String start_time, String end_time, String car_type, String rent_type, String additional_price, String mileage, String sumprice, String org_price, String create_time, String pay_time, String pay_role, String status, String rating) {
        this.id = id;
        this.orderNum = orderNum;
        this.driver_id = driver_id;
        this.passenger_id = passenger_id;
        this.passenger_mobile = passenger_mobile;
        this.start = start;
        this.destination = destination;
        this.start_lat = start_lat;
        this.start_lng = start_lng;
        this.destination_lat = destination_lat;
        this.destination_lng = destination_lng;
        this.start_time = start_time;
        this.end_time = end_time;
        this.car_type = car_type;
        this.rent_type = rent_type;
        this.additional_price = additional_price;
        this.mileage = mileage;
        this.sumprice = sumprice;
        this.org_price = org_price;
        this.create_time = create_time;
        this.pay_time = pay_time;
        this.pay_role = pay_role;
        this.status = status;
        this.rating = rating;
    }
}
