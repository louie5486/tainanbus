package com.hantek.ttia.module.roadutils;

import android.util.Log;

import com.hantek.ttia.module.Utility;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import component.LogManager;

/**
 * 站名播報
 */
public class RoadManager {
    static final String TAG = RoadManager.class.getName();

    private static final int UNFIXED_COUNT = 30;
    private static final int MAX_STOP_DISTANCE = 500;

    //20160920 Louie 改邏輯，改用物件 PNDPlay2
    private static RoadManager instance = new PNDPlay2();

    // 路線資料
    private final Object lockRoadListObj = new Object();
    private List<Road> roadArrayList = null;

    StationInterface stationInterface = null;

    private ReadWriteLock roadLock = new ReentrantReadWriteLock();
    /**
     * 目前選擇路線
     */
    Road currentRoad = null;

    private ReadWriteLock nextStationLock = new ReentrantReadWriteLock();

    private Station nextStation = null;

    /**
     * 站牌定位
     */
    private boolean stationChecked = false;
    private Station nextNextStation = null;

    private boolean onEndStation = false;// 結束:到最後一站

    private int gpsUnfixedCounter = 0;
    private double prevNextDistance = 0;
    private int countTime = 0;

    boolean arrive = false; // 進站旗標
    private boolean inRadiusConfirm = false;

    /**
     * 播報確認
     */
    private boolean inPlayConfirm = false;
    int outRadius = 50;
    int inRadius = 50;

    private GpsContent prevGpsContent = null;

    private Station prevNearestStation = null;
    private double prevNearestStationDistance = 0;
    private boolean leaving = false;

    private boolean delayPlay = false;

    public RoadManager() {

    }

    public static RoadManager getInstance() {
        return instance;
    }

    public void setInterface(StationInterface stationInterface) {
        this.stationInterface = stationInterface;
    }

    public void setRoadData(List<Road> list) {
        synchronized (this.lockRoadListObj) {
            this.roadArrayList = list;
        }
    }

    public List<Road> getLocalRoadData() {
        return this.roadArrayList == null ? new ArrayList<Road>() : this.roadArrayList;
    }

    public ArrayList<Road> queryRoad(int id, int direct, String branch) {
        ArrayList<Road> tmp = new ArrayList<>();
        synchronized (this.lockRoadListObj) {
            for (Road road : this.roadArrayList) {
                if (road.id == id) {
                    if (direct != -1) {
                        if (road.direct == direct) {
                            tmp.add(road);
                            Log.d(TAG, "Query=" + road.toString());
                        }
                    } else {
                        if (road.branch.equalsIgnoreCase(branch)) {
                            tmp.add(road);
                            Log.d(TAG, "Query=" + road.toString());
                        }
                    }
                }
            }
        }

        return tmp;
    }

    public ArrayList<Road> queryRoad(int id, int direct) {
        ArrayList<Road> tmp = new ArrayList<>();
        synchronized (this.lockRoadListObj) {
            for (Road road : this.roadArrayList) {
                if (road.id == id) {
                    if (direct != -1) {
                        if (road.direct == direct)
                            tmp.add(road);
                    } else
                        tmp.add(road);
                }
            }
        }

        return tmp;
    }

    /**
     * 選擇路線
     */
    public boolean setCurrentRoadID(int roadID, int direct, String branch) {
        pointList.clear();

        LogManager.write("debug", "Submit ID:" + roadID + " Direct:" + direct + " Branch:" + branch, null);
        boolean find = false;
        synchronized (this.lockRoadListObj) {
            for (Road road : this.roadArrayList) {
                if (road.id == roadID && road.direct == direct && road.branch.equalsIgnoreCase(branch)) {
                    try {
                        this.roadLock.writeLock().lock();
                        this.currentRoad = road;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        this.roadLock.writeLock().unlock();
                        find = true;
                    }
                    break;
                }
            }
        }

        return find;
    }

    public Road getCurrentRoad() {
        Road returnRoad = null;
//        this.roadLock.readLock().lock();
        try {
            returnRoad = this.currentRoad.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        this.roadLock.readLock().unlock();
        return returnRoad;
    }

    /**
     * 取得現在位置
     */
    public Station getCurrentStationForSearch() {
        Station returnStation;
        Station tmpStation = null;
        this.nextStationLock.readLock().lock();
        try {
            tmpStation = this.nextStation.clone();
        } catch (Exception e) {
        } finally {
            this.nextStationLock.readLock().unlock();
        }

        try {
            Station tmpCurrentStation = this.currentRoad.stationArrayList.get(tmpStation.id - 1).clone();
            if (arrive) {
                if (tmpStation.type == 0)
                    tmpStation = findPrevValidStation(tmpStation);
                returnStation = tmpStation;
            } else {
                if (tmpCurrentStation.type == 0)
                    tmpCurrentStation = findPrevValidStation(tmpCurrentStation);
                returnStation = tmpCurrentStation;
            }
        } catch (Exception e) {
            returnStation = null;
        }
        return returnStation;
    }

    public Station getCurrentStationForUI() {
        this.nextStationLock.readLock().lock();
        Station tmpStation = null;
        try {
            tmpStation = this.nextStation.clone();
        } catch (Exception e) {
        } finally {
            this.nextStationLock.readLock().unlock();
        }

        Station returnStation;
        try {
            if (tmpStation.type == 1) {
                if (arrive)
                    returnStation = tmpStation; // this.currentRoad.stationArrayList.get(tmpStation.id).clone();
                else
                    returnStation = null; // 不在站內，不顯示。
            } else {
                // 虛擬點
                returnStation = null; // 不顯示。
            }
        } catch (Exception e) {
            returnStation = null;
        }
        return returnStation;
    }

    public Station getPreviousStation() {
        this.nextStationLock.readLock().lock();
        Station tmpStation = null;
        try {
            tmpStation = this.nextStation.clone();
        } catch (Exception e) {
        } finally {
            this.nextStationLock.readLock().unlock();
        }

        Station returnStation;
        try {
            if (tmpStation.type == 1) {
                if (arrive)
                    returnStation = null; // this.currentRoad.stationArrayList.get(tmpStation.id).clone();
                else
                    returnStation = this.currentRoad.stationArrayList.get(tmpStation.id - 1).clone(); // 不在站內，不顯示。
            } else {
                // 虛擬點
                Station tmpCurrentStation = this.currentRoad.stationArrayList.get(tmpStation.id - 1).clone();
                returnStation = findPrevValidStation(tmpCurrentStation);
            }
        } catch (Exception e) {
            returnStation = null;
        }
        return returnStation;
    }

    public Station getNextStation() {
        Station tmp;
        this.nextStationLock.readLock().lock();
        try {
            tmp = this.nextStation.clone();
        } catch (Exception e) {
            tmp = null;
        } finally {
            this.nextStationLock.readLock().unlock();
        }
        return tmp;
    }

    public Station getNextStationUI() {
        Station returnStation;
        Station tmpStation = null;

        this.nextStationLock.readLock().lock();
        try {
            tmpStation = this.nextStation.clone();
        } catch (Exception e) {
        } finally {
            this.nextStationLock.readLock().unlock();
        }

        try {
            // 2016-01-27 修正最後一站不顯示, 確認是否為最後一站
            boolean isEnd = tmpStation.id == getCurrentRoad().stationArrayList.size() - 1;
            Station tmpNextStation;
            if (isEnd) {
                tmpNextStation = tmpStation;
            } else {
                tmpNextStation = currentRoad.stationArrayList.get(tmpStation.id + 1);
            }

            if (arrive) {
                if (tmpStation.type == 0)
                    returnStation = findNextValidStation(tmpNextStation);
                else {
                    if (isEnd) {
                        returnStation = new Station();
                    } else
                        returnStation = tmpNextStation.clone();
                }
            } else {
                if (tmpStation.type == 0)
                    returnStation = findPrevValidStation(tmpStation);
                else
                    returnStation = tmpStation;
            }
        } catch (Exception e) {
            returnStation = null;
        }

        return returnStation;
    }

    private void setNextStation(Station station) {
        this.nextStationLock.writeLock().lock();
        try {
            this.nextStation = station;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.nextStationLock.writeLock().unlock();
        }
    }

    public void setNextStation(int id) {
        this.nextStationLock.writeLock().lock();
        try {
            this.nextStation = currentRoad.stationArrayList.get(id);
            this.stationChecked = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.nextStationLock.writeLock().unlock();
        }
    }

    public void setOutRadius(int radius) {
        if (radius > 0)
            outRadius = radius;
        this.stationInterface.debug(String.format("OR,%s,.", outRadius));
    }

    public void setInRadius(int radius) {
        if (radius > 0)
            inRadius = radius;
        this.stationInterface.debug(String.format("IR,%s,.", inRadius));
    }

    public void reset() {
        prevNearestStationDistance = 0;
        this.prevNearestStation = null;
        this.onEndStation = false;
        this.inPlayConfirm = false;
        setNextStation(null);
        this.stationChecked = false;
    }

    private Date time;
    private GpsContent content;

    /**
     * 檢查是否有支線
     *
     * @param id
     * @param include0 不剔除主線
     * @return
     */
    public ArrayList<Road> hasBranch(int id, boolean include0) {
        ArrayList<String> branch = new ArrayList<>();
        ArrayList<Road> tmp = new ArrayList<>();
        synchronized (this.lockRoadListObj) {
            for (Road road : this.roadArrayList) {
                if (road.id == id) {
                    Log.d(TAG, "Include0=" + include0 + ", " + road.toString());
                    if (!branch.contains(road.branch)) {
                        if (include0)
                            tmp.add(road);
                        else if (!road.branch.equalsIgnoreCase("0")) {//剔除主線
                            tmp.add(road);
                        }
                    }
                    branch.add(road.branch);
                }
            }
        }

        return tmp;
    }

    public ArrayList<Road> getBranch(int id, String branchStr) {
        ArrayList<String> branch = new ArrayList<>();
        ArrayList<Road> tmp = new ArrayList<>();
        synchronized (this.lockRoadListObj) {
            for (Road road : this.roadArrayList) {
                if (road.id == id) {
                    Log.d(TAG, road.branch);
                    if (!branch.contains(road.branch)) {
                        if (road.branch.equalsIgnoreCase(branchStr)) {//剔除主線
                            tmp.add(road);
                        }
                    }
                    branch.add(road.branch);
                }
            }
        }

        return tmp;
    }

    public boolean checkDirect(int roadID, int direct, String branch) {
        boolean find = false;
        synchronized (this.lockRoadListObj) {
            for (Road road : this.roadArrayList) {
                if (road.id == roadID && road.direct == direct && road.branch.equalsIgnoreCase(branch)) {
                    find = true;
                    break;
                }
            }
        }

        return find;
    }

    public String checkVersion(int roadID, int direct, String branch) {
        String ver = "";
        for (Road road : this.roadArrayList) {
            if (road.id == roadID && road.direct == direct && road.branch.equalsIgnoreCase(branch)) {
                ver = "v" + String.valueOf(road.version);
                break;
            }
        }

        return ver;
    }

    public boolean toggle() {
        Road road = getCurrentRoad();
        int direct = (road.direct == 1 ? 2 : 1);
        boolean result = setCurrentRoadID(road.id, direct, road.branch);
        reset();
        return result;
    }

    public void checkStation(GpsContent data) {
        this.content = data;
        this.time = data.time;
        checkStation(data.lat, data.lon, data.isFixed);
        if (data.isFixed) {
            if (prevGpsContent == null) {
                prevGpsContent = data;
                return;
            }

            double distance = Utility.calcDistance(data.lon, data.lat, prevGpsContent.lon, prevGpsContent.lat);
            prevGpsContent = data;
            if (distance > 500)
                Log.w(TAG, "previous distance:" + distance);
        }
    }

    private void checkStation(double lat, double lon, boolean isFixed) {
        if (this.stationInterface == null) {
            Log.w(TAG, "null interface");
            return;
        }

        // 沒路線
        if (this.roadArrayList == null || this.roadArrayList.size() == 0) {
            return;
        }

        // 未選擇, 選擇其他路線
        if (this.currentRoad == null || this.currentRoad.id == 65535) {
            return;
        }

        try {
            if (!isFixed) {
                this.gpsUnfixedCounter += 1;
                if (this.gpsUnfixedCounter >= UNFIXED_COUNT) {
                    this.gpsUnfixedCounter = 0;
                    // GPS 無訊號
                    Log.d(TAG, "gps unfixed");
                }
                //2016-03-22
                this.stationInterface.debug(String.format("V,%s,%s,%s,%s,%s,%s,.", this.content.satelliteNumber, lon, lat, this.content.getAngle(), this.content.getSpeed(), time));
            } else {
                this.gpsUnfixedCounter = 0;
                playStation(lat, lon, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.write("error", e.toString(), null);
        }
    }

    ArrayList pointList = new ArrayList();
    static final int OFFSET_SIZE = 2;

    private void playStation(double lat, double lon, Date time) {
        // this.stationInterface.debug(String.format("*** 站名播報檢查(%s,%s) ***", lat, lon));
        this.stationInterface.debug(String.format("A,%s,%s,%s,%s,%s,%s,.", this.content.satelliteNumber, lon, lat, this.content.getAngle(), this.content.getSpeed(), time));

        // [START]平均位移過濾
        try {
            LanLng lanLng = new LanLng(lat, lon, time);
            pointList.add(lanLng);
            if (pointList.size() > OFFSET_SIZE) {
                pointList.remove(0);
            }

            if (calcGpsAvgOffset() >= 50) {
                this.stationInterface.debug(String.format("Offset,%s,%s,%s,%s,%s,%s,.", this.content.satelliteNumber, lon, lat, this.content.getAngle(), this.content.getSpeed(), time));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // [END]平均位移過濾

        // 站牌是否定位
        if (!this.stationChecked) {
            // 找最近站
            Station nearestStation = getNowStation(lat, lon, time, true);
            if (nearestStation != null) {
                if (this.prevNearestStation != null && this.prevNearestStation.id == nearestStation.id) {
                    // 判斷正在移動並接近下下一站
                    Station tmpNextStation = goesNextStation(lat, lon, nearestStation);
                    if (tmpNextStation != null) {
                        // 設定為已定位
                        this.stationChecked = true;
                        this.onEndStation = false;
                        setNextStation(nearestStation);

                        if (leaving) {
                            setNextStation(tmpNextStation);
                        } else {
                            setNextStation(nearestStation);
                        }

//                        this.stationInterface.debug(String.format("### 確認正在接近下一站:%s, %s. ###", getNextStation().zhName, getNextStation().id));
                        this.stationInterface.debug(String.format("C,%s,%s,%s%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), getNextStation().id, getNextStation().zhName));
                    }
                } else {
                    // 最近的站點變更了 do something...
                }
                this.prevNearestStation = nearestStation;
            } else {
                this.stationChecked = false;
                setNextStation(null);
            }
        }

        if (this.stationChecked) {
            //2016-02-22 加入時速判斷(m)
            double meterHour = (this.content.getSpeed() * 1.852d * 1000) / 3600d;
            int inRadiusWithSpeed = (int) (this.inRadius + meterHour);
            int outRadiusWithSpeed = (int) (this.outRadius + meterHour);

            Station tmpNext = nextStation.clone();
            Road tmpRoad = getCurrentRoad().clone();
            int tmpNextDistance = (int) Utility.calcDistance(tmpNext.longitude, tmpNext.latitude, lon, lat);

            // 是否最後一站
            if (tmpNext.id == getCurrentRoad().stationArrayList.size() - 1) {
                this.stationInterface.debug(String.format("*** 最後一站-%s,%s 距離:%s ***", tmpNext.id, tmpNext.zhName, tmpNextDistance));

                if (tmpNextDistance <= (inRadiusWithSpeed * 2) && !this.inPlayConfirm) {
                    // 進站
//                    this.stationInterface.debug(String.format("@!*** 進最後站 %s,%s,%s ***", tmpNext.id, tmpNext.zhName, tmpNextDistance));
                    this.stationInterface.debug(String.format("IN,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), tmpNext.id, tmpNext.zhName, tmpNextDistance, inRadiusWithSpeed));

                    // 移動中才播報, 反之則延遲.
                    if (this.content.getSpeed() * 1.852d > 5) {
                        delayPlay = false;
                        this.stationInterface.enterStation(tmpNext, this.content, true, tmpRoad);
                    } else
                        delayPlay = true;
//                     this.arrive = true;
                    this.inPlayConfirm = true;
                    this.inRadiusConfirm = false;
                } else if (tmpNextDistance <= inRadiusWithSpeed && !this.arrive && !this.inRadiusConfirm) {
//                    this.stationInterface.debug(String.format("@*** 進本站2 %s %s %s %s ***", getTime(time), tmpNext.id, tmpNext.zhName, tmpNextDistance));
//                    this.stationInterface.debug(String.format("!即將進站2: %s,%s.(%s)", tmpNext.id, tmpNext.zhName, tmpNextDistance));
                    this.stationInterface.debug(String.format("IN2,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), tmpNext.id, tmpNext.zhName, tmpNextDistance, inRadiusWithSpeed));
                    this.inRadiusConfirm = true;
                    this.arrive = true;
                    if (delayPlay) {
                        delayPlay = false;
                        this.stationInterface.enterStation(tmpNext, this.content, true, tmpRoad);
                    }
                } else if (((tmpNextDistance >= outRadiusWithSpeed && this.inRadiusConfirm) || tmpNextDistance > outRadiusWithSpeed) && this.arrive) {
                    // 離站
//                    this.stationInterface.debug(String.format("@!*** 離最後站 %s,%s,%s ***", tmpNext.id, tmpNext.zhName, tmpNextDistance));
                    this.stationInterface.debug(String.format("OUT,%s,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), tmpNext.id, tmpNext.zhName, tmpNextDistance, outRadiusWithSpeed, inRadiusWithSpeed));
                    if (this.nextNextStation != null)
                        this.stationInterface.debug(String.format("@!*** 最後離站, 下一站 %s,%s,%s ***", nextNextStation.id, nextNextStation.zhName, tmpNextDistance));

                    this.stationInterface.leaveStation(tmpNext, this.content, true, this.currentRoad);
                    this.arrive = false;
                    inPlayConfirm = false;
                    this.inRadiusConfirm = false;
                    this.onEndStation = true;
                } else {
                    if (this.onEndStation) {
                        this.stationChecked = false;
                        setNextStation(null);
                    }
                }

                return;
            }

            Station prevStation;
            double prevDistance = 0;
            double prevStationDistance = 0;
            if (tmpNext.id != 0) {
                prevStation = tmpRoad.stationArrayList.get(tmpNext.id - 1);
                prevDistance = Utility.calcDistance(tmpNext.longitude, tmpNext.latitude, prevStation.longitude, prevStation.latitude);
                prevStationDistance = Utility.calcDistance(tmpNext.longitude, tmpNext.latitude, lon, lat);
            }

            Station tmpNextNextStation = tmpRoad.stationArrayList.get(tmpNext.id + 1);
            double nextDistance = Utility.calcDistance(tmpNextNextStation.longitude, tmpNextNextStation.latitude, lon, lat);
            double stationDistance = Utility.calcDistance(tmpNext.longitude, tmpNext.latitude, tmpNextNextStation.longitude, tmpNextNextStation.latitude);

            this.stationInterface.debug(String.format("D,下一站:%s(%s), 下下一站:%s(%s), 兩站距:%s, 前站距:%s.", tmpNext.zhName, filter(tmpNextDistance), tmpNextNextStation.zhName, filter(nextDistance), filter(stationDistance), prevDistance));

            if (tmpNextDistance <= (inRadiusWithSpeed * 2) && !this.inPlayConfirm) {
                // 進站
//                this.stationInterface.debug(String.format("@*** 進本站 %s %s %s %s ***", getTime(time), tmpNext.id, tmpNext.zhName, tmpNextDistance));
//                this.stationInterface.debug(String.format("!即將進站: %s,%s.(%s)", tmpNext.id, tmpNext.zhName, tmpNextDistance));
                this.stationInterface.debug(String.format("IN,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), tmpNext.id, tmpNext.zhName, tmpNextDistance, inRadiusWithSpeed));

                // 移動中才播報, 反之則延遲.
                if (this.content.getSpeed() * 1.852d > 5) {
                    delayPlay = false;
                    this.stationInterface.enterStation(getNextStation(), this.content, false, tmpRoad);
                } else
                    delayPlay = true;
//                this.arrive = true;
                inPlayConfirm = true;
                this.inRadiusConfirm = false;
//            }else if (nextDistance < this.inRadius && !this.arrive) {
//                // 進下下站
//                this.stationInterface.debug(String.format("@*** 進下一站 %s %s %s %s ***", getTime(time), nextNextStation.id, nextNextStation.zhName, nextDistance));
//                this.stationInterface.debug(String.format("!即將進站(Next): %s,%s.", nextNextStation.id, nextNextStation.zhName));
//                this.stationInterface.enterStation(nextNextStation, this.content);
//                this.arrive = true;
//                setCurrentStation(nextNextStation); // 設定本站
//            }
            } else if (tmpNextDistance <= inRadiusWithSpeed && !this.arrive && !this.inRadiusConfirm) {
//                this.stationInterface.debug(String.format("@*** 進本站2 %s %s %s %s ***", getTime(time), tmpNext.id, tmpNext.zhName, tmpNextDistance));
//                this.stationInterface.debug(String.format("!即將進站2: %s,%s.(%s)", tmpNext.id, tmpNext.zhName, tmpNextDistance));
                this.stationInterface.debug(String.format("IN2,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), tmpNext.id, tmpNext.zhName, tmpNextDistance, inRadiusWithSpeed));
                this.inRadiusConfirm = true;
                this.arrive = true;
                if (delayPlay) {
                    delayPlay = false;
                    this.stationInterface.enterStation(getNextStation(), this.content, false, tmpRoad);
                }
            } else if (((tmpNextDistance >= outRadiusWithSpeed && this.inRadiusConfirm) || tmpNextDistance > outRadiusWithSpeed) && this.arrive) {
//            } else if (tmpNextDistance >= this.outRadius && this.arrive) {// 2016-02-16 modify
                // 離站
                Station tmpNearSta = this.getNowStation(lat, lon, time, false);
                int currID = tmpNext.id;
                if (tmpNearSta != null && (tmpNearSta.id == currID || tmpNearSta.id == currID + 1)) {
//                    this.stationInterface.debug(String.format("@*** 離本站 %s %s %s %s ***", getTime(time), tmpNext.id, tmpNext.zhName, tmpNextDistance));
//                    this.stationInterface.debug(String.format("!下一站: %s,%s.(%s)", nextStation.id, nextStation.zhName, tmpNextDistance));
                    this.stationInterface.debug(String.format("OUT,%s,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), tmpNext.id, tmpNext.zhName, tmpNextDistance, outRadiusWithSpeed, inRadiusWithSpeed));
                    setNextStation(tmpNext.id + 1); // 設定下一站
                    this.stationInterface.leaveStation(getNextStation(), this.content, false, tmpRoad);
                } else {
                    this.inPlayConfirm = false;
                    this.stationChecked = false;
                    setNextStation(null);
//                    this.stationInterface.debug(String.format("@!*** Warning 重新定位,%s. ***", currID));
                    this.stationInterface.debug(String.format("W,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), currID));
                }
                this.arrive = false;
                inPlayConfirm = false;
                this.inRadiusConfirm = false;
//            } else if (prevDistance > stationDistance && tmpNextDistance > stationDistance * (prevDistance / stationDistance)) {
//                // 偏移
////                this.stationInterface.debug("@!*** 偏移 A>C*N ***" + prevDistance / stationDistance);
//                this.stationInterface.debug(String.format("W1,%s,%s,%s,%s,%s,.", lat, lon, prevDistance, tmpNextDistance, stationDistance));
//                this.stationChecked = false;
//                setNextStation(null);
            } else if (prevDistance > 0 && prevStationDistance >= prevDistance * 3) { // tmpNextDistance >= prevDistance * 3) {
                //2016-02-24 目前與前站的距離 > 前站到下站距離的3倍
                // 偏移
//                this.stationInterface.debug("@!*** 偏移 A>C*3 ***");
                this.stationInterface.debug(String.format("W2,%s,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), tmpNext.id, tmpNext.zhName, prevDistance, tmpNextDistance));
                reset();
            } else if (prevDistance > 0 && tmpNextDistance <= prevDistance / 2) {
//                this.stationInterface.debug("*** 進站, LED不做動作 A<C/2 ***");
                this.stationInterface.debug(String.format("W3,%s,%s,%s,%s,%s,%s,%s,.", lat, lon, this.content.getAngle(), this.content.getSpeed(), prevDistance, tmpNextDistance, stationDistance));
            }
        }
    }

    /**
     * 尋找路線中距離目前GPS座標最近的站牌
     */
    private Station getNowStation(double latitude, double longitude, Date time, boolean flag) {
        Road road = getCurrentRoad();
        Station tmpStation = null;
        double distance = MAX_STOP_DISTANCE;
        Station nearStation = road.stationArrayList.size() > 0 ? road.stationArrayList.get(0) : null;
        double nearStationDistance = nearStation != null ? Utility.calcDistance(longitude, latitude, nearStation.longitude, nearStation.latitude) : 0;
        Queue<Station> debugStation = new LinkedList<>();
        for (Station station : road.stationArrayList) {
            double tmpDistance = Utility.calcDistance(longitude, latitude, station.longitude, station.latitude);
            if (tmpDistance < distance) {
                distance = tmpDistance;
                tmpStation = station;

                debugStation.offer(station);
                if (debugStation.size() > 5) {
                    debugStation.poll();
                }
            }

            if (tmpDistance < nearStationDistance) {
                nearStationDistance = tmpDistance;
                nearStation = station;
            }
        }

        // 紀錄最近5站
        for (Station station : debugStation) {
            double tmpDistance = Utility.calcDistance(longitude, latitude, station.longitude, station.latitude);
            this.stationInterface.debug(String.format("N5,%s,%s,%s,%s,%s,%s,.", latitude, longitude, station.id, station.zhName, filter(tmpDistance), getTime(time)));
        }

        // 檢查距離變化，是離開還是駛近。
        if (tmpStation != null && !this.stationChecked) {
            if (prevNearestStationDistance != 0) {
                leaving = distance > prevNearestStationDistance;
            }
            prevNearestStationDistance = distance;
        }

        if (flag)
            return tmpStation;
        else
            return nearStation;
    }

    /**
     * 判斷車子是否持續接近下一站
     */
    private Station goesNextStation(double latitude, double longitude, Station nearestStation) {
        Road tmpRoad = currentRoad;
        if (nearestStation.id + 1 == tmpRoad.stationArrayList.size()) {
            // 已在最後一站
            return null;
        }

        Station tmpNextStation = tmpRoad.stationArrayList.get(nearestStation.id + 1);

        double tmpDistance = Utility.calcDistance(longitude, latitude, tmpNextStation.longitude, tmpNextStation.latitude);
        if (tmpDistance < prevNextDistance) {
            countTime += 1;
        } else {
            countTime = 0;
        }
        prevNextDistance = tmpDistance;

        if (countTime >= 6) {
            countTime = 0;
            return tmpNextStation;
        } else {
            return null;
        }
    }

    private String getTime(Date time) {
        return String.format(" (%1$tH:%1$tM:%1$tS)", time);
    }

    String filter(double d) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(d);
    }

    private String filterLocation(double d) {
        DecimalFormat df = new DecimalFormat("###.######");
        return df.format(d);
    }

    private Station findNextValidStation(Station station) {
        try {
            Road road = getCurrentRoad();
            int id = station.id;
            Station tmpStation;
            do {
                tmpStation = road.stationArrayList.get(id++);
            } while (tmpStation.type == 0);
            return tmpStation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Station findPrevValidStation(Station station) {
        try {
            Road road = getCurrentRoad();
            int id = station.id;
            Station tmpStation;
            do {
                tmpStation = road.stationArrayList.get(id--);
            } while (tmpStation.type == 0);
            return tmpStation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected double calcGpsAvgOffset() {
        double avg = 0;
        if (pointList.size() > 1) {
//            Log.d(TAG, "AVG=" + avg);
            LanLng lanLng0 = (LanLng) pointList.get(0);
            LanLng lanLng1 = (LanLng) pointList.get(1);
            double distance = Utility.calcDistance(lanLng0.lon, lanLng0.lat, lanLng1.lon, lanLng1.lat);
            double time = (lanLng1.time.getTime() - lanLng0.time.getTime()) / 1000;
            avg = distance / time;
        }

        return avg;
    }
}
