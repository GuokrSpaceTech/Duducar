package com.guokrspace.dududriver.model;

import com.guokrspace.dududriver.util.DateUtil;

/**
 * Created by hyman on 15/11/13.
 */
public class DenseOrderNotice implements BaseNoticeItem {

    public String date;
    public String description;

    public DenseOrderNotice(String message){
        this.date = DateUtil.dateFormat(System.currentTimeMillis() + "");
        this.description = "发现订单密集区域 " + message + " ,点击查看~! ";
    }
    public DenseOrderNotice(String date, String description) {
        this.date = date;
        this.description = description;
    }
}
