package com.guokrspace.dududriver.net.message;

/**
 * Created by daddyfang on 15/11/24.
 */

import java.io.Serializable;

//{"id":29,"orderNum":"12345678889","driver_id":3,"passenger_id":3,"passenger_mobile":"13900000002",
//        "start":"\u6e56\u5357\u7701\u957f\u6c99\u5e02\u5cb3\u9e93\u533a\u767b\u9ad8\u8def4-2",
//        "destination":"\u83ab\u4fea\u82b1\u56ed","start_lat":28.185693,"start_lng":112.949612,
//        "destination_lat":28.185693,"destination_lng":112.949612,"start_time":1447162569,
//        "end_time":1447162579,"pre_mileage":"0.00","pre_price":"0.00","car_type":1,"rent_type":0,"additional_price":null,
//        "org_price":"0.01","isCancel":null,
//        "mileage":"0.0","sumprice":null,"create_time":1447162560,"isCityline":0,"cityline_id":null,"pay_time":null,"pay_type":null,"status":4,"rating":0}}
public class OrderDetail implements Serializable {
    int id;
    String orderNum;
    int driver_id;
    int passenger_id;
    String passenger_mobile;
    String start;
    String destination;
    Double start_lat;
    Double start_lng;
    Double destination_lat;
    Double destination_lng;
    Long start_time;
    Long end_time;
    String pre_mileage;
    String pre_price;
    int car_type;
    int rent_type;
    String addtional_price;
    String org_price;
    String isCancel;
    String mileage;
    String sumprice;
    Long create_time;
    int isCityline;
    String cityline_id;
    String pay_time;
    String pay_type;
    int status;
    int rating;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public int getPassenger_id() {
        return passenger_id;
    }

    public void setPassenger_id(int passenger_id) {
        this.passenger_id = passenger_id;
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

    public Double getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(Double start_lat) {
        this.start_lat = start_lat;
    }

    public Double getStart_lng() {
        return start_lng;
    }

    public void setStart_lng(Double start_lng) {
        this.start_lng = start_lng;
    }

    public Double getDestination_lat() {
        return destination_lat;
    }

    public void setDestination_lat(Double destination_lat) {
        this.destination_lat = destination_lat;
    }

    public Double getDestination_lng() {
        return destination_lng;
    }

    public void setDestination_lng(Double destination_lng) {
        this.destination_lng = destination_lng;
    }

    public Long getStart_time() {
        return start_time;
    }

    public void setStart_time(Long start_time) {
        this.start_time = start_time;
    }

    public Long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Long end_time) {
        this.end_time = end_time;
    }

    public String getPre_mileage() {
        return pre_mileage;
    }

    public void setPre_mileage(String pre_mileage) {
        this.pre_mileage = pre_mileage;
    }

    public String getPre_price() {
        return pre_price;
    }

    public void setPre_price(String pre_price) {
        this.pre_price = pre_price;
    }

    public int getCar_type() {
        return car_type;
    }

    public void setCar_type(int car_type) {
        this.car_type = car_type;
    }

    public int getRent_type() {
        return rent_type;
    }

    public void setRent_type(int rent_type) {
        this.rent_type = rent_type;
    }

    public String getAddtional_price() {
        return addtional_price;
    }

    public void setAddtional_price(String addtional_price) {
        this.addtional_price = addtional_price;
    }

    public String getOrg_price() {
        return org_price;
    }

    public void setOrg_price(String org_price) {
        this.org_price = org_price;
    }

    public String getIsCancel() {
        return isCancel;
    }

    public void setIsCancel(String isCancel) {
        this.isCancel = isCancel;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getSumprice() {
        return sumprice;
    }

    public void setSumprice(String sumprice) {
        this.sumprice = sumprice;
    }

    public Long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Long create_time) {
        this.create_time = create_time;
    }

    public int getIsCityline() {
        return isCityline;
    }

    public void setIsCityline(int isCityline) {
        this.isCityline = isCityline;
    }

    public String getCityline_id() {
        return cityline_id;
    }

    public void setCityline_id(String cityline_id) {
        this.cityline_id = cityline_id;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
