package com.guokrspace.dududriver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.java_websocket.util.Base64;
import org.json.JSONObject;
import com.baidu.location.BDLocation;
import com.guokrspace.dududriver.model.GpsLocation;

/**
 * Created by daddyfang on 15/11/10.
 */
public class BDLocation2GpsUtil {

        static BDLocation tempBDLocation = new BDLocation(); // 临时变量，百度位置
        static GpsLocation tempGPSLocation = new GpsLocation(); // 临时变量，gps位置
        public static enum Method{
            origin, correct;
        }
        private static final Method method = Method.correct;
        /**
         * 位置转换
         *
         * @param lBdLocation 百度位置
         * @return GPS位置
         */
        public static GpsLocation convertWithBaiduAPI(BDLocation lBdLocation) {
            switch (method) {
                case origin: //原点
                    GpsLocation location = new GpsLocation();
                    location.lat = lBdLocation.getLatitude();
                    location.lng = lBdLocation.getLongitude();
                    return location;
                case correct: //纠偏
                    //同一个地址不多次转换
                    if (tempBDLocation.getLatitude() == lBdLocation.getLatitude() && tempBDLocation.getLongitude() == lBdLocation.getLongitude()) {
                        return tempGPSLocation;
                    }
                    String url = "http://api.map.baidu.com/ag/coord/convert?from=0&to=4&"
                            + "x=" + lBdLocation.getLongitude() + "&y="
                            + lBdLocation.getLatitude();
                    String result = executeHttpGet(url);
                    LogUtil.i(BDLocation2GpsUtil.class + "", "result:" + result);
                    if (result != null) {
                        GpsLocation gpsLocation = new GpsLocation();
                        try {
                            JSONObject jsonObj = new JSONObject(result);
                            String lngString = jsonObj.getString("x");
                            String latString = jsonObj.getString("y");
                            // 解码
                            double lng = Double.parseDouble(new String(Base64.decode(lngString)));
                            double lat = Double.parseDouble(new String(Base64.decode(latString)));
                            // 换算
                            gpsLocation.lng = 2 * lBdLocation.getLongitude() - lng;
                            gpsLocation.lat = 2 * lBdLocation.getLatitude() - lat;
                            tempGPSLocation = gpsLocation;
                            LogUtil.i(BDLocation2GpsUtil.class + "", "result:" + gpsLocation.lat + "||" + gpsLocation.lng);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                        tempBDLocation = lBdLocation;
                        return gpsLocation;
                    } else {
                        LogUtil.i(BDLocation2GpsUtil.class + "", "百度API执行出错 ,url is:" + url);
                        return null;
                    }
            }
            return null;
        }

    public static String executeHttpGet(String strUrl) {
        String result = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        try {
            url = new URL(strUrl);
            connection = (HttpURLConnection) url.openConnection();
            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

}
