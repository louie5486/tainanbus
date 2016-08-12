package com.hantek.ttia.module;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;

import component.LogManager;

public class AccTimer extends Thread {
    static final String TAG = AccTimer.class.getName();

    private static AccTimer ourInstance = new AccTimer();

    private Context mContext;
    private boolean running = false;
    private int tick;
    private IAccListener accListener;

    // acc
    private Calendar lastAccCheck = Calendar.getInstance();
    private int shutdownCounter = 0;
    private int SHUTDOWN_COUNT = 60;
    private static final int SHUTDOWN_CHECK_MS = 1000;
    private boolean accStatus = false;

    private Calendar lastAccOffTime = Calendar.getInstance();

    boolean initPowerStatus;
    boolean initDelay;

    public static AccTimer getInstance() {
        return ourInstance;
    }

    private AccTimer() {
        this.setName("AccTimer");
    }

    public void open(Context context) {
        if (running)
            return;

        mContext = context;
        running = true;
        ourInstance.start();
    }

    public void close() {
        running = false;
        try {
            ourInstance.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            ourInstance.interrupt();
        }
    }

    public void setAcc(int tick) {
        this.SHUTDOWN_COUNT = tick;
        LogManager.write("debug", "acc,set:" + SHUTDOWN_COUNT + ",.", null);
    }

    public void setInterfaces(IAccListener listener) {
        this.accListener = listener;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10);

                if (!initPowerStatus) {
                    initPowerStatus = DioController.getInstance().powerOnDisable();
                    if (initPowerStatus)
                        Log.d(TAG, "Acc Init Power command success.");
                }

//                if (!initDelay) {
//                    initDelay = DioController.getInstance().powerTurnOff(1);
//                    if (initDelay)
//                        Log.d(TAG, "Acc Delay command success.");
//                }
                checkAcc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAcc() {
        try {
            if (Utility.dateDiffNow(this.lastAccCheck) >= SHUTDOWN_CHECK_MS) {
                this.lastAccCheck = Calendar.getInstance();
                boolean newState = DioController.getInstance().getAccStatus(true);
                if (this.accStatus != newState) {
                    Log.d(TAG, "Acc change~~");
                    if (!newState) {
                        Log.d(TAG, "Acc Off send powerTurnOff");
                        int minute = SHUTDOWN_COUNT / 60;
                        if (minute <= 0)
                            minute = 1;
                        DioController.getInstance().powerTurnOff(minute);
                        this.accListener.sendShutdown();
                        this.lastAccOffTime = Calendar.getInstance();
                    }
                }
                this.accStatus = newState;
                if (!this.accStatus && Utility.dateDiffNow(this.lastAccOffTime) >= (SHUTDOWN_COUNT * 0.5 * 1000)) {
                    this.lastAccOffTime = Calendar.getInstance();
                    LogManager.write("debug", "acc,timeout:" + SHUTDOWN_COUNT + ",.", null);

                    String cradle = DioController.getInstance().getCradleMcuVersion();
                    String tablet = DioController.getInstance().getTabletMcuVersion();

                    LogManager.write("debug", "acc,ct," + cradle + "," + tablet + ",.", null);
                    if (cradle == null || cradle.equalsIgnoreCase("error") || cradle.equalsIgnoreCase("null")) {
                        DioController.getInstance().shutdown(mContext);
                    }
                    if (tablet == null || tablet.equalsIgnoreCase("error") || tablet.equalsIgnoreCase("null")) {

                    }
                }
//                if (!DioController.getInstance().getAccStatus()) {
//                    shutdownCounter += 1;
//                    Log.d(TAG, "Acc counter: " + shutdownCounter);
//                } else {
//                    shutdownCounter = 0;
//                }
//                if (shutdownCounter >= SHUTDOWN_COUNT) {
//                    shutdownCounter = 0;
//                    this.accListener.sendShutdown();
//                    this.accListener.shutdown();
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.write("error", "acc,fail," + e.toString() + ",.", null);
        }
    }
}
