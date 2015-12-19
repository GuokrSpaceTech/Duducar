package com.guokrspace.dududriver.model;

import com.guokrspace.dududriver.util.DateUtil;

/**
 * Created by hyman on 15/11/13.
 */
public class DenseOrderNotice implements BaseNoticeItem {

    public String date;
    public String description;
    public int message_notice_id;

    public DenseOrderNotice(String message, int noticeId){
        this.date = DateUtil.dateFormat(System.currentTimeMillis() + "");
        this.description = "发现订单密集区域 " + message + " ,点击查看~! ";
        message_notice_id = noticeId;
    }
    public DenseOrderNotice(String date, String description) {
        this.date = date;
        this.description = description;
    }
}
