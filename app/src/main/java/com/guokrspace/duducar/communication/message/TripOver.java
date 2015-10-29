package com.guokrspace.duducar.communication.message;

/**
 * Created by kai on 10/29/15.
 */
public class TripOver {
    String cmd;
    Order  order;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public class Order{
        String id;
        String destination;
        String start_lat;
        String start_lng;
        String destination_lat;
        String destination_lng;
        String mileage;
        String price;
        String car_type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getMileage() {
            return mileage;
        }

        public void setMileage(String mileage) {
            this.mileage = mileage;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getCar_type() {
            return car_type;
        }

        public void setCar_type(String car_type) {
            this.car_type = car_type;
        }
    }
}
