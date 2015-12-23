package com.guokrspace.dududriver.wxapi;

import java.util.Random;

public class WePayUtil {


    //appid
    //请同时修改  androidmanifest.xml里面，.PayActivityd里的属性<data android:scheme="wxb4ba3c02aa476ea1"/>为新设置的appid
  public static final String APP_ID = "wxa58560e44b455eca";




  //商户号
   public static final String MCH_ID = "1285624001";


//  API密钥，在商户平台设置
    public static final  String API_KEY="412fde4e9c2e2bb619514ecea142e449";

    //获取时间戳（单位为秒）
    public static Long getTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    //获得商户定义的订单号
    public static String genOutTradeNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

}
