package com.hantek.ttia.module.roadutils;

import java.util.ArrayList;

/**
 * 路線資料
 */
public class Road {
    /**
     * 站點總數
     */
    public int totalStation;

    /**
     * 路線版本(0~255)
     */
    public int version;

    /**
     * 播音性別 男:m, 女:f
     */
    public String audioGender;

    /**
     * 播音種類 國(c)/台(t)/客(h)/英(e), 1/2/4/8
     */
    public String audioType;

    /**
     * 起站
     */
    public String beginStation;

    /**
     * 迄站
     */
    public String endStation;

    /**
     * 路線種類, 0:國道, 1:一般
     */
    public int type;

    /**
     * 路線長度(公尺)
     */
    public int distance;

    /**
     * 行駛時間(分鐘)
     */
    public int driveTime;

    /**
     * 站點清單
     */
    public ArrayList<Station> stationArrayList;

    /**
     * 路線編號
     */
    public int id;

    /**
     * char 路線種類 0x30(‘0’)主線，0x41~0x5A(‘A’~‘Z’)支線
     */
    public String branch;

    /**
     * 路線方向 0 其他，1 去程，2 回程
     */
    public int direct;

    /**
     * 大台南公車, 路線歡迎詞
     */
    public String audioNumber;

    public Road() {
        this.audioGender = "m";
        this.beginStation = "";
        this.endStation = "";
        this.audioNumber = "";
        this.stationArrayList = new ArrayList<>();
    }

    public static Road Parse(String data) {
        Road road = new Road();
        // String[] tmp = data.split("\r\n");
        // road.totalStation = Integer.parseInt(tmp[0].trim());
        // road.version = Integer.parseInt(tmp[1].trim());
        // String[] tmpAudio = tmp[2].trim().split(";");
        // road.audioGender = tmpAudio[0];
        // road.audioType = tmpAudio[1];
        // String[] tmpRoad = tmp[3].trim().split(";");
        // road.startStation = tmpRoad[0].trim();
        // road.endStation = tmpRoad[1].trim();
        // road.distance = Integer.parseInt(tmpRoad[2].trim());
        // road.driveTime = Integer.parseInt(tmpRoad[3].trim());
        return road;
    }

    public Road clone() {
        Road road = new Road();
        road.stationArrayList = stationArrayList;
        road.type = type;
        road.audioNumber = audioNumber;
        road.branch = branch;
        road.audioGender = audioGender;
        road.audioType = audioType;
        road.beginStation = beginStation;
        road.direct = direct;
        road.distance = distance;
        road.driveTime = driveTime;
        road.endStation = endStation;
        road.id = id;
        road.totalStation = totalStation;
        road.version = version;
        return road;
    }

    @Override
    public String toString() {
        return "Road [totalStation=" + totalStation + ", version=" + version + ", audioGender=" + audioGender + ", audioType=" + audioType + ", startStation=" + beginStation + ", endStation="
                + endStation + ", type=" + type + ", distance=" + distance + ", driveTime=" + driveTime + ", stationArrayList=" + stationArrayList.size() + ", id=" + id + ", branch=" + branch + ", direct="
                + direct + "]";
    }
}