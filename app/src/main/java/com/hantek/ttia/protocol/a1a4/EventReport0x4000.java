package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

/**
 * 勤務狀態回報(大台南公車)
 */
public class EventReport0x4000 extends EventReportBase {
    /**
     * 發生地點GPS 資料
     */
    public MonitorStructType2 monitorData;

    /**
     * 班表代碼
     */
    public int infoID;

    /**
     * 勤務現在狀態
     */
    public byte type;

    /**
     * 勤務之前狀態
     */
    public byte preType;

    public EventReport0x4000() {
        this.monitorData = new MonitorStructType2();
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[MonitorStructType2.Length + 4];
        int index = 0;

        System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
        index += MonitorStructType2.Length;

        System.arraycopy(BitConverter.toUShortByteArray(this.infoID), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        bytes[index++] = this.type;
        bytes[index++] = this.preType;

        bytes = this.combineByteArray(super.getBytes(), bytes);
        return bytes;
    }

    public EventReport0x4000 clone() {
        EventReport0x4000 varCopy = new EventReport0x4000();

        varCopy.monitorData = this.monitorData;
        varCopy.infoID = this.infoID;
        varCopy.type = this.type;
        varCopy.preType = this.preType;

        return varCopy;
    }
}
