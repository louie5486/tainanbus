package com.hantek.ttia.protocol.e2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

import com.hantek.ttia.comms.DataListener;
import com.hantek.ttia.comms.PhysicalInterface;
import com.hantek.ttia.module.BitConverter;
import com.hantek.ttia.module.Utility;

/**
 * 數位行車紀錄器, Baud Rate=115,200 bps，Stop bit= 1，Databits= 8，Parity Bit= N。
 */
public class DCR implements DataListener, DCRInterface, Runnable {
    private static final String TAG = DCR.class.getName();

    public static final int ACK = 0x06;
    public static final int NACK = 0x15;

    private static final int DLE = 0x10;
    private static final int STX = 0x02;
    private static final int ETX = 0x03;

    private static DCR dcr = new DCR();
    private PhysicalInterface comm;
    private DCRListener dataReceive;
    private List<Integer> dataArrayList = new ArrayList<Integer>();
    private boolean onReceiving = false;

    // 存最後一筆
    private BasicData lastBasicData = null;
    private DrivingData lastDrivingData = null;
    private InstantSpeed lastInstantSpeed = null;
    private HighSpeed lastHighSpeed = null;
    private TiredDriving lastTiredDriving = null;
    private ReadIntSpeed lastReadIntSpeed = null;
    private DriverRest lastDriverRest = null;

    public BasicData getLastBasicData() {
        return lastBasicData;
    }

    private void setLastBasicData(BasicData lastBasicData) {
        this.lastBasicData = lastBasicData;
    }

    public DrivingData getLastDrivingData() {
        return lastDrivingData;
    }

    private void setLastDrivingData(DrivingData lastDrivingData) {
        this.lastDrivingData = lastDrivingData;
    }

    public InstantSpeed getLastInstantSpeed() {
        return lastInstantSpeed;
    }

    private void setLastInstantSpeed(InstantSpeed lastInstantSpeed) {
        this.lastInstantSpeed = lastInstantSpeed;
    }

    public HighSpeed getLastHighSpeed() {
        return lastHighSpeed;
    }

    private void setLastHighSpeed(HighSpeed lastHighSpeed) {
        this.lastHighSpeed = lastHighSpeed;
    }

    public TiredDriving getLastTiredDriving() {
        return lastTiredDriving;
    }

    private void setLastTiredDriving(TiredDriving lastTiredDriving) {
        this.lastTiredDriving = lastTiredDriving;
    }

    public ReadIntSpeed getLastReadIntSpeed() {
        return lastReadIntSpeed;
    }

    private void setLastReadIntSpeed(ReadIntSpeed lastReadIntSpeed) {
        this.lastReadIntSpeed = lastReadIntSpeed;
    }

    public DriverRest getLastDriverRest() {
        return lastDriverRest;
    }

    private void setLastDriverRest(DriverRest lastDriverRest) {
        this.lastDriverRest = lastDriverRest;
    }

    // response process
    private boolean isStart = false;
    private Thread timeOutThread = null;
    private int waittingMsgID = -1; /* 同一時間只處理一筆 */
    private Calendar lastReqTime = Calendar.getInstance();
    private long waittingSeconds = 0;

    public static DCR getInstance() {
        return dcr;
    }

    @Override
    public void run() {
        while (isStart) {
            try {
                Thread.sleep(500);
                if (this.waittingMsgID != -1) {
                    if (Utility.dateDiffNow(this.lastReqTime) > this.waittingSeconds) {
                        clearBuffer();
                    }
                } else {
                    if (!this.onReceiving && this.dataArrayList.size() > 0) {
                        this.clearBuffer();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean open(String options) {
        this.comm.setListener(dcr);
        isStart = true;
        timeOutThread = new Thread(dcr);
        timeOutThread.start();
        return this.comm.open(options);
    }

    @Override
    public boolean close() {
        isStart = false;
        if (this.timeOutThread != null) {
            try {
                this.timeOutThread.join(1000);
            } catch (InterruptedException e) {
                this.timeOutThread.interrupt();
            }
        }

        boolean result = this.comm.close();

        this.dataArrayList.clear();
        this.waittingMsgID = -1;

        return result;
    }

    @Override
    public boolean isOpen() {
        return this.comm != null && this.comm.isOpen();
    }

    @Override
    public Calendar getLastReceiveTime() {
        return this.comm.getLastReceiveTime();
    }

    @Override
    public void onDataReceived(byte[] buffer, int size) {
        Calendar startTime = Calendar.getInstance();
        this.onReceiving = true;
        synchronized (this.dataArrayList) {
            for (int i = 0; i < size; i++) {
                this.dataArrayList.add(buffer[i] & 0xff); // Range 0 to 255, not -128 to 127
            }

            try {
                boolean msgStart = false;
                boolean pktStart = false;
                ArrayList<Integer> msgBuffer = new ArrayList<Integer>();
                int pktBufferIndex = 0;
                int calcBBC = 0;
                int pktLen = 0;
                int count = dataArrayList.size();
                int removeIndex = -1;

                for (int i = 0; i < count; i++) {
                    int data = dataArrayList.get(i);

                    if (!msgStart && data == DLE) {
                        msgStart = true;
                        pktStart = false;
                        continue;
                    }

                    if (!pktStart && data == STX && pktLen == 0 && msgBuffer.size() == 0) {
                        pktStart = true;
                        pktBufferIndex = 0;
                        continue;
                    }

                    if (pktStart && data == ETX && pktLen == 0) {
                        pktStart = false;
                        pktBufferIndex = 0;
                        msgBuffer.add(data); // ETX 加入 checksum
                        continue;
                    }

                    // BBC
                    if (!pktStart && msgBuffer.size() > 0) {
                        int[] intArrayList = new int[msgBuffer.size() + 2]; // 補回DLE, STX
                        intArrayList[0] = DLE;
                        intArrayList[1] = STX;
                        for (int j = 0; j < msgBuffer.size(); j++) {
                            intArrayList[j + 2] = msgBuffer.get(j);
                            calcBBC ^= intArrayList[j + 2];
                        }

                        msgBuffer.clear();

                        if (calcBBC == data) {
                            // GET FULL DATA
                            Log.d(TAG, "DCR GET FULL DATA.");
                            this.analysisFullPacket(intArrayList);
                        } else {
                            Log.d(TAG, "DCR data received wrong BBC");
                        }

                        msgStart = false;
                        calcBBC = 0;
                        removeIndex = i;
                        continue;
                    }

                    // 取得長度
                    if (pktBufferIndex == 2) {
                        pktLen += data;
                    } else if (pktBufferIndex == 3) {
                        pktLen += (data << 8);
                    } else if (pktBufferIndex == 4) {
                        pktLen += (data << 16);
                    } else if (pktBufferIndex == 5) {
                        pktLen += (data << 24);
                        if (dataArrayList.size() < pktLen + 8) {
                            break; // buffer長度不足
                        }
                    }

                    if (pktBufferIndex > 5 && pktLen > 0) {
                        pktLen -= 1;
                    }

                    if (pktStart) {
                        pktBufferIndex += 1;
                        msgBuffer.add(data);
                    }
                } // end for

                if (removeIndex != -1) {
                    // 從後面開始remove
                    Calendar rmTime = Calendar.getInstance();
                    for (int i = removeIndex; i >= 0; i--) {
                        dataArrayList.remove(i);
                        if (i % 10000 == 0) {
                            long eot = Utility.dateDiffNow(rmTime);
                            rmTime = Calendar.getInstance();
                            Log.d(TAG, String.format("DCR remove index:%s EOT:%s", i, eot));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.dataArrayList.clear();
            }
        }

        Log.d(TAG, "DCR data received EOT:" + Utility.dateDiffNow(startTime) + " DataList:" + this.dataArrayList.size());
        this.onReceiving = false;
    }

    // start-------------------- DCR protocol --------------------

    @Override
    public boolean basicDataReq() {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(DCRMsgID.BasicData.getValue(), 1000);
        BasicData data = new BasicData();
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean drivingDataReq() {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(DCRMsgID.DrivingData.getValue(), 40 * 1000);
        DrivingData data = new DrivingData();
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean instantSpeedReq() {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(DCRMsgID.InstantSpeed.getValue(), 60 * 1000);
        InstantSpeed data = new InstantSpeed();
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean highSpeedReq() {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(DCRMsgID.HighSpeed.getValue(), 1000);
        HighSpeed data = new HighSpeed();
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean tiredDrivingReq() {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(DCRMsgID.TiredDriving.getValue(), 1000);
        TiredDriving data = new TiredDriving();
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean readIntSpeedReq(int seconds) throws Exception {
        if (this.waittingMsgID != -1) {
            return false;
        }

        if (seconds > 10 * 60 * 60 || seconds < 0) {
            return false;
            // throw new Exception("out of range: 0 second to 10 hours");
        }

        int sec = seconds * 5;
        if (sec < 1000) {
            sec = 1000; // MIN waitting sec.
        }
        this.setWaittingResponse(DCRMsgID.ReadIntSpeed.getValue(), sec);
        ReadIntSpeed data = new ReadIntSpeed(seconds);
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean driverRestReq() {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(DCRMsgID.DriverRest.getValue(), 1000);
        DriverRest data = new DriverRest();
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean reportGPS(ReportGps data) {
        // ReportGps data = new ReportGps(gpsTime, lon, lat, angle, speed, fixed);
        // Log.d("DCR gps", data.toString());
        return this.comm.send(data.getBytes());
    }

    // -------------------- DCR protocol --------------------end

    public void setInterface(PhysicalInterface comm) {
        this.comm = comm;
    }

    public void setListener(DCRListener listener) {
        this.dataReceive = listener;
    }

    public boolean isWaitting() {
        return this.waittingMsgID == -1 ? false : true;
    }

    public void clearBuffer() {
        Log.w(TAG, String.format("DCR waitting time out msgID: %02Xh, sec: %s", this.waittingMsgID & 0xff, this.waittingSeconds));

        try {
            synchronized (this.dataArrayList) {
                this.dataArrayList.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.waittingMsgID = -1;
        Log.w(TAG, String.format("DCR time out clear buffer: %s", this.dataArrayList.size()));
    }

    private synchronized void setWaittingResponse(int command, int waittingSeconds) {
        this.lastReqTime = Calendar.getInstance();
        this.waittingSeconds = waittingSeconds;
        this.waittingMsgID = command;
        Log.w(TAG, String.format("DCR set Waitting %02Xh sec:%s", this.waittingMsgID, this.waittingSeconds));
    }

    /**
     * @param intArray 原始傳送封包資料內容
     */
    private void analysisFullPacket(int[] intArray) {

        try {
            int command = intArray[2];
            if (this.waittingMsgID == command) {
                Log.d(TAG, String.format("DCR get waitting command..0x%02X", this.waittingMsgID));
                this.waittingMsgID = -1;
            } else {
                // if (this.waittingMsgID != -1) {
                Log.w(TAG, String.format("DCR wrong sequence.0x%02X", command));
                return;
                // }
            }

            int state = intArray[3];
            long len = BitConverter.toUInteger(intArray, 4);
            Message data = null;
            if (command == DCRMsgID.BasicData.getValue()) {
                this.setLastBasicData(BasicData.parse(intArray));
                data = this.lastBasicData;
            } else if (command == DCRMsgID.DrivingData.getValue()) {
                this.setLastDrivingData(DrivingData.parse(intArray));
                data = this.lastDrivingData;
            } else if (command == DCRMsgID.InstantSpeed.getValue()) {
                this.setLastInstantSpeed(InstantSpeed.parse(intArray));
                data = this.lastInstantSpeed;
            } else if (command == DCRMsgID.HighSpeed.getValue()) {
                this.setLastHighSpeed(HighSpeed.parse(intArray));
                data = this.lastHighSpeed;
            } else if (command == DCRMsgID.TiredDriving.getValue()) {
                this.setLastTiredDriving(TiredDriving.parse(intArray));
                data = this.lastTiredDriving;
            } else if (command == DCRMsgID.ReadIntSpeed.getValue()) {
                this.setLastReadIntSpeed(ReadIntSpeed.parse(intArray));
                data = this.lastReadIntSpeed;
            } else if (command == DCRMsgID.DriverRest.getValue()) {
                this.setLastDriverRest(DriverRest.parse(intArray));
                data = this.lastDriverRest;
            }

            Log.d(TAG, String.format("DCR receive cmd=%02Xh, sta=%s, len=%s, EOT:%s", command & 0xff, state, len, Utility.dateDiffNow(lastReqTime)));
            if (data != null) {
                if (this.dataReceive != null) {
                    dataReceive.onDCRDataReceived(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
