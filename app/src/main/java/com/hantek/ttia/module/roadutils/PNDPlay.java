package com.hantek.ttia.module.roadutils;

import android.util.Log;

import com.hantek.ttia.module.Utility;

import java.util.ArrayList;

/**
 * Created by HantekPC on 2016/5/6.
 */
public class PNDPlay extends RoadManager {

    final int MAX_STOP_DIST = 500;

    //Road currentRoad = null;
    boolean stationChecked = false;
    int iNowStationNo = 1;//目前 站牌序號   '從1開始 1~
    int iOldStatNo;
    int nowStNo;
    int calTimes;
    int nextDist;
    int iFindNextStop;
    boolean bonGOLarrival;

    @Override
    public void reset() {
        super.reset();
        this.stationInterface.debug(String.format("Reset=%s,%s,.", currentRoad.id, currentRoad.beginStation));
        stationChecked = false;
        setiNowStationNo(1); // iNowStationNo = 1;
    }

    public void checkStation(GpsContent gpsContent) {
        int iGetNo;
        this.stationInterface.debug(String.format("%s,%s,%s,%s,%s,%s,%s,.", gpsContent.isFixed ? "A" : "V", gpsContent.satelliteNumber, gpsContent.lon, gpsContent.lat, gpsContent.getAngle(), gpsContent.getSpeed(), gpsContent.time));

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

        if (gpsContent.isFixed) {
            // GPS 訊號正常
            if (getiNowStationNo() >= currentRoad.stationArrayList.size()) {
                //最後一站, 重新定位
                stationChecked = false;
                setiNowStationNo(1);
                this.stationInterface.debug(String.format("W6,%s,%s,%s,%s,.", gpsContent.lat, gpsContent.lon, gpsContent.getAngle(), gpsContent.getSpeed()));
            }

            //速度五以下 ,且不是在第一站, 且不是折返點 不判斷
            if ((gpsContent.getSpeed() * 1.852) < 5 && getiNowStationNo() > 1) {
                // this.stationInterface.debug(String.format("PND 速度五以下 ,且不是在第一站, 且不是折返點 不判斷, Origin=%s, KM=%s,.", gpsContent.getSpeed(), gpsContent.getSpeed() * 1.852d));
                this.stationInterface.debug(String.format("W5,%s,%s,%s,%s,%s,.", gpsContent.lat, gpsContent.lon, gpsContent.getAngle(), gpsContent.getSpeed(), gpsContent.getSpeed() * 1.852d));
                return;
            }

            if (!this.stationChecked) {
                iGetNo = getNowStationNo(gpsContent.lon, gpsContent.lat);
                if (iGetNo > 0) {
                    setiNowStationNo(iGetNo);
                    this.stationInterface.debug(String.format("C,%s,%s,%s,%s,%s,%s,.", gpsContent.lat, gpsContent.lon, gpsContent.getAngle(), gpsContent.getSpeed(), currentRoad.stationArrayList.get(getiNowStationNo() - 1).id, currentRoad.stationArrayList.get(getiNowStationNo() - 1).zhName));
                    //[START]第一次站點定位不會報進站, 只有報出站
                    //如果距離已經在站點內, 才當作已到達, 否則會誤報出站
                    if (checkNowStation(gpsContent.lon, gpsContent.lat, gpsContent.getSpeed(), gpsContent.getAngle())) {
                        bonNearStop = false;
                        bonArrival = true;
                        bonNextStop = true;
                        bonGOLarrival = bonArrival;
                        this.stationInterface.debug(String.format("CIN,%s,%s,%s,%s,%s,%s,.", gpsContent.lat, gpsContent.lon, gpsContent.getAngle(), gpsContent.getSpeed(), currentRoad.stationArrayList.get(getiNowStationNo() - 1).id, currentRoad.stationArrayList.get(getiNowStationNo() - 1).zhName));
                    }
                    //[END]

//                    if (this.iOldStatNo != getiNowStationNo()) {
//// TODO LED
//                    }
//                    this.stationInterface.enterStation(currentRoad.stationArrayList.get(getiNowStationNo() - 1), gpsContent, false, currentRoad);
                    this.stationChecked = true;
                } else
                    this.stationChecked = false;
            }

            this.iOldStatNo = getiNowStationNo();
            if (this.stationChecked) {
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

            if (bonArrival)
                this.stationInterface.debug(String.format("D,下一站:%s(%s), 兩站距:%s, 檢查IN=%s(%s), OUT=%s(%s), Speed=%s,.", tmpCurrStation.zhName, filter(iDist), filter(sigStopDist), iStopArea2, oStop, iLeaveArea2, oLeave, sigKM2));
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
    private int getNowStationNo(double longitude, double latitude) {
        // 依據 座標 找最近的站牌
        int j;
        int stNo;
        int dist;
        boolean getStation;
        int tmpDist;
        int nDist;

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
                this.nextDist = 0;
                this.nowStNo = stNo;
            } else {
                if (stNo != nowStNo) {
                    //與前一次計算, 站牌有換
                    this.calTimes = 0;
                    this.nextDist = 0;
                    this.nowStNo = stNo;
                    getStation = false;
                } else {
                    //' 驗證是否接近 下一站 ,
                    if (stNo == this.currentRoad.stationArrayList.size()) {
                        // 最後一站
                        stNo = 0;
                        getStation = false;
                        this.calTimes = 0;
                    } else {
                        Station station = this.currentRoad.stationArrayList.get(stNo);
                        nDist = (int) Utility.calcDistance(longitude, latitude, station.longitude, station.latitude);
                        if (nDist < this.nextDist) {//'接近下一站
                            this.calTimes = this.calTimes + 1;
                            getStation = false;
                            this.nowStNo = stNo;
                        } else {//'遠離下一站
                            getStation = false;
                            if (this.calTimes < 0)
                                this.calTimes = 0;
                            this.nowStNo = stNo;
                            stNo = 0;
                        }
                        this.nextDist = nDist;
                    }
                }
            }

            if (this.calTimes > 6) {
                //驗證 接近下一站
                stNo = stNo;
                getStation = true;
                this.calTimes = 0;
            } else {
                //尚未完成驗證 接近下一站
                stNo = 0;
                getStation = false;
            }
        }

        this.stationInterface.debug(String.format("N,%s,%s,%s,%s,%s,%s,.", latitude, longitude, stNo, this.calTimes, getStation, this.nextDist));
//        this.stationInterface.debug("PND GetNowStation SE= " + stNo + ", calTime= " + this.calTimes + ", getStation=" + getStation);
        return stNo;
    }

    /**
     * 第一次定位, 檢查是否在本站範圍內
     *
     * @param lon
     * @param lat
     * @param speed
     * @param angle
     * @return
     */
    private boolean checkNowStation(double lon, double lat, double speed, double angle) {
        int iNextDist;
        int iStopArea = inRadius, iLeaveArea = outRadius;
        int iCheckStopArea = iStopArea + iLeaveArea + 50; // 檢查 兩站之間 距離是否過近
        double sigKM = speed * 1.852d; //' 海里 換算為 公里
        double sigKM2 = (sigKM * 1000) / 3600; // 公尺 / 每秒
        int iStopArea2 = (int) (iStopArea + sigKM2); // 進站距離  加入 速度因素
        int iLeaveArea2 = (int) (iLeaveArea - sigKM2); // 離站距離  加入 速度因素
        int sigStopDist = 0;//兩站之間的距離

        ArrayList<Station> stationArrayList = currentRoad.stationArrayList;
        //本站
        Station tmpCurrStation = stationArrayList.get(getiNowStationNo() - 1);
        //下一站
        Station tmpNextStation = stationArrayList.get(getiNowStationNo());

        int iDist = (int) Utility.calcDistance(lon, lat, tmpCurrStation.longitude, tmpCurrStation.latitude);
        if (getiNowStationNo() != stationArrayList.size()) {//不是最後一站
            //下一站距離
            iNextDist = iDist;
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
        }

        this.stationInterface.debug(String.format("C2,下一站:%s(%s), 兩站距:%s, 檢查IN=%s(%s), OUT=%s(%s), Speed=%s,.", tmpCurrStation.zhName, filter(iDist), filter(sigStopDist), iStopArea2, oStop, iLeaveArea2, oLeave, sigKM2));

        // 在站點範圍內
        if (iNextDist < iStopArea2) {
            return true;
        }

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
