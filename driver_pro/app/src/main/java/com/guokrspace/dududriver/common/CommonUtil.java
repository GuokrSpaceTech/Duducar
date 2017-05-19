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
