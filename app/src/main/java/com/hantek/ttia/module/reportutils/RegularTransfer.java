package com.hantek.ttia.module.reportutils;

import android.util.Log;

import com.hantek.ttia.module.Utility;
import com.hantek.ttia.protocol.a1a4.MonitorStructType1;
import com.hantek.ttia.protocol.a1a4.RegularReport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RegularTransfer extends Thread {
    static final String TAG = RegularTransfer.class.getName();

    static final int SLEEP = 10;
    static final int UPDATE_TYPE1_MS = 5000;
    static int REGULAR_REPORT_MS = 20000;

    private static RegularTransfer ourInstance = new RegularTransfer();

    // regular report data
    private ReadWriteLock monitorStructType1ArrayRWL = new ReentrantReadWriteLock();
    private ArrayList<MonitorStructType1> monitorStructType1ArrayList = new java.util.ArrayList<>();

    private IReport interfaces;
    private boolean running;
    private Calendar lastSendRegularReport = Calendar.getInstance();
    private Calendar lastGetMonitorData1Time = Calendar.getInstance();

    public static RegularTransfer getInstance() {
        return ourInstance;
    }

    private RegularTransfer() {
        this.setName("RegularTransfer");
    }

    public void setInterface(IReport interfaces) {
        this.interfaces = interfaces;
    }

    public void reset() {
        this.lastGetMonitorData1Time = Calendar.getInstance();
        this.lastSendRegularReport = Calendar.getInstance();
    }

    public void setTransfer(int tick) {
        REGULAR_REPORT_MS = tick * 1000;
        Log.d(TAG, "Report:" + tick);
    }

    public void open() {
        running = true;
        ourInstance.start();
    }

    public void close() {
        running = false;
        try {
            ourInstance.join(SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
            ourInstance.interrupt();
        }
    }

    public ArrayList<MonitorStructType1> getMonitorStructType1Array() {
        monitorStructType1ArrayRWL.readLock().lock();
        try {
            return this.monitorStructType1ArrayList;
        } finally {
            monitorStructType1ArrayRWL.readLock().unlock();
        }
    }

    public void addMonitorStructType1Array(MonitorStructType1 monitorStructType1Array) {
        monitorStructType1ArrayRWL.writeLock().lock();
        try {
            monitorStructType1ArrayList.add(monitorStructType1Array);
            if (monitorStructType1ArrayList.size() > 4)
                monitorStructType1ArrayList.remove(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            monitorStructType1ArrayRWL.writeLock().unlock();
        }
    }

    public void removeMonitorStructType1Array() {
        monitorStructType1ArrayRWL.writeLock().lock();
        try {
            monitorStructType1ArrayList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            monitorStructType1ArrayRWL.writeLock().unlock();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                // 配合華夏系統取消
                // 每 UPDATE_TYPE1_MS 取 monitor struct type1放到 systemPara array
//                double update = Utility.dateDiffNow(this.lastGetMonitorData1Time);
//                if (update >= UPDATE_TYPE1_MS) {
//                    this.lastGetMonitorData1Time = Calendar.getInstance();
//                    Log.d(TAG, "update regular report." + update);
//                    this.interfaces.updateReport();
//                    this.lastGetMonitorData1Time.add(Calendar.MILLISECOND, (int) (UPDATE_TYPE1_MS - update));
//                }
                // 每 REGULAR_REPORT_MS 傳送一次
                double send = Utility.dateDiffNow(this.lastSendRegularReport);
                if (send >= REGULAR_REPORT_MS) {
                    this.lastSendRegularReport = Calendar.getInstance();
                    Log.d(TAG, "send regular report." + send);
                    // 配合華夏系統調整
                    if (getMonitorStructType1Array().size() == 0)
                        this.interfaces.updateReport();
                    RegularReport report = new RegularReport();
                    report.monitorDataList = getMonitorStructType1Array();
                    report.monitorData = (byte) report.monitorDataList.size();
                    this.interfaces.report(report);
                    removeMonitorStructType1Array();
                    this.lastSendRegularReport.add(Calendar.MILLISECOND, (int) (REGULAR_REPORT_MS - send));
                }

                Thread.sleep(SLEEP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
