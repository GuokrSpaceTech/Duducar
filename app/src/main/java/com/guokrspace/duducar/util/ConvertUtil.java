package com.guokrspace.duducar.util;

import com.alibaba.fastjson.JSON;
import com.guokrspace.duducar.model.Order;
import com.guokrspace.duducar.communication.message.OrderDetail;

/**
 * Created by hyman on 15/12/20.
 */
public class ConvertUtil {

    public static OrderDetail transform2OrderDetail(Order order){
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(order.getId()+"");
        orderDetail.setDriver(JSON.toJSONString(order.getDriver()));
        orderDetail.setStart(order.getStart());
        orderDetail.setStart_lat(order.getStart_lat() + "");
        orderDetail.setStart_lng(order.getStart_lng() + "");
        orderDetail.setDestination(order.getDestination());
        orderDetail.setDestination_lat(order.getDestination_lat() + "");
        orderDetail.setDestination_lng(order.getDestination_lng() + "");
        orderDetail.setPassenger_mobile(order.getId() + "");
        orderDetail.setPassenger_mobile(order.getPassenger_mobile());
        orderDetail.setAdd_price1(order.getAdd_price1());
        orderDetail.setAdd_price2(order.getAdd_price2());
        orderDetail.setAdd_price3(order.getAdd_price3());
        orderDetail.setAddtional_price(order.getAdditional_price());
        orderDetail.setCar_type(order.getCar_type() + "");
        orderDetail.setIsCityline(order.getIsCityline() + "");
        orderDetail.setCityline_id(order.getCityline_id() + "");
        orderDetail.setDriver_id(order.getDriver_id() + "");
        orderDetail.setEnd_time(order.getEnd_time() + "");
        orderDetail.setStart_time(order.getStart_time() + "");
        orderDetail.setIsCancel(order.getIsCancel() + "");
        orderDetail.setLow_speed_time(order.getLow_speed_time());
        orderDetail.setMileage(order.getMileage());
        orderDetail.setOrderNum(order.getOrderNum());
        orderDetail.setOrg_price(order.getOrg_price());
        orderDetail.setPay_role(order.getPay_role() + "");
        orderDetail.setPay_time(order.getPay_time() + "");
        orderDetail.setPay_type(order.getPay_type() + "");
        orderDetail.setRating(order.getRating() + "");
        orderDetail.setRent_type(order.getRent_type() + "");
        orderDetail.setStatus(order.getStatus() + "");
        orderDetail.setSumprice(order.getSumprice());
        return orderDetail;
    }

}
