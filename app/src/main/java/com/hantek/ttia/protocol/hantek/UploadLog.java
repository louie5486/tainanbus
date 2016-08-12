package com.hantek.ttia.protocol.hantek;

import com.hantek.ttia.module.BitConverter;

import java.io.UnsupportedEncodingException;

/**
 * 上傳log
 */
public class UploadLog {
    public int type;
    public int year;
    public int month;
    public int day;
    public String fileName;

    public UploadLog() {
        fileName = "";
    }

    public static UploadLog Parse(int[] intArray) {
        int index = 0;
        UploadLog log = new UploadLog();
        log.type = intArray[index++];

        log.year = BitConverter.toUShort(intArray, index);
        index += BitConverter.UshortSIZE;

        log.month = BitConverter.toUShort(intArray, index);
        index += BitConverter.UshortSIZE;

        log.day = BitConverter.toUShort(intArray, index);
        index += BitConverter.UshortSIZE;

        try {
            log.fileName = BitConverter.toString(intArray, index, intArray.length - 7, "Big-5");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return log;
    }
}
