package com.guokrspace.duducar.communication.message;

import java.util.List;

/**
 * Created by kai on 10/22/15.
 */
public class NearByCars {
    int message_id;
    String status;
    String cmd;
    List<CarLocation> cars;

   public class CarLocation {
        double lat;
        double lng;
        String car_grade;

       public double getLat() {
           return lat;
       }

       public void setLat(double lat) {
           this.lat = lat;
       }

       public double getLng() {
           return lng;
       }

       public void setLng(double lng) {
           this.lng = lng;
       }

       public String getCar_grade() {
           return car_grade;
       }

       public void setCar_grade(String car_grade) {
           this.car_grade = car_grade;
       }
   }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<CarLocation> getCars() {
        return cars;
    }

    public void setCars(List<CarLocation> cars) {
        this.cars = cars;
    }
}
