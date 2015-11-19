package com.guokrspace.dududriver.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.guokrspace.dududriver.DuduDriverApplication;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by hyman on 15/10/22.
 */
public class CommonUtil {
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }
        return false;
    }

    public static boolean isMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean checkSdCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    public static boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private static String currentStatus;

    public static void changeCurStatus(String curStatus){
        currentStatus = curStatus;
    }

    public static String getCurrentStatus(){
        return currentStatus;
    }

    public static int getOrderDealInfoId() {
        return orderDealInfoId;
    }

    public static void setOrderDealInfoId(int orderDealInfoId) {
        CommonUtil.orderDealInfoId = orderDealInfoId;
    }

    private static int orderDealInfoId;

    public static double getCurLat() {
        return curLat;
    }

    public static void setCurLat(double curLat) {
        CommonUtil.curLat = curLat;
    }

    public static double getCurLng() {
        return curLng;
    }

    public static void setCurLng(double curLng) {
        CommonUtil.curLng = curLng;
    }

    public static long getCurTime() {
        return curTime;
    }

    public static void setCurTime(long curTime) {
        CommonUtil.curTime = curTime;
    }

    public static double curLat;
    public static double curLng;
    public static long curTime;

    public static void updateToday(){
        TimeZone.setDefault(TimeZone.getTimeZone("GTM-8"));
        Calendar mCalendar = Calendar.getInstance();
        String today =  mCalendar.get(Calendar.YEAR) + "-" + mCalendar.get(Calendar.DAY_OF_YEAR);

        if(!today.equals(SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "today", new String("20151")))){
            initTodayAllWork();
            initTodayCash();
            initTodayDoneWork();
            SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), "today", today);
        }
    }

    public static int getTodayDoneWork(){
        return (int) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "todaydonework", new Integer(0));
    }

    public static void addTodayDoneWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), "todaydonework", new Integer(getTodayDoneWork() + 1));
    }

    private static void initTodayDoneWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), "todaydonework", new Integer(0));
    }

    private static int getTodayAllWork(){
        return (int) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "todayallwork", new Integer(0));
    }

    public static void addTodayAllWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), "todayallwork", new Integer(getTodayAllWork() + 1));
    }

    public static String getTodayWorkRate(){
        if(getTodayAllWork() == 0){
            return "0";
        }
        double rate = ((double)getTodayDoneWork() * 100.00D) / ((double)getTodayAllWork());
        return new DecimalFormat("###.0").format(rate) ;
    }

    private static void initTodayAllWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), "todayallwork", new Integer(0));
    }

    public static float getTodayCash(){
        return (float)SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "todaycash", new Float(0.0));
    }

    public static void addTodayCash(float cash){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), "todaycash", new Float(getTodayCash() + cash));
    }

    private static void initTodayCash(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), "todaycash", new Double(0));
    }

    public static boolean isServiceOn() {
        return isServiceOn;
    }

    public static void setIsServiceOn(boolean isServiceOn) {
        CommonUtil.isServiceOn = isServiceOn;
    }

    private static boolean isServiceOn;

}
