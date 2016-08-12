package com.hantek.ttia.services;

import com.hantek.ttia.dl.DownloadReceiver;
import com.hantek.ttia.module.roadutils.Road;

import java.util.ArrayList;

public interface IService {

    boolean isConfiguration();

    boolean isDown();

    boolean checkRegisterResponse();

    boolean download(DownloadReceiver receiver);

    /**
     * 司機登入
     *
     * @param driverID
     * @return
     */
    boolean login(String driverID);

    /**
     * 司機登出
     *
     * @return
     */
    boolean logoff();

    /**
     * 系統檢查
     *
     * @return
     */
    boolean systemCheck();

    /**
     * 變更路線
     *
     * @param roadID
     * @param direct
     * @return
     */
    boolean changeRoad(int roadID, int direct, String branch);

    /**
     * 查詢路線
     *
     * @return
     */
    ArrayList<Road> queryRoad(int roadID, int direct);

    /**
     * 發車
     *
     * @return
     */
    boolean startWork(boolean manually);

    /**
     * 非營運
     *
     * @return
     */
    boolean stopWork();

    /**
     * 變更行車狀態
     *
     * @param status
     * @return
     */
    boolean changeBusStatus(int status);

    /**
     * 變更站序
     *
     * @param stationID
     * @return
     */
    boolean changeStation(int stationID);

    /**
     * 變更方向
     *
     * @param direct
     * @return
     */
    boolean changeDirect(int direct);

    /**
     * 變更勤務狀態
     *
     * @param status
     * @return
     */
    boolean changeDutyStatus(int status);

    /**
     * 訊息回覆
     */
    boolean replyNotifyMessage(int infoID, int reportType);

    /**
     * 重發車
     *
     * @return
     */
    boolean secondGo();

    int getGpsStatus();

    int getAccStatus();

    int getNetworkStatus();

    int getComm1Status();

    int getComm2Status();

    int getSignalStrengths();

    int getDI1();

    int getDI2();

    int getttyUSB1();

    int getttyUSB2();

    int getttyUSB3();

    int getAdvertFileSize();
}
