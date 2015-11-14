package com.guokrspace.dududriver.model;

/**
 * Created by hyman on 15/11/13.
 */
public class WealthNotice implements BaseNoticeItem {

    public String date;

    public String cardNum;//银行卡尾号

    public String sum;//金额

    public WealthNotice(String date, String cardNum, String sum) {
        this.date = date;
        this.cardNum = cardNum;
        this.sum = sum;
    }
}
