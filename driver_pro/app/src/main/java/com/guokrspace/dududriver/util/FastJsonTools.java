package com.guokrspace.dududriver.util;

/**
 * Created by Yang Kai on 14-11-17.
 */

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FastJson工具类，引入了jar包fastjson-1.1.34.jar
 *
 * @author xilehang
 */
public class FastJsonTools {

    /**
     * @param <T>
     * @param jsonString
     * @param cls
     * @return
     */
    public static <T> T getObject(String jsonString, Class<T> cls) {
        T t = null;
        try {
            t = JSON.parseObject(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("daddy", t.toString());
        return t;
    }

    /**
     * @param <T>
     * @param jsonString
     * @param cls
     * @return
     */
    public static <T> List<T> getListObject(String jsonString, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        try {
            list = JSON.parseArray(jsonString, cls);
        } catch (Exception e) {
        }
        return list;
    }


    public static List<Map<String, Object>> listKeyMaps(String jsonString) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            list = JSON.parseObject(jsonString,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
        }
        return list;
    }

}

