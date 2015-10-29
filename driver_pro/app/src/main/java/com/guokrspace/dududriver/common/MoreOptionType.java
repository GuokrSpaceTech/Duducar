package com.guokrspace.dududriver.common;

/**
 * Created by hyman on 15/10/24.
 */
public enum  MoreOptionType {

    SUGGESTION(0, "推荐"),

    ORDER_MAP(1, "订单地图"),

    CAR_UPDATE(2, "车型升级"),

    MESSAGE(3, "消息通知"),

    GUIDE(4, "专车指南"),

    SETTING(5, "设置");

    private int val;

    private String remark;

    public int getVal() {
        return val;
    }

    public String getRemark() {
        return remark;
    }

    private  MoreOptionType(int val, String remark) {
        this.val = val;
        this.remark = remark;
    }

    public static MoreOptionType getByRemark(String remak) {
        for (MoreOptionType mot : MoreOptionType.values()) {
            if (mot.getRemark().equals(remak)) {
                return mot;
            }
        }
        return null;
    }

    public static MoreOptionType getByVal(int val) {
        for (MoreOptionType mot : MoreOptionType.values()) {
            if (mot.getVal() == val) {
                return mot;
            }
        }
        return SUGGESTION;
    }
}
