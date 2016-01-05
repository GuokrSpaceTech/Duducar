package com.guokrspace.duducar.model;

import java.io.Serializable;

/**
 * Created by hyman on 16/1/4.
 */
public class AddrRowDescriptor implements Serializable{

    public int iconResId;
    public String rowName;
    public String addrName;
    public String addrDetail;
    public Double latitude;
    public Double longitude;

    public AddrRowDescriptor() {
    }

    public AddrRowDescriptor(int iconResId, String rowName, String addrName, String addrDetail, Double latitude, Double longitude) {
        this.iconResId = iconResId;
        this.rowName = rowName;
        this.addrName = addrName;
        this.addrDetail = addrDetail;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
