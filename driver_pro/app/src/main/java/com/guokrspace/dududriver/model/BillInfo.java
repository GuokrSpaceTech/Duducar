package com.guokrspace.dududriver.model;

/**
 * Created by hyman on 15/12/10.
 */
public class BillInfo extends BaseModel {

    public String sum; //金额

    public String account; //如果是收入就是支付方或嘟嘟账户，如果是体现就是银行卡

    public String accountType; //交易类型

    public String tradeTime; //交易时间，毫秒数

    public String desctiption; //交易说明

    public BillInfo(String sum, String account, String accountType, String tradeTime, String desctiption) {
        this.sum = sum;
        this.account = account;
        this.accountType = accountType;
        this.tradeTime = tradeTime;
        this.desctiption = desctiption;
    }
}
