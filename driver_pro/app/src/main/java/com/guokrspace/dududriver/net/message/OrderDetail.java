package com.guokrspace.dududriver.net.message;

/**
 * Created by daddyfang on 15/11/24.
 */

import java.io.Serializable;

/*
`orderNum` char(22) NOT NULL,
        `driver_id` int(11) NOT NULL COMMENT '司机ID',
        `passenger_id` int(10) unsigned NOT NULL COMMENT '乘客ID',
        `passenger_mobile` char(11) DEFAULT NULL,
        `start` varchar(255) NOT NULL COMMENT '出发地址',
        `destination` varchar(255) NOT NULL COMMENT '目的地址',
        `start_lat` double NOT NULL COMMENT '出发纬度',
        `start_lng` double NOT NULL COMMENT '出发经度',
        `destination_lat` double NOT NULL COMMENT '目的纬度',
        `destination_lng` double NOT NULL COMMENT '目的经度',
        `start_time` int(11) DEFAULT NULL COMMENT '开始时间',
        `end_time` int(11) DEFAULT NULL COMMENT '结束时间',
        `pre_mileage` decimal(9,2) DEFAULT NULL COMMENT '预估里程',
        `pre_price` decimal(9,2) DEFAULT NULL COMMENT '预估价格',
        `car_type` tinyint(3) DEFAULT NULL COMMENT '车型',
        `rent_type` tinyint(3) DEFAULT '0' COMMENT '0-包车 1-拼车',
        `additional_price` decimal(9,2) DEFAULT NULL COMMENT '低速加价（总价）',
        `org_price` decimal(9,2) DEFAULT NULL COMMENT '原始价格（总）',
        `add_price1` decimal(10,0) DEFAULT '0' COMMENT '附加费1',
        `add_price2` decimal(10,0) DEFAULT '0' COMMENT '附加费2',
        `add_price3` decimal(10,0) DEFAULT '0' COMMENT '附加费3',
        `isCancel` tinyint(2) DEFAULT NULL COMMENT '是否取消',
        `mileage` decimal(9,2) DEFAULT NULL COMMENT '里程',
        `sumprice` decimal(9,2) DEFAULT NULL COMMENT '总价（应付价格）',
        `low_speed_time` decimal(3,0) DEFAULT '0' COMMENT '低速行驶时间（分）',
        `create_time` int(11) DEFAULT NULL COMMENT '订单创建时间',
        `isCityline` tinyint(2) DEFAULT '0' COMMENT '是否城际线路 0-否 1-是',
        `cityline_id` tinyint(2) DEFAULT NULL COMMENT '城际线路ID',
        `pay_time` int(11) DEFAULT NULL COMMENT '支付时间',
        `pay_type` tinyint(4) DEFAULT NULL COMMENT '支付类型：1-支付宝 ，2-微信 ，3-银联',
        `pay_role` tinyint(4) DEFAULT '2' COMMENT '支付角色 1-司机 2-乘客',
        `status` tinyint(11) DEFAULT NULL COMMENT '1-订单初始化 2-接单 3-开始 4-结束 5-取消',
        `rating` tinyint(4) DEFAULT '0' COMMENT '0-未评价  ，1-5 评价等级',

*/
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
    String add_price1;
    String add_price2;
    String add_price3;
    String isCancel;
    String mileage;
    String sumprice;
    String low_speed_time;
    Long create_time;
    int isCityline;
    String cityline_id;
    String pay_time;
    String pay_type;
    String pay_role;

    int status;
    int rating;


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
