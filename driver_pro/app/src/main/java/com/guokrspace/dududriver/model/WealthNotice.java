package com.guokrspace.dududriver.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hyman on 15/11/13.
 */
public class WealthNotice implements BaseNoticeItem {

    public int order_id;
    public String orderNum;
    public int driver_id;
    public int passenger_id;
    public String passenger_mobile;
    public double start_lng;
    public double start_lat;
    public double destination_lat;
    public double destination_lng;
    public long start_time;
    public long end_time;
    public String pre_mileage;
    public String pre_price;
    public int car_type;
    public int rent_type;
    public String additional_price;
    public String org_price;
    public String add_price1;
    public String add_price2;
    public String add_price3;
    public int isCancel;
    public String mileage;
    public String start;
    public String destination;
    public String sumprice;
    public long create_time;
    public int isCityline;
    public int cityline_id;
    public long pay_time;
    public int pay_type;
    public int pay_role;
    public int status;
    public int rating;
    public String low_speed_time;

    public WealthNotice(String message){
        try{
            JSONObject object = new JSONObject(message);
            Log.e("daddy message", message);
            order_id =  object.get("id") == null ? 0 : (Integer) object.get("id");
            orderNum = object.get("orderNum") == null ? "0" : (String) object.get("orderNum");
            org_price = object.get("org_price") == null? "0.01" : (String) object.get("org_price");
            start = object.get("start") == null ? "出发地" : (String) object.get("start") ;
            destination = object.get("destination") == null ? "目的地" : (String) object.get("destination");
            passenger_mobile = object.get("passenger_mobile") == null ? "13900000002" : (String) object.get("passenger_mobile");
            sumprice = object.get("sumprice") == null ? "0.01" :  object.get("sumprice").toString();
            start_time = (Integer) object.get("start_time");
            end_time = (Integer) object.get("end_time");

            if(object.get("pay_time") instanceof Integer){
                pay_time = object.get("pay_time") == null ? System.currentTimeMillis() : (Integer)object.get("pay_time");
            } else if(object.get("pay_time") instanceof String){
                pay_time = Long.parseLong((String) object.get("pay_time"));
            }
            if(JSONObject.NULL.equals(object.get("pay_role"))){
                pay_role = 2;
            } else {
                pay_role = object.get("pay_role") == null ? 2 : Integer.parseInt((String) object.get("pay_role"));
            }
            mileage = object.get("mileage") == null ? "1" : (String) object.get("mileage");
            low_speed_time = object.get("low_speed_time") == null ? "0" : (String) object.get("low_speed_time");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
