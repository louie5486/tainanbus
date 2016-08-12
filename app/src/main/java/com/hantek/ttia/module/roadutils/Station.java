package com.hantek.ttia.module.roadutils;

public class Station {
    /**
     * 屬性
     */
    public int type;

    /**
     * 編號( 0 ~ N )
     */
    public int id;

    /**
     * 中文名稱
     */
    public String zhName;

    /**
     * 英文名稱
     */
    public String enName;

    /**
     * 經度
     */
    public double longitude;

    /**
     * 緯度
     */
    public double latitude;

    /**
     * 速限
     */
    public int speedLimit;

    /**
     * 業者自行定義（保留）
     */
    public String reserved;

    /**
     * 業者自行定義（保留）
     */
    public String audioID;

    public Station() {
        zhName = "";
        enName = "";
    }

    /**
     * @param data "屬性; 編號; 中文名稱; 英文名稱; 經度; 緯度; 速限; 業者自行定義（保留）"
     * @return Station
     */
    public static Station Parse(String data) {
        Station station = new Station();
        String[] tmp = data.split(";");
        station.type = Integer.parseInt(tmp[0].trim());
        station.id = Integer.parseInt(tmp[1].trim());
        station.zhName = tmp[2].trim();
        station.enName = tmp[3].trim();
        station.longitude = Double.parseDouble(tmp[4].trim());
        station.latitude = Double.parseDouble(tmp[5].trim());
        station.speedLimit = Integer.parseInt(tmp[6].trim());

        if (tmp.length > 7)
            station.reserved = tmp[7].trim();

        if (tmp.length > 8)
            station.audioID = tmp[8].trim();
        return station;
    }

    public Station clone() {
        Station newStation = new Station();
        newStation.enName = enName;
        newStation.id = id;
        newStation.zhName = zhName;
        newStation.audioID = audioID;
        newStation.type = type;
        newStation.latitude = latitude;
        newStation.longitude = longitude;
        newStation.reserved = reserved;
        newStation.speedLimit = speedLimit;
        return newStation;
    }
}
