package com.guokrspace.dududriver.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/12/18.
 */
public class BillItem extends BaseModel implements Serializable {

    private Long id;

    /*
     *  交易金额
     */
    private String money;

    /*
     *  交易类型，现阶段三类：0提现，1收到车费， 2系统奖励
     */
    private Integer type;

    /*
     *  交易描述
     */
    private String description;

    /*
     *  交易时间
     */
    private String time;

    /*
     *  交易对象，收入则为支付方，支出则为银行卡
     */
    private String opposite;

    public BillItem() {
        super();
    }

    public BillItem(Long id, String money, Integer type, String description, String time, String opposite) {
        super();
        this.id = id;
        this.money = money;
        this.type = type;
        this.description = description;
        this.time = time;
        this.opposite = opposite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOpposite() {
        return opposite;
    }

    public void setOpposite(String opposite) {
        this.opposite = opposite;
    }
}
