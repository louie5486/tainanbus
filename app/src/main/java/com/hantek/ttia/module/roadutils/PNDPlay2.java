package com.hantek.ttia.module.roadutils;

import android.os.Message;
import android.util.Log;

import com.hantek.ttia.FragmentTestMode;
import com.hantek.ttia.SystemPara;
import com.hantek.ttia.module.Utility;
import com.hantek.ttia.module.gpsutils.GpsReceiver;
import com.hantek.ttia.protocol.a1a4.GpsStruct;
import com.hantek.ttia.protocol.e2.DCR;
import com.hantek.ttia.protocol.e2.ReadIntSpeed;
import com.hantek.ttia.protocol.e2.SpeedData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import component.LogManager;
import component.gps_test;

/**
 * 20160909 修改邏輯
 */
public class PNDPlay2 extends RoadManager {

    final int MAX_STOP_DIST = 500;

    //Road currentRoad = null;
    boolean stationChecked = false;
    int iNowStationNo = 1;//目前 站牌序號   '從1開始 1~
    int iOldStatNo;
    int nowStNo;
    int calTimes = 0;
    boolean duty_start = true;  //切換路線、開始勤務第一站的判斷
    int iFindNextStop;

    int bonStation_flag = 0; //偏移路線後，到站播報的旗標
    boolean bonGOLarrival;
    GpsContent last_gpsContent;
    gps_test gps = SystemPara.getInstance().getGPS();
    private Timer run_Thread = new Timer();

    public PNDPlay2(){
        this.Initial();
    }

    private TimerTask t_task = new TimerTask() {
        @Override
        public void run() {
            try{
                GpsContent data = new GpsContent();
                if (gps.getNow_location() == null){
                    data.lat = GpsReceiver.getInstance().getLatitude();
                    data.lon = GpsReceiver.getInstance().getLongitude();
                    data.isFixed = GpsReceiver.getInstance().isFixed();
                    data.time = GpsReceiver.getInstance().getUTCTime();
                    data.setSpeed(GpsReceiver.getInstance().getSpeed());
                    data.setAngle(GpsReceiver.getInstance().getAngle());
                    data.satelliteNumber = GpsReceiver.getInstance().getSatelliteNumber();
                    data.nmeaLat = GpsReceiver.getInstance().getNmeaLatitude();
                    data.nmeaLon = GpsReceiver.getInstance().getNmeaLongitude();
                    data.gpsStruct = getGpsStruct();
                }else{
                    data.lat = gps.getNow_location().getLatitude();
                    data.lon = gps.getNow_location().getLongitude();
                    data.isFixed = gps.isIs_valid();
                    data.time = gps.getGps_d();
                    data.setSpeed((double)gps.getSpeeed());
                    data.setAngle(Double.parseDouble(gps.getAngle()));
                    data.satelliteNumber = gps.getSat_num();
                    data.nmeaLat = GpsReceiver.getInstance().getNmeaLatitude();
                    data.nmeaLon = GpsReceiver.getInstance().getNmeaLongitude();
                    data.gpsStruct = getGpsStruct();
                }


                checkStation(data);
            }catch(Exception x){
                x.printStackTrace();
            }


        }
        private GpsStruct getGpsStruct() {
            GpsStruct struct = new GpsStruct();
            struct.satelliteNo = (byte)gps.getSat_num();
            struct.gpsStatus = (byte) (gps.isIs_valid() ? 1 : 0);

            String tmpLongitude = GpsReceiver.getInstance().getNmeaLongitude();
            String tmpLatitude = GpsReceiver.getInstance().getNmeaLatitude();

            byte lonDu = Utility.nmeaToDu(tmpLongitude);
            byte lonFen = Utility.nmeaToFen(tmpLongitude);
            int lonMiao = Utility.nmeaToMiao(tmpLongitude);
            struct.longitudeDu = lonDu;
            struct.longitudeFen = lonFen;
            struct.longitudeMiao = lonMiao;
            struct.longitudeQuadrant = GpsReceiver.getInstance().getLongitudeQuadrant().getBytes()[0];
            if (lonDu != 0 && lonMiao == 0) {
                LogManager.write("error", String.format("Longitude:%s,%s,%s. %s,%s ", struct.longitudeDu, struct.longitudeFen, struct.longitudeMiao, tmpLatitude, tmpLongitude), null);
            }//2016-03-22 add log, why equals zero.

            byte latDu = Utility.nmeaToDu(tmpLatitude);
            byte latFen = Utility.nmeaToFen(tmpLatitude);
            int latMiao = Utility.nmeaToMiao(tmpLatitude);
            struct.latitudeDu = latDu;
            struct.latitudeFen = latFen;
            struct.latitudeMiao = latMiao;
            struct.latitudeQuadrant = GpsReceiver.getInstance().getLatitudeQuadrant().getBytes()[0];
            if (latDu != 0 && latMiao == 0) {
                LogManager.write("error", String.format("Latitude:%s,%s,%s. %s,%s ", struct.latitudeDu, struct.latitudeFen, struct.latitudeMiao, tmpLatitude, tmpLongitude), null);
            }//2016-03-22 add log, why equals zero.

            struct.direction = (int) GpsReceiver.getInstance().getAngle();

            // get dcr speed or gps speed
            boolean useGps = true;
            if (DCR.getInstance().isOpen() && Utility.dateDiffNow(DCR.getInstance().getLastReceiveTime()) < 5000) {
                useGps = false;
            }

            if (useGps) {
                struct.intSpeed = (int) (GpsReceiver.getInstance().getSpeed() * 1.852d); // gps speed
            } else {
                ReadIntSpeed readIntSpeed = DCR.getInstance().getLastReadIntSpeed();
                if (readIntSpeed != null) {
                    List<SpeedData> dataList = readIntSpeed.getSpeedDataList();
                    if (dataList != null && dataList.size() > 0) {
                        SpeedData data = dataList.get(0);
                        if (data != null) {
                            struct.intSpeed = data.getSpeed();
                        }
                    }
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            if (Utility.dateDiffNow(GpsReceiver.getInstance().getLastReceiveTime()) < 5000) {
                calendar.setTime(GpsReceiver.getInstance().getUTCTime());
            }

            int tmpYear = calendar.get(Calendar.YEAR);
            struct.year = (byte) (tmpYear >= 2000 ? tmpYear - 2000 : 0);
            struct.month = (byte) (calendar.get(Calendar.MONTH) + 1);
            struct.day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
            struct.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            struct.minute = (byte) calendar.get(Calendar.MINUTE);
            struct.second = (byte) calendar.get(Calendar.SECOND);

//         Log.d(TAG, "GetGpsStruct:" + tmpLongitude + " " + tmpLatitude);
            return struct;
        }

    };

    public void Initial(){
        run_Thread.schedule(t_task,1000,1000);
    }

    @Override
    public void reset() {
        super.reset();
        this.stationInterface.debug(String.format("Reset=%s,%s,.", currentRoad.id, currentRoad.beginStation));
        stationChecked = false;
        setiNowStationNo(1); // iNowStationNo = 1;
        bonStation_flag = 0;
        this.calTimes = 0;
        duty_start = true;
    }

    private void show_debug_msg(String ds) {
        if (ds != null && ds.length() > 0) {
            if (FragmentTestMode.mHandler != null) {
                Message msg = new Message();
                msg.what = FragmentTestMode.HDL_Staion;
                msg.obj = ds;
                FragmentTestMode.mHandler.sendMessage(msg);
            }
        }
    }

    public void checkStation(GpsContent gpsContent) {
        int iGetNo;
        last_gpsContent = gpsContent;

        if (this.stationInterface == null) {
            Log.w(TAG, "null interface");
            return;
        }

        // 未選擇, 選擇其他路線
        if (this.currentRoad == null || this.currentRoad.id == 65535) {
            return;
        }


        // [START]平均位移過濾
        try {
            LanLng lanLng = new LanLng(gpsContent.lat, gpsContent.lon, gpsContent.time);
            pointList.add(lanLng);
            if (pointList.size() > OFFSET_SIZE) {
                pointList.remove(0);
            }

            if (calcGpsAvgOffset() >= 50) {
                this.stationInterface.debug(String.format("Offset,%s,%s,%s,%s,%s,%s,.",
                        gpsContent.satelliteNumber, gpsContent.lon, gpsContent.lat, gpsContent.getAngle(), gpsContent.getSpeed(), gpsContent.time));
//                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // [END]平均位移過濾
        this.stationInterface.debug(String.format("%s,%s,%s,%s,%s,%s,%s,.", gpsContent.isFixed ? "A" : "V", gpsContent.satelliteNumber, gpsContent.lon, gpsContent.lat, gpsContent.getAngle(), gpsContent.getSpeed(), gpsContent.time));


        if (gpsContent.isFixed) {
            // GPS 訊號正常
            if (getiNowStationNo() >= currentRoad.stationArrayList.size()) {
                //最後一站, 重新定位
                stationChecked = false;
                setiNowStationNo(1);
                this.stationInterface.debug(String.format("W6,%s,%s,%s,%s,.", gpsContent.lat, gpsContent.lon, gpsContent.getAngle(), gpsContent.getSpeed()));
            }

            //速度五以下 ,且不是在第一站, 且不是折返點 不判斷
            if (gpsContent.getSpeed()< 5 && getiNowStationNo() > 1) {
                // this.stationInterface.debug(String.format("PND 速度五以下 ,且不是在第一站, 且不是折返點 不判斷, Origin=%s, KM=%s,.", gpsContent.getSpeed(), gpsContent.getSpeed() * 1.852d));
                this.stationInterface.debug(String.format("W5,%s,%s,%s,%s,%s,.", gpsContent.lat, gpsContent.lon, gpsContent.getAngle(), gpsContent.getSpeed(), gpsContent.getSpeed() * 1.852d));
                return;
            }

            if (!this.stationChecked) {
                iGetNo = getNowStationNo(gpsContent);
                if (iGetNo > 0) {
                    setiNowStationNo(iGetNo);
                    this.stationInterface.debug(String.format("C,%s,%s,%s,%s,%s,%s,.", gpsContent.lat, gpsContent.lon, gpsContent.getAngle(), gpsContent.getSpeed(), currentRoad.stationArrayList.get(getiNowStationNo() - 1).id, currentRoad.stationArrayList.get(getiNowStationNo() - 1).zhName));
                    this.stationChecked = true;
                } else
                    this.stationChecked = false;
            }

            this.iOldStatNo = getiNowStationNo();
            if (this.stationChecked) {

                //針對第一站的播報加入的邏輯
                if (duty_start){
                    if (getiNowStationNo() == 1){
                        this.checkNowStation(gpsContent);
                        if (bonStation_flag==1){
                            this.stationInterface.enterStation(currentRoad.stationArrayList.get(getiNowStationNo() - 1), gpsContent, false, currentRoad);
                            duty_start = false;
                        }else{
                            if (bonStation_flag == 2){
                                this.stationInterface.leaveStation(currentRoad.stationArrayList.get(getiNowStationNo()-1), gpsContent, false, currentRoad);
                                duty_start = false;
                            }
                            if (bonStation_flag == 4){
                                stationChecked = false;
                                bonStation_flag = 0;
                                this.calTimes = 0;
                                duty_start = false;
                            }
                        }
                    }else{
                        duty_start = false;
                    }
                    return;
                }

                iFindNextStop = findNextStop(gpsContent.lon, gpsContent.lat, gpsContent.getSpeed(), gpsContent.getAngle());
                switch (iFindNextStop) {
                    case 1://到達新站
//                        this.stationInterface.debug(String.format("PND 結果IN=%s,.", currentRoad.stationArrayList.get(getiNowStationNo()).zhName));
                        this.stationInterface.enterStation(currentRoad.stationArrayList.get(getiNowStationNo()), gpsContent, false, currentRoad);
                        setiNowStationNo(iNowStationNo + 1);
                        arrive = true;
                        break;
                    case 2://離站
//                        this.stationInterface.debug(String.format("PND 結果OUT=%s,.", currentRoad.stationArrayList.get(getiNowStationNo() - 1).zhName));
                        this.stationInterface.leaveStation(currentRoad.stationArrayList.get(getiNowStationNo()), gpsContent, false, currentRoad);
                        arrive = false;
                        break;
                    case 3://路線偏移
                        if (stationChecked) {
                            stationChecked = false;
                            bonStation_flag = 0;
                            this.calTimes = 0;
                            this.stationInterface.debug(String.format("PND 結果路線偏移=%s, %s, %s,.", iFindNextStop, iLeaveStop, currentRoad.stationArrayList.get(getiNowStationNo()).zhName));
                        }
                        break;
                    case 4://接近,進站
                        this.stationInterface.debug(String.format("PND 結果接近, 紀錄進站=%s,.", iFindNextStop));
                        break;
                }
            }
        }
    }

    boolean bonNearStop = false; //接近
    boolean bonArrival = false; //進站
    boolean bonNextStop = false; //進站
    boolean bonLeave = false; //離站
    int iLeaveStop = 0;//偏移路線判斷 次數

    private void setiNowStationNo(int value) {
        this.iNowStationNo = value;
    }

    private int getiNowStationNo() {
        return this.iNowStationNo;
    }

    private int findNextStop(double lon, double lat, double speed, double angle) {
        //  依據 座標 變化 , 比對是否到下一站
        int iDist;
        int iNextDist;
        int iStopArea = inRadius, iLeaveArea = outRadius;
        int iCheckStopArea = iStopArea + iLeaveArea + 50; // 檢查 兩站之間 距離是否過近
        double sigKM = speed * 1.852d; //' 海里 換算為 公里
        double sigKM2 = (sigKM * 1000) / 3600; // 公尺 / 每秒
        int iStopArea2 = (int) (iStopArea + sigKM2); // 進站距離  加入 速度因素
        int iLeaveArea2 = (int) (iLeaveArea - sigKM2); // 離站距離  加入 速度因素
        int iRet = 0;
        int sigStopDist = 0;//兩站之間的距離

        try {
            ArrayList<Station> stationArrayList = currentRoad.stationArrayList;
            // TODO 折返點
            //本站
            Station tmpCurrStation = stationArrayList.get(getiNowStationNo() - 1);
            //下一站
            Station tmpNextStation = stationArrayList.get(getiNowStationNo());

            iDist = (int) Utility.calcDistance(lon, lat, tmpCurrStation.longitude, tmpCurrStation.latitude);
//            this.stationInterface.debug(String.format("PND findNextStop 最近站的距離=%s,%s,.", tmpCurrStation.zhName, iDist));

            if (getiNowStationNo() != stationArrayList.size()) {//不是最後一站
                //下一站距離
                iNextDist = (int) Utility.calcDistance(lon, lat, tmpNextStation.longitude, tmpNextStation.latitude);
                sigStopDist = (int) Utility.calcDistance(tmpCurrStation.longitude, tmpCurrStation.latitude,
                        tmpNextStation.longitude, tmpNextStation.latitude);
//                this.stationInterface.debug(String.format("PND findNextStop Next=%s,%s, 兩站距=%s,.", tmpNextStation.zhName, iNextDist, sigStopDist));
            } else {
                iNextDist = iDist;
                sigStopDist = (int) Utility.calcDistance(stationArrayList.get(getiNowStationNo() - 2).longitude, stationArrayList.get(getiNowStationNo() - 2).latitude,
                        tmpCurrStation.longitude, tmpCurrStation.latitude);
//                this.stationInterface.debug(String.format("PND findNextStop Last=%s,%s, 兩站距=%s,.", stationArrayList.get(getiNowStationNo() - 2).zhName, iNextDist, sigStopDist));
            }

            int oStop = iStopArea2;
            int oLeave = iLeaveArea2;
            //站與站 之間距離  過近   以距離 比率計算進離站
            if (sigStopDist < iCheckStopArea) {
                iStopArea2 = sigStopDist / 3;
                iLeaveArea2 = sigStopDist / 3;
//                this.stationInterface.debug(String.format("PND 檢查距離(New)=%s, IN=%s(%s), OUT=%s(%s), B2=%s, %s,.", iNextDist, iStopArea2, oStop, iLeaveArea2, oLeave, sigStopDist, iCheckStopArea));
            } else {
//                this.stationInterface.debug(String.format("PND 檢查距離=%s, IN=%s, OUT=%s, B2=%s, %s,.", iNextDist, iStopArea2, iLeaveArea2, sigStopDist, iCheckStopArea));
            }

            show_debug_msg(String.format("CurrStation=%s,iDist=%s,iStopArea2=%s,iLeaveArea2=%s,iNextDist=%s,,sigStopDist=%s,.", tmpCurrStation.zhName, iDist, iStopArea2, iLeaveArea2, iNextDist,sigStopDist));

            if (bonArrival)
                this.stationInterface.debug(String.format("D,本站:%s(%s), 兩站距:%s, 檢查IN=%s(%s), OUT=%s(%s), Speed=%s,.", tmpCurrStation.zhName, filter(iDist), filter(sigStopDist), iStopArea2, oStop, iLeaveArea2, oLeave, sigKM2));
            else
                this.stationInterface.debug(String.format("D,下一站:%s(%s), 兩站距:%s, 檢查IN=%s(%s), OUT=%s(%s), Speed=%s,.", tmpNextStation.zhName, filter(iNextDist), filter(sigStopDist), iStopArea2, oStop, iLeaveArea2, oLeave, sigKM2));

            if (iNextDist < iStopArea2) {
                //到站
                bonNearStop = false;
                bonArrival = true;
                bonNextStop = true;
                bonGOLarrival = bonArrival;
                iRet = 1;//進站
                this.stationInterface.debug(String.format("IN,%s,%s,%s,%s,%s,%s,%s,%s,.1", lat, lon, angle, sigKM, tmpNextStation.id, tmpNextStation.zhName, iNextDist, iStopArea2));
                return iRet;
            } else {
                if (bonArrival) {
                    if (iNextDist < sigStopDist) {
                        if (iNextDist < iStopArea2) {//進站
                            bonNearStop = false;
                            bonArrival = true;
                            bonNextStop = true;
                            iRet = 1;
                            bonGOLarrival = bonArrival;
                            this.stationInterface.debug(String.format("IN,%s,%s,%s,%s,%s,%s,%s,%s,.2", lat, lon, angle, sigKM, tmpNextStation.id, tmpNextStation.zhName, iNextDist, iStopArea2));
                            return iRet;
                        }

                        if (iDist > iLeaveArea2) {
                            iRet = 2;//離站
                            bonArrival = false;
                            bonLeave = true;
                            bonNextStop = false;
                            this.stationInterface.debug(String.format("OUT,%s,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, angle, sigKM, tmpCurrStation.id, tmpCurrStation.zhName, iDist, iLeaveArea2, iStopArea2));
                        }
                    }
                } else {
                    if (iNextDist < iDist && !bonNearStop) {
                        iRet = 4;
                        bonNearStop = true;
                    } else
                        iRet = 0;
                }
            }

            if (iDist > (sigStopDist * 2)) {
                iLeaveStop = iLeaveStop + 1;
                if (iLeaveStop > 5) {
                    iLeaveStop = 0;
                    bonArrival = false;
                    bonLeave = false;
                    iRet = 3;//偏移路線
                    this.stationInterface.debug(String.format("W2,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, angle, sigKM, tmpCurrStation.id, tmpCurrStation.zhName, iDist, sigStopDist * 2));
                }
            } else {
                iLeaveStop = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bonGOLarrival = bonArrival;
        return iRet;
    }

    /**
     * 回傳最近站點1~N
     */
    private int getNowStationNo(GpsContent gpsContent) {
        // 依據 座標 找最近的站牌
        int j;
        int stNo;
        int dist;
        boolean getStation;
        int tmpDist;
        int nDist;
        double longitude, latitude;
        longitude = gpsContent.lon;
        latitude = gpsContent.lat;

        if (this.calTimes == 0){
            if (getiNowStationNo() <= 1)
                j = 0;
            else
                j = getiNowStationNo() - 1;
            stNo = 1;

            //先計算第一站距離
            dist = (int) Utility.calcDistance(longitude, latitude, currentRoad.stationArrayList.get(0).longitude, currentRoad.stationArrayList.get(0).latitude);
            if (dist < MAX_STOP_DIST && j == 0) {
                getStation = true;

    //            this.stationInterface.debug("PND GetNowStation S0= " + dist + ", calTime= " + this.calTimes);
            } else {
                getStation = false;
                try {
                    for (int i = j; i < currentRoad.stationArrayList.size(); i++) {
                        Station station = currentRoad.stationArrayList.get(i);
                        tmpDist = (int) Utility.calcDistance(longitude, latitude, station.longitude, station.latitude);
                        if (tmpDist < MAX_STOP_DIST) {
                            if (tmpDist < dist) {
                                stNo = i + 1;
                                dist = tmpDist;
                                getStation = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    getStation = false;
                    e.printStackTrace();
                }

                if (!getStation) {
                    try {
                        for (int i = 0; i < currentRoad.stationArrayList.size(); i++) {
                            Station station = currentRoad.stationArrayList.get(i);
                            tmpDist = (int) Utility.calcDistance(longitude, latitude, station.longitude, station.latitude);
                            if (tmpDist < MAX_STOP_DIST) {
                                if (tmpDist < dist) {
                                    stNo = i + 1;
                                    dist = tmpDist;
                                    getStation = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        getStation = false;
                        e.printStackTrace();
                    }
                }
            }
        }else{
            getStation = true;
            stNo = this.nowStNo;
        }
//        this.stationInterface.debug("PND GetNowStation S1= " + stNo + ", calTime= " + this.calTimes + ", getStation=" + getStation);

        // 沒有找到 最近的站牌
        if (!getStation) {
            stNo = 0;
            this.nowStNo = stNo;
            this.calTimes = 0;
        } else {
            // 有找到 , 再比對 是否接近下一站 , 比對五次
            if (stNo == 1) {
                // 找到第一站 為本站 或是 觸發站
                this.calTimes = 21;
                this.nowStNo = stNo;
                setiNowStationNo(stNo);
                duty_start = true;
            } else {
                if (stNo != nowStNo) {
                    //與前一次計算, 站牌有換
                    this.calTimes = 0;
                    this.nowStNo = stNo;
                } else {
                    //' 驗證是否接近 下一站 ,
                    setiNowStationNo(stNo-1);
                    this.checkNowStation(gpsContent);

                    if (bonStation_flag != 0 && bonStation_flag != 4) {//'接近下一站
                        this.calTimes = this.calTimes + 1;
                    } else {//'遠離下一站
                        if (this.calTimes < 0)
                            this.calTimes = 0;
                        stNo = 0;
                    }
                }
            }

            if (this.calTimes > 6) {
                //驗證 接近下一站
                this.calTimes = 0;
            } else {
                //尚未完成驗證 接近下一站
                stNo = 0;
            }
        }

        this.stationInterface.debug(String.format("PND GetNowStation lat=%s,lon=%s,stNo=%s,calltime=%s,isGetStation=%s,.", latitude, longitude, stNo, this.calTimes, getStation));
//        this.stationInterface.debug("PND GetNowStation SE= " + stNo + ", calTime= " + this.calTimes + ", getStation=" + getStation);
        return stNo;
    }

    /**
     * 第一次定位, 檢查是否在本站範圍內
     *
     */
    private boolean checkNowStation(GpsContent gpsContent) {
        int iNextDist = 0;//目前點位至下一站的距離
        int iStopArea = inRadius, iLeaveArea = outRadius;
        int iCheckStopArea = iStopArea + iLeaveArea + 50; // 檢查 兩站之間 距離是否過近
        double sigKM = gpsContent.getSpeed() * 1.852d; //' 海里 換算為 公里
        double sigKM2 = (sigKM * 1000) / 3600; // 公尺 / 每秒
        int iStopArea2 = (int) (iStopArea + sigKM2); // 進站距離  加入 速度因素
        int iLeaveArea2 = (int) (iLeaveArea - sigKM2); // 離站距離  加入 速度因素
        int sigStopDist = 0;//兩站之間的距離
        bonStation_flag = 0;

        ArrayList<Station> stationArrayList = currentRoad.stationArrayList;
        //本站
        Station tmpCurrStation = stationArrayList.get(getiNowStationNo() - 1);
        //下一站
        Station tmpNextStation = stationArrayList.get(getiNowStationNo());

        int iDist = (int) Utility.calcDistance(gpsContent.lon, gpsContent.lat, tmpCurrStation.longitude, tmpCurrStation.latitude);

        if (getiNowStationNo() != stationArrayList.size()) {//不是最後一站
            //下一站距離

            sigStopDist = (int) Utility.calcDistance(tmpCurrStation.longitude, tmpCurrStation.latitude,
                    tmpNextStation.longitude, tmpNextStation.latitude);
            iNextDist = (int) Utility.calcDistance(gpsContent.lon, gpsContent.lat,tmpNextStation.longitude, tmpNextStation.latitude);
//                this.stationInterface.debug(String.format("PND findNextStop Next=%s,%s, 兩站距=%s,.", tmpNextStation.zhName, iNextDist, sigStopDist));
        } else {
            sigStopDist = (int) Utility.calcDistance(stationArrayList.get(getiNowStationNo() - 2).longitude, stationArrayList.get(getiNowStationNo() - 2).latitude,
                    tmpCurrStation.longitude, tmpCurrStation.latitude);
            iNextDist = -1;
//                this.stationInterface.debug(String.format("PND findNextStop Last=%s,%s, 兩站距=%s,.", stationArrayList.get(getiNowStationNo() - 2).zhName, iNextDist, sigStopDist));
        }

        int oStop = iStopArea2;
        int oLeave = iLeaveArea2;
        //站與站 之間距離  過近   以距離 比率計算進離站
        if (sigStopDist < iCheckStopArea) {
            iStopArea2 = sigStopDist / 3;
            iLeaveArea2 = sigStopDist / 3;
        }

        this.stationInterface.debug(String.format("C2,接近:%s(%s), 兩站距:%s, 檢查IN=%s(%s), OUT=%s(%s), Speed=%s,.", tmpCurrStation.zhName, filter(iDist), filter(sigStopDist), iStopArea2, oStop, iLeaveArea2, oLeave, sigKM2));

        // 在站點範圍內
        if (iDist < iStopArea2) {
            this.stationInterface.debug(String.format("C3,判斷進站:%s(%s), 兩站距:%s", tmpCurrStation.zhName, filter(iDist), filter(sigStopDist)));
            bonStation_flag = 1;    //進入本站
            return true;
        }else{
            //離開本站、往下一站前進
            if (iDist > iLeaveArea2)
                if (iNextDist!=-1 && iNextDist< sigStopDist){
                    this.stationInterface.debug(String.format("C2,判斷離站:%s(%s), 兩站距:%s", tmpCurrStation.zhName, filter(iDist), filter(sigStopDist)));
                    bonStation_flag = 2;    //離開本站
                }
        }
        if (iDist < this.MAX_STOP_DIST){
            bonStation_flag = 3;            //還在判斷范圍內
        }else{
            if (iDist > this.MAX_STOP_DIST+200)
            bonStation_flag = 4;        //偏移路線
        }

        show_debug_msg(String.format("bonStation_flag=%s,iDist=%s,iStopArea2=%s,iLeaveArea2=%s,iNextDist=%s,,sigStopDist=%s,.", bonStation_flag, iDist, iStopArea2, iLeaveArea2, iNextDist,sigStopDist));

        return false;
    }

    public Station getCurrentStationForUI() {
        try {
            if (stationChecked && bonGOLarrival) {
                return currentRoad.stationArrayList.get(getiNowStationNo() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Station getPreviousStation() {
        try {
            if (stationChecked)
                return currentRoad.stationArrayList.get(getiNowStationNo() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Station getNextStationUI() {
        try {
            if (stationChecked && getiNowStationNo() < currentRoad.stationArrayList.size())
                return currentRoad.stationArrayList.get(getiNowStationNo());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取得現在位置
     */
    public Station getCurrentStationForSearch() {
        Station returnStation = null;

        try {
            if (stationChecked) {
                if (bonGOLarrival) {
                    returnStation = this.currentRoad.stationArrayList.get(getiNowStationNo() - 1).clone();
                } else {
                    returnStation = this.currentRoad.stationArrayList.get(getiNowStationNo()).clone();
                }
            }
        } catch (Exception e) {
            returnStation = null;
        }
        return returnStation;
    }

    public void setNextStation(int id) {
        try {
            setiNowStationNo(id);
            this.stationChecked = true;
            this.stationInterface.leaveStation(currentRoad.stationArrayList.get(getiNowStationNo()), last_gpsContent, false, currentRoad);
            this.stationInterface.debug(String.format("PND 手動設定=%s,.", getiNowStationNo()));
            bonGOLarrival = false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public Station getNextStation() {
        Station tmp = null;
        try {
            if (stationChecked)
                tmp = currentRoad.stationArrayList.get(getiNowStationNo());
        } catch (Exception e) {
            tmp = null;
        } finally {
        }
        return tmp;
    }
}
