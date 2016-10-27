package com.guokrspace.dududriver.util;

import android.text.TextUtils;
import android.util.Log;

import com.guokrspace.dududriver.BuildConfig;

/**
 * Created by hyman on 15/10/22.
 */
public class LogUtil {
    public static final String DEFAULT_TAG = "dudu_trace";
    //只打印LEVEL级别以上的日志
    public static final int VERBOSE= 1;
    public static final int DEBUG= 2;
    public static final int INFO= 3;
    public static final int WARN= 4;
    public static final int ERROR= 5;
    public static final int NOTHING= 6;
    public static final int LEVEL= ERROR;

    /**
     * 这级别的日志可以打印，则可以打印所有级别的日志
     * @param tag
     * @param msg
     */
    public static void v(String tag,String msg)
    {
        if (LEVEL > VERBOSE)
            return;
        if (BuildConfig.DEBUG) {
            if (TextUtils.isEmpty(tag)) {
                Log.v(DEFAULT_TAG, msg);
                return;
            }
            Log.v(tag, msg);
        }
    }

    public static void v(String msg) {
        v(DEFAULT_TAG, msg);
    }


    /**
     * 这级别的日志可以打印，则可以打印debug以上级别的信息
     * @param tag
     * @param msg
     */
    public static void d(String tag,String msg)
    {
        if (LEVEL > DEBUG)
            return;
        if (BuildConfig.DEBUG) {
            if (TextUtils.isEmpty(tag)) {
                Log.d(DEFAULT_TAG, msg);
                return;
            }
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        d(DEFAULT_TAG, msg);
    }


    /**
     * 这级别的日志可以打印，则可以打印info级别以上的所有信息
     * @param tag
     * @param msg
     */
    public static void i(String tag,String msg)
    {
        if (LEVEL > INFO)
            return;
        if (BuildConfig.DEBUG) {
            if (TextUtils.isEmpty(tag)) {
                Log.i(DEFAULT_TAG, msg);
                return;
            }
            Log.i(tag, msg);
        }
    }

    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }


    /**
     * 这级别的日志可以打印，则可以打印warn级别以上的所有信息
     * @param tag
     * @param msg
     */
    public static void w(String tag,String msg)
    {
        if (LEVEL > WARN)
            return;
        if (BuildConfig.DEBUG) {
            if (TextUtils.isEmpty(tag)) {
                Log.w(DEFAULT_TAG, msg);
                return;
            }
            Log.w(tag, msg);
        }
    }

    public static void w(String msg) {
        w(DEFAULT_TAG, msg);
    }


    /**
     * 可以打印error级别的信息
     * @param tag
     * @param msg
     */
    public static void e(String tag,String msg)
    {
        if (LEVEL > ERROR)
            return;
        if (BuildConfig.DEBUG) {
            if (TextUtils.isEmpty(tag)) {
                Log.e(DEFAULT_TAG, msg);
                return;
            }
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }
}
