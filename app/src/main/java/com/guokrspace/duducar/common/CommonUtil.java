package com.guokrspace.duducar.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.util.PhoneUtils;

/**
 * Created by daddyfang on 15/11/13.
 */
public class CommonUtil {

    private static String IMSI;
    private static String IMEI;
    private static String MAC;

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
        Context _context = context.getApplicationContext();
        NetworkInfo info = getNetworkInfo(_context);
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
}
