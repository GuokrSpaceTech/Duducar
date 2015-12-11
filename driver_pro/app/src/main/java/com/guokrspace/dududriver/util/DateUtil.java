package com.guokrspace.dududriver.util;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by daddyfang on 15/11/27.
 */
public class DateUtil {

    public static String dateFormat(String date) {
        if (TextUtils.isEmpty(date)) {
            return "刚刚";
        }
        Date orderDate = new Date(Long.parseLong(date+"000"));
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");
        return format.format(orderDate);
    }

}
