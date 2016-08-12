package com.hantek.ttia.module.roadutils;

import java.util.Date;

public class LanLng {
    double lat;
    double lon;
    Date time;

    public LanLng(double lat, double lon, Date time) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
    }
}
