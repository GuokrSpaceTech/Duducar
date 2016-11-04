package com.guokrspace.duducar.communication.http;

import android.util.SparseArray;

/**
 * Created by hyman on 15/11/23.
 */
public class HttpUrls {

    public static final String API_URL = "http://www.duducab.com/api/";
//    api.duducab.com

    public static final int WX_PAY_WXUNIFIEDORDER = 100;//提交预支付请求，获得prepay_id，sign等
    public static final int WX_PAY_GETWXPAYRESULT = 101;//请求确认支付结果

    private static final SparseArray<String> sUrlArray = new SparseArray<>();

    static {
        sUrlArray.put(WX_PAY_WXUNIFIEDORDER, "Pay/wxunifiedorder");
        sUrlArray.put(WX_PAY_GETWXPAYRESULT, "Pay/getWxpayResult");
    }

    public static String getUrl(int key) {
        return  API_URL + sUrlArray.get(key);
    }

}
