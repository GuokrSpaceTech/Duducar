package com.guokrspace.duducar.util;

import android.text.TextUtils;
import android.util.Log;

import com.guokrspace.duducar.BuildConfig;

/**
 * Created by hyman on 16/1/5.
 */
public class Trace {

    public static final String DEFAULT_TAG = "hyman_trace";

    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            if (TextUtils.isEmpty(tag)) {
                Log.e(DEFAULT_TAG, msg);
                return;
            }
            Log.e(tag, msg);
        }
    }

}
