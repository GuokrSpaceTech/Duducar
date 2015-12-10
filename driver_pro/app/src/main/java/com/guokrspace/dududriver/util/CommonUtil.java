package com.guokrspace.dududriver.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.model.LatLng;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.model.OrderItem;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by hyman on 15/10/22.
 */
public class CommonUtil {

    public static boolean isGpsOpen(Context mContext){
        LocationManager locationManager
                = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

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

    public static OrderItem getCurOrderItem() {
        return curOrderItem;
    }

    public static void setCurOrderItem(OrderItem curOrderItem) {
        CommonUtil.curOrderItem = curOrderItem;
    }

    private static OrderItem curOrderItem;

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

    private static double curLat;
    private static double curLng;
    private static LatLng curLatLng;

    public static LatLng getCurLatLng() {
        return curLatLng;
    }

    public static void setCurLatLng(LatLng curLatLng) {
        CommonUtil.curLatLng = curLatLng;
    }

    public static BDLocation getMCLocation(double lat, double lng){
        BDLocation location = new BDLocation();
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setCoorType("bd09ll");
        return LocationClient.getBDLocationInCoorType(LocationClient.getBDLocationInCoorType(location, BDLocation.BDLOCATION_BD09LL_TO_GCJ02), BDLocation.BDLOCATION_GCJ02_TO_BD09);
    }

    public static String getCurAddress() {
        return curAddress;
    }

    public static void setCurAddress(String curAddress) {
        CommonUtil.curAddress = curAddress;
    }

    public static String getCurAddressDescription() {
        return curAddressDescription;
    }

    public static void setCurAddressDescription(String curAddressDescription) {
        CommonUtil.curAddressDescription = curAddressDescription;
    }

    private static String curAddress;
    private static String curAddressDescription;
    private static long curTime;

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
        return new BigDecimal((float)SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "todaycash", new Float(0.0))).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
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

    public static double countPrice(double mileage, int lowtime) {

        float starting_price = Float.parseFloat((String) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "starting_price", "5.5"));
        float starting_distance = Float.parseFloat((String) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "starting_distance", "6.0"));
        float km_price = Float.parseFloat((String) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "km_price", "2.0"));
        float low_speed_price = Float.parseFloat((String) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "low_speed_price", "2.0"));
        mileage = mileage/1000.0d;
        if(mileage <= starting_distance + 0.5){
            return starting_price + low_speed_price * lowtime;
        }
        mileage = mileage - starting_distance;

        return  new BigDecimal(starting_price + mileage * km_price + lowtime * low_speed_price).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static float getStartPrice(){
        return Float.parseFloat((String) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), "starting_price", "5.5"));
    }

}
