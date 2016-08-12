package com.hantek.ttia.module.roadutils;

public interface StationInterface {
    /**
     * 進站事件
     *
     * @param station
     */
    void enterStation(Station station, GpsContent gps, boolean endStation, Road road);

    /**
     * 離站事件
     *
     * @param station
     */
    void leaveStation(Station station, GpsContent gps, boolean endStation, Road road);

    void debug(String content);
}