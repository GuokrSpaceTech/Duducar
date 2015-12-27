package com.guokrspace.duducar.database;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.guokrspace.duducar.communication.message.OrderDetail;

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

    public static PersonalInformation getPersion() {
        return persion;
    }

    public static void setPersion(PersonalInformation persion) {
        CommonUtil.persion = persion;
    }

    private static PersonalInformation persion;

    public static OrderDetail tripOverOrderDetail = null;


    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /*
     * 格式化为小数点后只有一位小数
     * @param dec
     * @return
     */
    public static String formatDecimal(String dec) {
        if (TextUtils.isEmpty(dec)) return "";
        Double d = Double.parseDouble(dec);
        return String.format("%.1f",d);
    }

}
