package com.guokrspace.dududriver.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.model.LatLng;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.model.OrderItem;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hyman on 15/10/22.
 */
public class CommonUtil {

    public static final String MONTH_DAY_ZH = "MM月dd日 HH:mm";
    public static final String YEAR_MONTH_DAY = "yyyy-MM-dd HH:mm:ss";

    private static String IMSI;
    private static String IMEI;
    private static String MAC;

    public static String getIMSI() {
        if (IMSI == null) {
            setIMSI(PhoneUtils.getIMSI());
        }
        return IMSI;
    }

    public static void setIMSI(String IMSI) {
        CommonUtil.IMSI = IMSI;
    }

    public static String getIMEI() {
        if (IMEI == null) {
            setIMEI(PhoneUtils.getIMEI());
        }
        return IMEI;
    }

    public static void setIMEI(String IMEI) {
        CommonUtil.IMEI = IMEI;
    }

    public static String getMAC() {
        if (MAC == null){
            setMAC(PhoneUtils.getMAC());
        }
        return MAC;
    }

    public static void setMAC(String MAC) {
        CommonUtil.MAC = MAC;
    }

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

    public static String getCurSpeed() {
        return curSpeed;
    }

    public static void setCurSpeed(String curSpeed) {
        CommonUtil.curSpeed = curSpeed;
    }

    public static float getCurDirction() {
        return curDirction;
    }

    public static void setCurDirction(float curDirction) {
        CommonUtil.curDirction = curDirction;
    }

    private static String curSpeed;
    private static double curLat;
    private static double curLng;
    private static LatLng curLatLng;
    private static float curDirction;

    public static double curBaseDistance;
    public static int curBaseLowTime;
    public static double curPrice;
    public static double curChargeTime;
    public static double cur5sDistance;
    public static double curDistance;
    public static int curLowSpeedTime;
    public static int curOrderId;
    public static int curOrderStatus;
    public static boolean isCharging;
//    public static OrderItem curOrderItem;

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

        if(!today.equals(SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY, new String("20151218")))){
            initTodayAllWork();
            initTodayCash();
            initTodayDoneWork();
            SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY, today);
        }
    }

    public static int getTodayDoneWork(){
        return (int) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_DONE_WORK, new Integer(0));
    }

    public static void addTodayDoneWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_DONE_WORK, new Integer(getTodayDoneWork() + 1));
    }

    private static void initTodayDoneWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_DONE_WORK, new Integer(0));
    }

    private static int getTodayAllWork(){
        return (int) SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_ALL_WORK, new Integer(0));
    }

    public static void addTodayAllWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_ALL_WORK, new Integer(getTodayAllWork() + 1));
    }

    public static String getTodayWorkRate(){
        if(getTodayAllWork() == 0 || getTodayDoneWork() == 0){
            return "0";
        }
        double rate = ((double)getTodayDoneWork() * 100.00D) / ((double)getTodayAllWork());
        return new DecimalFormat("###.0").format(rate) ;
    }

    private static void initTodayAllWork(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_ALL_WORK, new Integer(0));
    }

    public static float getTodayCash(){
        return new BigDecimal((float)SharedPreferencesUtils.getParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_CASH, new Float(0.0))).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static void addTodayCash(float cash){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_CASH, new Float(getTodayCash() + cash));
    }

    private static void initTodayCash(){
        SharedPreferencesUtils.setParam(DuduDriverApplication.getInstance(), Constants.PREFERENCE_KEY_TODAY_CASH, new Double(0));
    }

    public static boolean isServiceOn() {
        return isServiceOn;
    }

    public static void setIsServiceOn(boolean isServiceOn) {
        CommonUtil.isServiceOn = isServiceOn;
    }

    private static boolean isServiceOn;

    public static double countPrice(double mileage, int lowtime) {

        float starting_price = Float.parseFloat(CommonUtil.getCurOrderItem().getCharge_rule().getStarting_price());
        float starting_distance = Float.parseFloat(CommonUtil.getCurOrderItem().getCharge_rule().getStarting_distance());
        float km_price = Float.parseFloat(CommonUtil.getCurOrderItem().getCharge_rule().getKm_price());
        float low_speed_price = Float.parseFloat(CommonUtil.getCurOrderItem().getCharge_rule().getLow_speed_price());
        mileage = mileage/1000.0d;
        if(mileage <= starting_distance + 0.2){
            return starting_price + low_speed_price * lowtime;
        }
        mileage = mileage - starting_distance;

        return  new BigDecimal(starting_price + mileage * km_price + lowtime * low_speed_price).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static float getStartPrice(){
        if (CommonUtil.getCurOrderItem() != null)
            return Float.parseFloat(CommonUtil.getCurOrderItem().getCharge_rule().getStarting_price());
        return Float.parseFloat("5.0");
    }

    //格式化日期
    public static String dateFormat(String date, String pattern) {
        if (TextUtils.isEmpty(date)) {
            return "刚刚";
        }
        String currentTime = String.valueOf(System.currentTimeMillis());
        if (currentTime.length() > date.length()) {
            StringBuilder sb = new StringBuilder(date);
            for (int i = 0; i < currentTime.length() - date.length(); i++) {
                sb.append("0");
            }
            date = sb.toString();
        }
        Date orderDate = new Date(Long.parseLong(date));
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(orderDate);
    }

    //格式化手机号：139****3214
    public static String phoneNumFormat(String mobiles) {
        if (TextUtils.isEmpty(mobiles)) {
            return "110";
        }
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        if (mobiles.length() == 11 && m.matches()) {
            String centerNums = mobiles.substring(3, 7);
            mobiles = mobiles.replace(centerNums, "****");
            return mobiles;
        }
        return mobiles;
    }

    /*
     * 检验输入的金额是否符合格式要求，小数点后最多两位
     * @param balance
     * @return
     */
    public static boolean checkBalanceFormat(String balance) {
        String reg = "^(([1-9]\\d*)|0)(\\.\\d{1,2})?$";
        Pattern p = Pattern.compile(reg);
        Matcher matcher = p.matcher(balance);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }


    /*
     * 返回当前应用的版本名称
     * @param context
     * @return
     */
    public static String getAPKVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    /*
     * 格式化为小数点后只有一位小数
     * @param dec
     * @return
     */
    public static String formatDecimal(String dec) {
        if (TextUtils.isEmpty(dec)) return "";
        Double d = Double.parseDouble(dec);
        return String.format("%.1f", d);
    }


    /*
     * 返回当前日期是星期几，星期一到星期天返回1~7
     * @param dt
     * @return
     */
    public static int getDayOfWeek(Date dt) {
        if (dt == null) return Integer.MAX_VALUE;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt);
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w == 0)
            w = 7;
        return w;
    }

    /*
     * 根据当前日期返回下次提现的具体日期，以yyyy-MM-dd格式的字符串返回
     * @param dayOfWeek
     * @return
     */
    public static String getNextWithdrawDate(int dayOfWeek) {
        int diff = 2 - dayOfWeek;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar cal = Calendar.getInstance();
        Date newDate = new Date(System.currentTimeMillis());
        cal.setTime(newDate);
        if (diff < 0) {
            diff = 7 - Math.abs(diff);
        }
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + diff);
        return formatter.format(cal.getTime());
    }

    @TargetApi(23)
    public static void getPermissions(final Activity act) {
        Log.e("getPermission", " start to getpermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("getPermission", "SDK check ");
            ArrayList<String> permissions = new ArrayList<>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(act.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(act.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(act, permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(act, permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                Log.e("PERMISSION", "REQUEST PERMISSION");
                if (!act.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showMessageOKCancel(act.getApplicationContext(), "无法完成定位,请查看权限管理!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startSettingActivity(act);
                                    AppExitUtil.getInstance().exit();
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //直接退出
                                    dialog.dismiss();
                                    AppExitUtil.getInstance().exit();
                                }
                            });
                    return;
                }
                act.requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
                Log.e("PERMISSION", "REQUEST AFTER");
            }
        }
    }

    @TargetApi(23)
    private static boolean addPermission(Activity act, ArrayList<String> permissionsList, String permission) {
        if (act.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (act.shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    private static void startSettingActivity(Activity act){

        Intent i = new  Intent();
        i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + act.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            act.startActivity(i);
        } catch (Exception e){
            Log.e("PickUpService", "start Setting failed");
        }
    }

    private static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener exitListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("设  置", okListener)
                .setNegativeButton("退  出", exitListener)
                .setCancelable(false)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        AppExitUtil.getInstance().exit();
                    }
                })
                .create()
                .show();
    }

    private static final int SDK_PERMISSION_REQUEST = 127;
    private static String permissionInfo;

}
