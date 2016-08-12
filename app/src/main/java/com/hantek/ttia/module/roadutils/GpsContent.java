package com.hantek.ttia.module.roadutils;

import com.hantek.ttia.protocol.a1a4.GpsStruct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GpsContent {
    public boolean isFixed;
    public double lon;
    public double lat;
    private double speed;
    private double angle;
    public Date time;
    public int satelliteNumber;

    public String nmeaLon;
    public String nmeaLat;
    public GpsStruct gpsStruct;

    public GpsContent() {
        gpsStruct = null;
        nmeaLon = "";
        nmeaLat = "";
    }

    /**
     * 設定海浬
     *
     * @param speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    /**
     * 設定方位角0.0~360.0
     *
     * @param value
     */
    public void setAngle(double value) {
        this.angle = value;
    }

    public double getAngle() {
        return angle;
    }

    public static GpsContent parse(String data) {
        String[] tmp = data.split(",");
        GpsContent d = new GpsContent();
        d.isFixed = tmp[1].trim().equalsIgnoreCase("A");
        d.satelliteNumber = Integer.parseInt(tmp[2]);
        d.lon = Double.parseDouble(tmp[3]);
        d.lat = Double.parseDouble(tmp[4]);
        d.angle = Double.parseDouble(tmp[5]);
        d.speed = Double.parseDouble(tmp[6]);

        TimeZone utc = TimeZone.getTimeZone("UTC");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(utc);
        try {
            d.time = sdf.parse(tmp[7]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    @Override
    public String toString() {
        return "GpsContent{" +
                "isFixed=" + isFixed +
                ", lon=" + lon +
                ", lat=" + lat +
                ", speed=" + speed +
                ", angle=" + angle +
                ", time=" + time +
                ", satelliteNumber=" + satelliteNumber +
                '}';
    }
}