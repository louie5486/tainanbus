package com.hantek.ttia;

import com.hantek.ttia.protocol.a1a4.BusStatus;
import com.hantek.ttia.protocol.a1a4.DutyStatus;
import com.hantek.ttia.protocol.a1a4.Header;
import com.hantek.ttia.protocol.a1a4.RegisterResponse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.hantek.ttia.protocol.a1a4.Message;

import component.BestLocationListener;
import component.gps_test;

public class SystemPara {
    private static SystemPara parameter = new SystemPara();

    /* 車機製造商 代號 */
    private static final byte manufacturer = (byte) 255;

    public byte getManufacturer() {
        return manufacturer;
    }

    private String version_code;

    public BestLocationListener bastLocation = new BestLocationListener();
    public gps_test GPS = bastLocation.getGps();

    // header
    private String protocolID;
    private int protocolVer;
    private int customerID;
    private int carID;
    private int idStorage;
    private long driverID = 0;

    // logon request information
    private String IMSI;
    private String IMEI;

    private ReadWriteLock registerResponseRWL = new ReentrantReadWriteLock();
    private RegisterResponse registerResponse = null;

    private Header header = null;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header h) {
        header = h;
    }

    public RegisterResponse getRegisterResponse() {
        registerResponseRWL.readLock().lock();
        try {
            return registerResponse;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            registerResponseRWL.readLock().unlock();
        }

        return null;
    }

    public void setRegisterResponse(RegisterResponse registerResponse) {
        registerResponseRWL.writeLock().lock();
        try {
            this.registerResponse = registerResponse;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            registerResponseRWL.writeLock().unlock();
        }
    }

    // monitor struct
    private ReadWriteLock rpmArrayRWL = new ReentrantReadWriteLock();
    private ArrayList<Integer> rpmArray = new ArrayList<Integer>();
    private ReadWriteLock intSpeedArrayRWL = new ReentrantReadWriteLock();
    private ArrayList<Integer> intSpeedArray = new ArrayList<Integer>();

    private int avgSpeed;
    private ReadWriteLock currentDutyStatusRWL = new ReentrantReadWriteLock();
    private DutyStatus currentDutyStatus = DutyStatus.Ready;
    private ReadWriteLock currentBusStatusRWL = new ReentrantReadWriteLock();
    private BusStatus currentBusStatus = BusStatus.OnRoad;
    private long mileage;

    // notify message data
    private ReadWriteLock notifyMessageRWL = new ReentrantReadWriteLock();
    private Queue<Message> notifyMessageQueue = new LinkedList<Message>();

    private ReadWriteLock currentRPMRWL = new ReentrantReadWriteLock();
    private int currentRPM;

    private ReadWriteLock currentSpeedRWL = new ReentrantReadWriteLock();
    private int currentSpeed;

    private int infoID;
    private boolean fireCar;

    SystemPara() {
        this.protocolID = "APTS";
        this.protocolVer = 0x02;
        this.customerID = 0;
        this.carID = 0;
        this.idStorage = 0;
        this.IMEI = "";
        this.IMSI = "";
    }

    public static SystemPara getInstance() {
        return parameter;
    }

    public String getProtocolID() {
        return protocolID;
    }

    public int getProtocolVer() {
        return protocolVer;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getCarID() {
        return carID;
    }

    public void setCarID(int carID) {
        this.carID = carID;
    }

    public int getIdStorage() {
        return idStorage;
    }

    public void setIdStorage(int idStorage) {
        this.idStorage = idStorage;
    }

    /**
     * driver input
     */
    public long getDriverID() {
        return driverID;
    }

    public void setDriverID(long driverID) {
        this.driverID = driverID;
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String iMSI) {
        IMSI = iMSI;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String iMEI) {
        IMEI = iMEI;
    }

    public int[] getRpmArray() {
        rpmArrayRWL.readLock().lock();
        try {
            int[] tmpInt = new int[20];
            for (int i = 0; i < rpmArray.size(); i++) {
                tmpInt[i] = rpmArray.get(i);
            }
            return tmpInt;
        } finally {
            rpmArrayRWL.readLock().unlock();
        }
    }

    public void setRpmArray(int rpm) {
        rpmArrayRWL.writeLock().lock();
        try {
            this.rpmArray.add(rpm);
            if (this.rpmArray.size() > 20)
                this.rpmArray.remove(0);
        } finally {
            rpmArrayRWL.writeLock().unlock();
        }
    }

    public int[] getIntSpeedArray() {
        intSpeedArrayRWL.readLock().lock();
        try {
            int[] tmpInt = new int[20];
            for (int i = 0; i < intSpeedArray.size(); i++) {
                tmpInt[i] = intSpeedArray.get(i);
            }
            return tmpInt;
        } finally {
            intSpeedArrayRWL.readLock().unlock();
        }
    }

    // 採DCR 前20秒速度
    public void setIntSpeedArray(int intSpeed) {
        intSpeedArrayRWL.writeLock().lock();
        try {
            this.intSpeedArray.add(intSpeed);
            if (this.intSpeedArray.size() > 20)
                this.intSpeedArray.remove(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            intSpeedArrayRWL.writeLock().unlock();
        }
    }

    public int getAvgSpeed() {
        return avgSpeed;
    }

    // TODO 採DCR之速度
    public void setAvgSpeed(int avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public DutyStatus getCurrentDutyStatus() {
        currentDutyStatusRWL.readLock().lock();
        try {
            return currentDutyStatus;
        } finally {
            currentDutyStatusRWL.readLock().unlock();
        }
    }

    public void setCurrentDutyStatus(DutyStatus currentDutyStatus) {
        currentDutyStatusRWL.writeLock().lock();
        try {
            this.currentDutyStatus = currentDutyStatus;
        } finally {
            currentDutyStatusRWL.writeLock().unlock();
        }
    }

    public BusStatus getCurrentBusStatus() {
        currentBusStatusRWL.readLock().lock();
        try {
            return currentBusStatus;
        } finally {
            currentBusStatusRWL.readLock().unlock();
        }
    }

    public void setCurrentBusStatus(BusStatus currentBusStatus) {
        currentBusStatusRWL.writeLock().lock();
        try {
            this.currentBusStatus = currentBusStatus;
        } finally {
            currentBusStatusRWL.writeLock().unlock();
        }
    }

    public long getMileage() {
        return mileage;
    }

    public void setMileage(long mileage) {
        this.mileage = mileage;
    }

    public int getNotifyMessageQueueSize() {
        notifyMessageRWL.readLock().lock();
        try {
            return notifyMessageQueue.size();
        } finally {
            notifyMessageRWL.readLock().unlock();
        }
    }

    public Message getNotifyMessageQueue() {
        notifyMessageRWL.readLock().lock();
        try {
            return notifyMessageQueue.poll();
        } finally {
            notifyMessageRWL.readLock().unlock();
        }
    }

    public void addNotifyMessageQueue(Message notifyMessage) {
        notifyMessageRWL.writeLock().lock();
        try {
            this.notifyMessageQueue.offer(notifyMessage);
        } finally {
            notifyMessageRWL.writeLock().unlock();
        }
    }

    public int getCurrentRPM() {
        currentRPMRWL.readLock().lock();
        try {
            return currentRPM;
        } finally {
            currentRPMRWL.readLock().unlock();
        }
    }

    public void setCurrentRPM(int currentRPM) {
        currentRPMRWL.writeLock().lock();
        try {
            this.currentRPM = currentRPM;
        } finally {
            currentRPMRWL.writeLock().unlock();
        }
    }

    public int getCurrentSpeed() {
        currentSpeedRWL.readLock().lock();
        try {
            return currentSpeed;
        } finally {
            currentSpeedRWL.readLock().unlock();
        }
    }

    public void setInfoID(int infoID) {
        this.infoID = infoID;
    }

    public int getInfoID() {
        return infoID;
    }

    public void setFireCar(boolean fire) {
        fireCar = fire;
    }

    public boolean getFireCar() {
        return fireCar;
    }

    /**
     * DCR速度或GPS速度
     *
     * @param currentSpeed
     */
    public void setCurrentSpeed(int currentSpeed) {
        currentSpeedRWL.writeLock().lock();
        try {
            this.currentSpeed = currentSpeed;
        } finally {
            currentSpeedRWL.writeLock().unlock();
        }
    }

    // 異常移動距離
    private double moveTotalMileage = 0;

    public void resetAlarmMileage() {
        this.moveTotalMileage = 0;
    }

    public void setAlarmMileage(double mile) {
        this.moveTotalMileage += mile;
    }

    public double getAlarmTotalMileage() {
        return this.moveTotalMileage;
    }

    private int psdReconnect = 0;
    private int packetSendCounter = 0;
    private int packetReceiveCounter = 0;
    private int gpsCounter = 0;
    private int gpsFixedCounter = 0;

    public void addReconnectCounter() {
        psdReconnect++;
        if (psdReconnect > 65535)
            psdReconnect = 1;
    }

    public void addSendPacketCounter() {
        this.packetSendCounter++;
    }

    public void addReceivePacketCounter() {
        this.packetReceiveCounter++;
    }

    public void addGpsCounter() {
        gpsCounter++;
    }

    public void addGpsFixedCounter() {
        this.gpsFixedCounter++;
    }

    /**
     * 數據連線重建次數
     */
    public int getPSDReconnect() {
        return psdReconnect;
    }

    /**
     * 訊息傳送成功比例
     */
    public int getPacketRatio() {
        double receive = (double) this.packetReceiveCounter;
        double send = (double) this.packetSendCounter;
        if (receive > send)
            return 100;

        return (int) ((receive / send) * 100);
    }

    /**
     * 開機期間GPS Active比例
     */
    public int getGPSRatio() {
        double activeGPS = (double) this.gpsFixedCounter;
        double totalGPS = (double) this.gpsCounter;
        if (activeGPS > totalGPS)
            return 100;

        return (int) ((activeGPS / totalGPS) * 100);
    }

    public String getVersion_code() {
        return version_code;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }
    public gps_test getGPS() {
        return GPS;
    }

    public BestLocationListener getBastLocation() {
        return bastLocation;
    }

    public void setBastLocation(BestLocationListener bastLocation) {
        this.bastLocation = bastLocation;
    }
}
