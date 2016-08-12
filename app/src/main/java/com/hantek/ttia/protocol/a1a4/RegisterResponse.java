package com.hantek.ttia.protocol.a1a4;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.hantek.ttia.module.BitConverter;

/**
 * 註冊回覆訊息
 */
public class RegisterResponse {

    /**
     * 註冊結果:0:成功, 1~255 失敗
     */
    public int result;

    /**
     * 班表資料:0:無班表 1:有班表 2:遊覽車
     */
    public int schedule;

    /**
     * 班表之路線代號
     */
    public int routeID;

    /**
     * 班表之路線方向0:其他 1:去程 2:回程
     */
    public int routeDirect;

    /**
     * 班表之路線種類 0x30:主線 0x41~0x5A:支線
     */
    public int routeBranch;

    /**
     * 班表之路線版本
     */
    public int[] routeVer; // byte[] routeVer;

    /**
     * 保留
     */
    public int[] reserved;

    /**
     * 班表之司機代碼
     */
    public long driverID;

    /**
     * 班表之司機姓名（4 個中文）Big5
     */
    public String driverName;

    /**
     * 班表之起站:時
     */
    public int departHour;

    /**
     * 班表之起站:分
     */
    public int departMinute;

    /**
     * UTC時間之年, 西元2000年起始(2009->9)
     */
    public int year;

    /**
     * UTC時間之月
     */
    public int month;

    /**
     * UTC時間之日
     */
    public int day;

    /**
     * UTC時間之時 24 小時制
     */
    public int hour;

    /**
     * UTC時間之分
     */
    public int minute;

    /**
     * UTC時間之秒
     */
    public int second;

    /**
     * 事件回報
     */
    public int eventReport;

    /**
     * 轉速限制（預設值：3000）
     */
    public int RPM = 3000;

    /**
     * 加速度限制（3 秒內加速度超越值）（預設值：30）
     */
    public int accelerate = 30;

    /**
     * 減速度限制（3 秒內減速度超越值）（預設值：30）
     */
    public int decelerate = 30;

    /**
     * 停車不熄火時間限制（單位：分）（預設值：10）
     */
    public int halt = 10;

    /**
     * 進站偵測半徑（單位：10 公尺）（預設值：4）
     */
    public int inRadius = 4;

    /**
     * 出站偵測半徑（單位：10 公尺）（預設值：5）
     */
    public int outRadius = 5;

    /**
     * 異常發車移動距離（單位：10 公尺）（預設值：10）
     */
    public int movement = 10;

    /**
     * [Optional] OTA Check 時間
     */
    public int OTATime;

    /**
     * [Optional] OTA Server IP
     */
    public String OTAIP;

    /**
     * [Optional] OTA Server Port
     */
    public int OTAPort;

    public RegisterResponse() {
        this.driverName = "";
        this.OTATime = 0;
        this.OTAIP = "";
        this.OTAPort = 0;
    }

    public static RegisterResponse Parse(int[] intArray) {
        int index = 0;
        RegisterResponse response = new RegisterResponse();
        response.result = intArray[index++];
        response.schedule = intArray[index++];

        response.routeID = BitConverter.toUShort(intArray, index);
        index += BitConverter.UshortSIZE;

        response.routeDirect = intArray[index++];
        response.routeBranch = intArray[index++];

        try {
            response.routeVer = new int[]{intArray[index], intArray[index + 1]};
        } catch (Exception e) {
            e.printStackTrace();
        }
        index += 2;

        try {
            response.reserved = new int[]{intArray[index], intArray[index + 1]};
        } catch (Exception e) {
            e.printStackTrace();
        }
        index += 2;

        response.driverID = BitConverter.toUInteger(intArray, index);
        index += BitConverter.UintSIZE;

        try {
            response.driverName = BitConverter.toString(intArray, index, 8, "Big-5");//TTIA
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        index += 8;

        response.departHour = intArray[index++];
        response.departMinute = intArray[index++];

        response.year = intArray[index++];
        response.month = intArray[index++];
        response.day = intArray[index++];
        response.hour = intArray[index++];
        response.minute = intArray[index++];
        response.second = intArray[index++];

        response.eventReport = BitConverter.toUShort(intArray, index);
        index += BitConverter.UshortSIZE;

        response.RPM = BitConverter.toUShort(intArray, index);
        index += BitConverter.UshortSIZE;

        response.accelerate = intArray[index++];
        response.decelerate = intArray[index++];
        response.halt = intArray[index++];
        response.inRadius = intArray[index++];
        response.outRadius = intArray[index++];

        response.movement = BitConverter.toUShort(intArray, index);
        index += BitConverter.UshortSIZE;

        try {
            // optional
            response.OTATime = intArray[index++];
            response.OTAIP = String.format("%c.%c.%c.%c", intArray[index], intArray[index + 1], intArray[index + 2], intArray[index + 3]);
            index += 4;

            response.OTAPort = BitConverter.toUShort(intArray, index);
            index += BitConverter.UshortSIZE;
        } catch (Exception e) {
            response.OTATime = 0;
            response.OTAIP = "";
            response.OTAPort = 0;
        }

        return response;
    }

    @Override
    public String toString() {
        return "RegisterResponse [result=" + result + ", schedule=" + schedule + ", routeID=" + routeID + ", routeDirect=" + routeDirect + ", routeBranch=" + routeBranch + ", routeVer="
                + Arrays.toString(routeVer) + ", reserved=" + Arrays.toString(reserved) + ", driverID=" + driverID + ", driverName=" + driverName + ", departHour=" + departHour + ", departMinute="
                + departMinute + ", year=" + year + ", month=" + month + ", day=" + day + ", hour=" + hour + ", minute=" + minute + ", second=" + second + ", eventReport=" + eventReport + ", RPM="
                + RPM + ", accelerate=" + accelerate + ", decelerate=" + decelerate + ", halt=" + halt + ", inRadius=" + inRadius + ", outRadius=" + outRadius + ", movement=" + movement
                + ", OTATime=" + OTATime + ", OTAIP=" + OTAIP + ", OTAPort=" + OTAPort + "]";
    }
}
