package com.guokrspace.duducar.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/12/14.
 */
public class Order extends BaseModel implements Serializable {

    private Long id;
    /** Not-null value. */
    private String orderNum;
    private String passenger_mobile;
    private String start;
    private String destination;
    private Double start_lat;
    private Double start_lng;
    private Double destination_lat;
    private Double destination_lng;
    private Long start_time;
    private Long end_time;
    private String mileage;
    private String sumprice;
    private Integer car_type;
    private Integer rent_type;
    private String additional_price;
    private String org_price;
    private String add_price1;
    private String add_price2;
    private String add_price3;
    private Integer isCancel;
    private String low_speed_time;
    private Integer isCityline;
    private Integer cityline_id;
    private Long pay_time;
    private Integer pay_type;
    private Integer pay_role;
    private Integer status;
    private Integer rating;
    private String company;
    private Integer passenger_id;
    private long driver_id;
    private Driver driver;

    public Order() {
        super();
    }

    public Order(Long id, String orderNum, String passenger_mobile, String start, String destination, Double start_lat, Double start_lng, Double destination_lat, Double destination_lng, Long start_time, Long end_time, String mileage, String sumprice, Integer car_type, Integer rent_type, String additional_price, String org_price, String add_price1, String add_price2, String add_price3, Integer isCancel, String low_speed_time, Integer isCityline, Integer cityline_id, Long pay_time, Integer pay_type, Integer pay_role, Integer status, Integer rating, String company, Integer passenger_id, long driver_id, Driver driver) {
        super();
        this.id = id;
        this.orderNum = orderNum;
        this.passenger_mobile = passenger_mobile;
        this.start = start;
        this.destination = destination;
        this.start_lat = start_lat;
        this.start_lng = start_lng;
        this.destination_lat = destination_lat;
        this.destination_lng = destination_lng;
        this.start_time = start_time;
        this.end_time = end_time;
        this.mileage = mileage;
        this.sumprice = sumprice;
        this.car_type = car_type;
        this.rent_type = rent_type;
        this.additional_price = additional_price;
        this.org_price = org_price;
        this.add_price1 = add_price1;
        this.add_price2 = add_price2;
        this.add_price3 = add_price3;
        this.isCancel = isCancel;
        this.low_speed_time = low_speed_time;
        this.isCityline = isCityline;
        this.cityline_id = cityline_id;
        this.pay_time = pay_time;
        this.pay_type = pay_type;
        this.pay_role = pay_role;
        this.status = status;
        this.rating = rating;
        this.company = company;
        this.passenger_id = passenger_id;
        this.driver_id = driver_id;
        this.driver = driver;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
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

    public Integer getCar_type() {
        return car_type;
    }

    public void setCar_type(Integer car_type) {
        this.car_type = car_type;
    }

    public Integer getRent_type() {
        return rent_type;
    }

    public void setRent_type(Integer rent_type) {
        this.rent_type = rent_type;
    }

    public String getAdditional_price() {
        return additional_price;
    }

    public void setAdditional_price(String additional_price) {
        this.additional_price = additional_price;
    }

    public String getOrg_price() {
        return org_price;
    }

    public void setOrg_price(String org_price) {
        this.org_price = org_price;
    }

    public String getAdd_price1() {
        return add_price1;
    }

    public void setAdd_price1(String add_price1) {
        this.add_price1 = add_price1;
    }

    public String getAdd_price2() {
        return add_price2;
    }

    public void setAdd_price2(String add_price2) {
        this.add_price2 = add_price2;
    }

    public String getAdd_price3() {
        return add_price3;
    }

    public void setAdd_price3(String add_price3) {
        this.add_price3 = add_price3;
    }

    public Integer getIsCancel() {
        return isCancel;
    }

    public void setIsCancel(Integer isCancel) {
        this.isCancel = isCancel;
    }

    public String getLow_speed_time() {
        return low_speed_time;
    }

    public void setLow_speed_time(String low_speed_time) {
        this.low_speed_time = low_speed_time;
    }

    public Integer getIsCityline() {
        return isCityline;
    }

    public void setIsCityline(Integer isCityline) {
        this.isCityline = isCityline;
    }

    public Integer getCityline_id() {
        return cityline_id;
    }

    public void setCityline_id(Integer cityline_id) {
        this.cityline_id = cityline_id;
    }

    public Long getPay_time() {
        return pay_time;
    }

    public void setPay_time(Long pay_time) {
        this.pay_time = pay_time;
    }

    public Integer getPay_type() {
        return pay_type;
    }

    public void setPay_type(Integer pay_type) {
        this.pay_type = pay_type;
    }

    public Integer getPay_role() {
        return pay_role;
    }

    public void setPay_role(Integer pay_role) {
        this.pay_role = pay_role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Integer getPassenger_id() {
        return passenger_id;
    }

    public void setPassenger_id(Integer passenger_id) {
        this.passenger_id = passenger_id;
    }

    public long getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(long driver_id) {
        this.driver_id = driver_id;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}
