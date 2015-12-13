package com.guokrspace.duducar.communication.message;

import java.io.Serializable;

//\"id\":1139,\"orderNum\":\"2015121219231450485797\",
// \"driver_id\":5,
// \"passenger_id\":4,\"passenger_mobile\":\"13700000003\"
// ,\"start\":\"\\u6e56\\u5357\\u7701\\u957f\\u6c99\\u5e02\\u5cb3\\u9e93\\u533a\\u9e93\\u5c71\\u5357\\u8def\",\"d
// estination\":\"\\u65b0\\u5409\\u7965\\u7f51\\u554a\",\
// "start_lat\":28.185328,\"start_lng\":112.949661,\"destination_lat\":28.185328,\"destination_lng\":112.949661,\
// "start_time\":1449913836,
// \"end_time\":1449919394,\
// "pre_mileage\":\"0.00\",\"pre_price\":\"0.00\",\
// "car_type\":1,\"rent_type\":0,\"additional_price\":\"0.00\",
// \"org_price\":\"0.11\",\"add_price1\":\"0\",\"add_price2\":\"0\",\"add_price3\"
// :\"0\",\"isCancel\":0,\"mileage\":\"0.00\",\"sumprice\":\"0.11\",\"low_speed_time\":\"10\",
// \"create_time\":1449913797,
// \"isCityline\":0,\"cityline_id\":0,\
// "pay_time\":0,\"pay_type\":0,\"pay_role\":1,\"status\":4,
// \"rating\":0,\
// "driver\":\"{\\\"name\\\":\\\"\\\\u
// 738b\\\\u5e08\\\\u5085\\\",\\\"mobile\\\":\\\"
// 13900000005\\\",\\\"avatar\\\":\\\"http:\\\\\\\/\\\\\\\/120.24.237.15:83\\\\\\\/Uploads\\\\\\\/Pict
// ure\\\\\\\/2015-11-10\\\\\\\/444444444444444440.j
// pg\\\",\\\"plate\\\":\\\"\\\\u6e58A3366A\\\",\\\"pic
// ture\\\":\\\"http:\\\\\\\/\\\\\\\/120.24.237.15:
// 83\\\\\\\/Uploads\\\\\\\/Picture\\\\\\\/2015-11-10\\\\\\\/1496144_229305.jpg\\\",\
// \\"description\\\":\\\" \\\\u94f6\\\\u7070\\\\u8272\\\\u6717\\\\u9038\\\"}\"}",

public class OrderDetail implements Serializable{
    public String id;
    public String orderNum;
    public String driver_id;
    public String passenger_id;
    public String passenger_mobile;
    public String start;
    public String destination;
    public String start_lat;
    public String start_lng;
    public String destination_lat;
    public String destination_lng;
    public String start_time;
    public String end_time;
    public String pre_mileage;
    public String pre_price;
    public String car_type;
    public String rent_type;
    public String addtional_price;
    public String org_price;
    public String add_price1;
    public String add_price2;
    public String add_price3;
    public String isCancel;
    public String mileage;
    public String sumprice;
    public String low_speed_time;
    public String create_time;
    public String isCityline;
    public String cityline_id;
    public String pay_time;
    public String pay_type;
    public String pay_role;

    public String status;
    public String rating;
    public String driver;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
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

    public String getLow_speed_time() {
        return low_speed_time;
    }

    public void setLow_speed_time(String low_speed_time) {
        this.low_speed_time = low_speed_time;
    }

    public String getPay_role() {
        return pay_role;
    }

    public void setPay_role(String pay_role) {
        this.pay_role = pay_role;
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

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
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

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getIsCityline() {
        return isCityline;
    }

    public void setIsCityline(String isCityline) {
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
}
