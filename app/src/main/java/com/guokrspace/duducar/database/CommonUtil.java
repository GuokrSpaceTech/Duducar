package com.guokrspace.duducar.database;

/**
 * Created by daddyfang on 15/11/13.
 */
public class CommonUtil {

    public static boolean isLocationSuccess() {
        return locationSuccess;
    }

    public static void setLocationSuccess(boolean locationSuccess) {
        CommonUtil.locationSuccess = locationSuccess;
    }

    public static boolean locationSuccess = false;

    public static double getCurLng() {
        return curLng;
    }

    public static void setCurLng(double curLng) {
        CommonUtil.curLng = curLng;
    }

    public static double getCurLat() {
        return curLat;
    }

    public static void setCurLat(double curLat) {
        CommonUtil.curLat = curLat;
    }

    private static double curLng;
    private static double curLat;

    public static long getCurTime() {
        return curTime;
    }

    public static void setCurTime(long curTime) {
        CommonUtil.curTime = curTime;
    }

    private static long curTime;

}
