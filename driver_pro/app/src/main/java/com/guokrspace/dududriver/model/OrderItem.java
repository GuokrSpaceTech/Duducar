package com.guokrspace.dududriver.model;

/**
 * Created by daddyfang on 15/11/2.
 */
public class OrderItem {

    private Passenger passenger;
    private String orderNo;

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }


    public class Passenger{

        String start_lat;
        String start_lng;
        String end_lat;
        String end_lng;
        String mobile;


        public String getStart_lat() {
            return start_lat;
        }

        public void setStart_lat(String start_lat) {
            this.start_lat = start_lat;
        }

        public String getEnd_lat() {
            return end_lat;
        }

        public void setEnd_lat(String end_lat) {
            this.end_lat = end_lat;
        }

        public String getEnd_lng() {
            return end_lng;
        }

        public void setEnd_lng(String end_lng) {
            this.end_lng = end_lng;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getStart_lng() {
            return start_lng;
        }

        public void setStart_lng(String start_lng) {
            this.start_lng = start_lng;
        }
    }

}
