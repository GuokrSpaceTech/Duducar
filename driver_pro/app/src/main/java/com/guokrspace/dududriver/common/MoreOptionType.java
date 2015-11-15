package com.guokrspace.dududriver.common;

/**
 * Created by hyman on 15/10/24.
 */
public enum  MoreOptionType {

    GUIDE(0, "专车指南"),

    ORDER_MAP(1, "订单地图"),

    MESSAGE(2, "消息通知"),

    SETTING(3, "账号设置");

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
        return GUIDE;
    }
}
