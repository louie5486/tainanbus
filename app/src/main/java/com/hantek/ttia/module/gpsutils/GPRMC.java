package com.hantek.ttia.module.gpsutils;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GPRMC {
    private String originStr;
    private String time;
    private String status;
    private String nmeaLat = "0.0";
    private String latitudeQuadrant = " ";
    private String nmeaLon = "0.0";
    private String longitudeQuadrant = " ";
    private double speed;// knot
    private double angle;
    private String date;
    private String degrees;
    private String east_west;
    private String checksum;

    private double latitude;
    private double longitude;

    private Date gpsTime = new Date(); // utc time

    public double getAngle() {
        return angle;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getDate() {
        return date;
    }

    public String getDegrees() {
        return degrees;
    }

    public String getEast_west() {
        return east_west;
    }

    public Date getGpsTime() {
        return gpsTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getLatitudeQuadrant() {
        return latitudeQuadrant;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLongitudeQuadrant() {
        return longitudeQuadrant;
    }

    public String getNmeaLat() {
        return nmeaLat;
    }

    public String getNmeaLon() {
        return nmeaLon;
    }

    public String getOriginStr() {
        return originStr;
    }

    public double getSpeed() {
        return speed;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public static GPRMC parse(String data) {
        String[] tmp = data.split(",");
        GPRMC rmc = new GPRMC();
        try {
            rmc.originStr = data;
            rmc.time = tmp[1];
            rmc.status = tmp[2];
            rmc.nmeaLat = tmp[3];
            rmc.latitudeQuadrant = tmp[4].length() > 0 ? tmp[4] : " ";
            rmc.nmeaLon = tmp[5];
            rmc.longitudeQuadrant = tmp[6].length() > 0 ? tmp[6] : " ";
            rmc.speed = tmp[7].length() > 0 ? Double.parseDouble(tmp[7]) : 0;
            rmc.angle = tmp[8].length() > 0 ? Double.parseDouble(tmp[8]) : 0;
            rmc.date = tmp[9];
            rmc.degrees = tmp[10];
            rmc.east_west = tmp[11];
            rmc.checksum = tmp[12];

            rmc.latitude = rmc.nmeaLat.length() > 0 ? rmc.DM2DD(rmc.nmeaLat) : 0;
            rmc.longitude = rmc.nmeaLon.length() > 0 ? rmc.DM2DD(rmc.nmeaLon) : 0;

            int day = Integer.parseInt(rmc.date.substring(0, 2));
            int month = Integer.parseInt(rmc.date.substring(2, 4));
            int year = Integer.parseInt(rmc.date.substring(4, 6)) + 2000;
            int hourOfDay = Integer.parseInt(rmc.time.substring(0, 2));
            int minute = Integer.parseInt(rmc.time.substring(2, 4));
            int second = Integer.parseInt(rmc.time.substring(4, 6));

            // GPS Time = UTC Time
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.set(year, month - 1, day, hourOfDay, minute, second); // month = 0~11
            rmc.gpsTime = calendar.getTime();

            // Log.d(TAG, String.format("GetTime(UTC):%s, %s, %s, %s, %s, %s", year, month, day, hourOfDay, minute, second));
            // Log.d(TAG, "GetTime(UTC):" + gpsTime);
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }

        return rmc;
    }

    private double DM2DD(String astr) {
        try {
            String[] aList;
            aList = astr.split("\\.");
            double d1 = Double.parseDouble(aList[0].substring(0, aList[0].length() - 2)) + Double.parseDouble(aList[0].substring(aList[0].length() - 2) + "." + aList[1]) / 60;

            DecimalFormat df = new DecimalFormat("###.0000000");
            d1 = Double.parseDouble(df.format(d1));
            return d1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public String toString() {
        return "GPRMC{" +
                "angle=" + angle +
                ", originStr='" + originStr + '\'' +
                ", time='" + time + '\'' +
                ", status='" + status + '\'' +
                ", nmeaLat='" + nmeaLat + '\'' +
                ", latitudeQuadrant='" + latitudeQuadrant + '\'' +
                ", nmeaLon='" + nmeaLon + '\'' +
                ", longitudeQuadrant='" + longitudeQuadrant + '\'' +
                ", speed=" + speed +
                ", date='" + date + '\'' +
                ", degrees='" + degrees + '\'' +
                ", east_west='" + east_west + '\'' +
                ", checksum='" + checksum + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", gpsTime=" + gpsTime +
                '}';
    }
}
