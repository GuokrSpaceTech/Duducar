package com.guokrspace.dududriver.model;

/**
 * Created by daddyfang on 15/11/10.
 */
public class DistanceInfo {

    private int id;
    private float distance;
    private double longitude;
    private double latitude;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public float getDistance() {
        return distance;
    }
    public void setDistance(float distance) {
        this.distance = distance;
    }
    public double getLongitude() {

        return longitude;
    }
    public void setLongitude(double longitude) {

        this.longitude = longitude;
    }
    public double getLatitude() {

        return latitude;
    }
    public void setLatitude(double latitude) {

        this.latitude = latitude;
    }
    @Override
    public String toString() {

        return "DistanceInfo [id=" + id +" , distance=" + distance
                +" , longitude= "+ longitude + " , latitude= " + latitude +" ]";
    }

}
