package com.guokrspace.duducar.common;

import android.text.TextUtils;

/**
 * Created by hyman on 16/1/4.
 */
public enum CommonAddrType {
    HOME(1, "家"),
    COMPANY(2, "公司"),
    UNDEFINE(3, "未知");


    private final int id;
    private final String desc;

    CommonAddrType(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public static CommonAddrType getByDesc(String desc) {
        for (CommonAddrType type : CommonAddrType.values()) {
            if (TextUtils.equals(type.getDesc(), desc)) {
                return type;
            }
        }
        return UNDEFINE;
    }
}
