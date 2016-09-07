package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;
import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.Station;

/**
 * 進出站回報
 */
public class EventReport0x0001 extends EventReportBase {
    /**
     * 發生地點GPS 資料
     */
    public MonitorStructType2 monitorData;

    /**
     * 路線
     */
    public Road oroad;

    /**
     * 站點編號
     */
    public int stationID;

    /**
     * 站點
     */
    public Station istation;

    /**
     * 進出狀態(0x01:IN, 0x00:OUT)
     */
    public byte type;

    /**
     * 出站時用來表示進站至出站期間是否發生車門開啟 0:未曾開啟, 1:曾開啟
     */
    public byte doorOpen;

    public EventReport0x0001() {
        this.monitorData = new MonitorStructType2();
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[MonitorStructType2.Length + 4];
        int index = 0;

        System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
        index += MonitorStructType2.Length;

        System.arraycopy(BitConverter.toUShortByteArray(this.stationID), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        bytes[index++] = this.type;
        bytes[index++] = this.doorOpen;

        bytes = this.combineByteArray(super.getBytes(), bytes);
        return bytes;
    }

    public EventReport0x0001 clone() {
        EventReport0x0001 varCopy = new EventReport0x0001();

        varCopy.monitorData = this.monitorData;
        varCopy.stationID = this.stationID;
        varCopy.type = this.type;
        varCopy.doorOpen = this.doorOpen;

        return varCopy;
    }

    @Override
    public String toString() {
        return "{" +
                "stationID=" + stationID +
                ", type=" + type +
                ", doorOpen=" + doorOpen +
                // ", monitorData=" + monitorData +
                '}';
    }


    public String toFmtString() {
        return "{" +
                "roadID=" + oroad.id +
                ",stationID=" + stationID +
                ",stationName=" + istation.zhName +
                ", type=" + type +
                ", doorOpen=" + doorOpen +
                // ", monitorData=" + monitorData +
                '}';
    }
}
