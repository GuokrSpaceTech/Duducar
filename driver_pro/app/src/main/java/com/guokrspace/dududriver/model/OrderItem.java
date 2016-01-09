package com.guokrspace.dududriver.model;

import java.io.Serializable;

/**
 * Created by daddyfang on 15/11/2.
 */
public class OrderItem implements Serializable {

    private String CMD;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    private Order order;

    public Charge_rule getCharge_rule() {
        return charge_rule;
    }

    public void setCharge_rule(Charge_rule charge_rule) {
        this.charge_rule = charge_rule;
    }

    private Charge_rule charge_rule;

    public String getCMD() {
        return CMD;
    }

    public void setCMD(String CMD) {
        this.CMD = CMD;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    private String distance;

    public static class Order implements Serializable{

        String start_lat;
        String start_lng;
        String destination_lat;
        String destination_lng;
        String passenger_mobile;
        String start;
        String destination;
        String id;

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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public class Charge_rule implements Serializable{
        public String getStarting_price() {
            return starting_price;
        }

        public void setStarting_price(String starting_price) {
            this.starting_price = starting_price;
        }

        public String getStarting_distance() {
            return starting_distance;
        }

        public void setStarting_distance(String starting_distance) {
            this.starting_distance = starting_distance;
        }

        public String getKm_price() {
            return km_price;
        }

        public void setKm_price(String km_price) {
            this.km_price = km_price;
        }

        public String getLow_speed_price() {
            return low_speed_price;
        }

        public void setLow_speed_price(String low_speed_price) {
            this.low_speed_price = low_speed_price;
        }

        private String starting_price;
        private String starting_distance;
        private String km_price;
        private String low_speed_price;

    }

}
