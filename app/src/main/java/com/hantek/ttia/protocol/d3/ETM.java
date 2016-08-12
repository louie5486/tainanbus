package com.hantek.ttia.protocol.d3;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

import com.hantek.ttia.comms.DataListener;
import com.hantek.ttia.comms.PhysicalInterface;
import com.hantek.ttia.module.BitConverter;
import com.hantek.ttia.module.Utility;

/**
 * 電子票證模組, Baud Rate=115,200 bps，Stop bit= 1，Databits= 8，Parity Bit= N。
 */
public class ETM implements DataListener, ETMInterface, Runnable {
    private static final String TAG = ETM.class.getName();

    public static final int ACK = 0x06;
    public static final int NACK = 0x15;

    private static final int DLE = 0x10;
    private static final int STX = 0x02;
    private static final int ETX = 0x03;

    private static ETM etm = new ETM();
    private List<Integer> dataArrayList = new ArrayList<Integer>();
    private PhysicalInterface comm;
    private ETMListener dataReceive;
    private boolean onReceiving = false;

    // response process
    private boolean isStart = false;
    private Thread timeOutThread = null;
    private int waittingMsgID = -1; /* 同一時間只處理一筆 */
    private Calendar lastReqTime = Calendar.getInstance();
    private long waittingSeconds = 0;

    // varible
    private int sequenceNo = 0;

    public static ETM getInstance() {
        return etm;
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
        this.comm.setListener(etm);
        isStart = true;
        timeOutThread = new Thread(etm);
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
    public void onDataReceived(byte[] rawData, int size) {
        Calendar startTime = Calendar.getInstance();
        this.onReceiving = true;
        synchronized (this.dataArrayList) {
            for (int i = 0; i < size; i++) {
                dataArrayList.add(rawData[i] & 0xff); // Range 0 to 255, not -128 to 127
            }

            try {
                boolean pktStart = false;
                ArrayList<Integer> msgBuffer = new ArrayList<Integer>();
                int pktBufferIndex = 0;
                int checksum = 0;
                int pktLen = 0;
                int count = dataArrayList.size();
                int removeIndex = -1;
                int DLEcount = 0;
                boolean getDLE = false;
                int startIndex = -1;
                int tmpLen = 0;

                for (int i = 0; i < count; i++) {
                    int data = dataArrayList.get(i);

                    if (!pktStart && data == STX && !getDLE) {
                        pktStart = true;
                        pktBufferIndex = 0;
                        startIndex = i;
                        continue;
                    }

                    if (pktStart && data == ETX && i > (startIndex + 9 + tmpLen) && !getDLE) {
                        pktStart = false;
                        pktBufferIndex = 0;
                    }

                    // ignore DLE
                    if (pktLen > 0 && data == DLE && !getDLE) {
                        getDLE = true;
                        DLEcount += 1;
                        pktLen -= 1;
                        continue;
                    }

                    if (getDLE)
                        getDLE = false;

                    // checksum
                    if (!pktStart && msgBuffer.size() > 0) {
                        int originLen = (msgBuffer.get(0x06) + (msgBuffer.get(0x07) << 8)) - DLEcount;

                        int[] intArrayList = new int[msgBuffer.size() + 1];
                        intArrayList[0] = STX;// 補回STX
                        checksum ^= STX;
                        for (int j = 0; j < msgBuffer.size(); j++) {
                            data = msgBuffer.get(j);

                            // replace data length
                            if (j == 6) {
                                data = originLen & 0xff;
                            } else if (j == 7) {
                                data = originLen >> 8;
                            }

                            intArrayList[j + 1] = data;

                            if (j != msgBuffer.size() - 1) {
                                checksum ^= intArrayList[j + 1];
                            }
                        }

                        msgBuffer.clear();

                        if (checksum == data) {
                            // GET FULL DATA
                            Log.d(TAG, "ETM data received");
                            this.analysisFullPacket(intArrayList);
                        } else {
                            Log.d(TAG, "ETM data received wrong checksum");
                        }

                        removeIndex = i;
                        checksum = 0;
                        DLEcount = 0;
                        continue;
                    }

                    // 取得長度
                    if (pktBufferIndex == 6) {
                        pktLen += data;
                    } else if (pktBufferIndex == 7) {
                        pktLen += (data << 8);
                        tmpLen = pktLen;
                    }

                    if (pktBufferIndex > 7 && pktLen > 0) {
                        pktLen -= 1;
                    }

                    if (pktStart) {
                        pktBufferIndex += 1;
                        msgBuffer.add(data);
                    }
                } // end for

                if (removeIndex != -1) {
                    // for (int k = 0; k <= removeIndex; k++) {
                    // dataArrayList.remove(0);
                    // }

                    // 從後面開始remove
                    Calendar rmTime = Calendar.getInstance();
                    for (int i = removeIndex; i >= 0; i--) {
                        dataArrayList.remove(i);
                        if (i % 10000 == 0) {
                            long eot = Utility.dateDiffNow(rmTime);
                            rmTime = Calendar.getInstance();
                            Log.d(TAG, String.format("ETM remove index:%s EOT:%s", i, eot));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.dataArrayList.clear();
            }
        }

        Log.d(TAG, "ETM data received EOT:" + Utility.dateDiffNow(startTime) + " DataList:" + dataArrayList.size());
        this.onReceiving = false;
    }

    // start-------------------- ETM protocol --------------------

    @Override
    public boolean authenRequest(int operatorAuthenType, int operatorType, long operatorID, String password) {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(ETMMsgID.AuthenResult.getValue(), 1000);
        AuthenRequest data = new AuthenRequest(operatorAuthenType, operatorType, operatorID, password);
        data.setSeqNo(getSeqNo());
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean startRequestGpsInfoAck(int result) {
        StartRequestGpsInfoAck data = new StartRequestGpsInfoAck(result);
        data.setSeqNo(getSeqNo());
        return this.comm.send(data.getBytes());
    }

    @Override
    public boolean sendGpsInfo(GpsInfo gpsInfo) {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(ETMMsgID.SendGpsInfoAck.getValue(), 1000);
        SendGpsInfo data = new SendGpsInfo(gpsInfo);
        data.setSeqNo(getSeqNo());
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean stopRequestGpsInfoAck(int result) {
        StopRequestGpsInfoAck data = new StopRequestGpsInfoAck(result);
        data.setSeqNo(getSeqNo());
        return this.comm.send(data.getBytes());
    }

    @Override
    public boolean uploadDataReq(int dataID, Encryption encryptionFlag) {
        UploadDataReq data = new UploadDataReq(dataID, encryptionFlag);
        data.setSeqNo(getSeqNo());
        return this.comm.send(data.getBytes());
    }

    @Override
    public boolean uploadDataAck(int result, int serialNumber) {
        UploadDataAck data = new UploadDataAck(result, serialNumber);
        data.setSeqNo(getSeqNo());
        return this.comm.send(data.getBytes());
    }

    @Override
    public boolean downloadDataReq(int dataID, Encryption encryptionFlag) {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(ETMMsgID.DataDownloadReady.getValue(), 1000);
        DownloadDataReq data = new DownloadDataReq(dataID, encryptionFlag);
        data.setSeqNo(getSeqNo());
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean downloadData(int dataID, int serialNumber, int lastMessage, byte[] dataByte) {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(ETMMsgID.DownloadDataAck.getValue(), 1000);
        DownloadData data = new DownloadData(dataID, serialNumber, lastMessage, dataByte);
        data.setSeqNo(getSeqNo());
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean startODDataReq(Encryption encryptionFlag, int reportFlag) {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(ETMMsgID.StartODDataAck.getValue(), 1000);
        StartODDataReq data = new StartODDataReq(encryptionFlag, reportFlag);
        data.setSeqNo(getSeqNo());
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean stopODDataReq() {
        if (this.waittingMsgID != -1) {
            return false;
        }

        this.setWaittingResponse(ETMMsgID.StopODDataAck.getValue(), 1000);
        StopODDataReq data = new StopODDataReq();
        data.setSeqNo(getSeqNo());
        boolean result = this.comm.send(data.getBytes());
        if (!result) {
            this.waittingMsgID = -1;
        }

        return result;
    }

    @Override
    public boolean ODDataAck(int result) {
        ODDataAck data = new ODDataAck(result);
        data.setSeqNo(getSeqNo());
        return this.comm.send(data.getBytes());
    }

    // -------------------- ETM protocol --------------------end

    public void setInterface(PhysicalInterface comm) {
        this.comm = comm;
    }

    public void setListener(ETMListener listener) {
        this.dataReceive = listener;
    }

    public boolean isWaitting() {
        return this.waittingMsgID == -1 ? false : true;
    }

    public void clearBuffer() {
        Log.w(TAG, String.format("ETM waitting time out msgID: 0x%02X, sec: %s", this.waittingMsgID & 0xff, this.waittingSeconds));

        try {
            synchronized (this.dataArrayList) {
                this.dataArrayList.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.waittingMsgID = -1;
        Log.w(TAG, "ETM time out clear buffer: " + this.dataArrayList.size());
    }

    private synchronized void setWaittingResponse(int command, int waittingSeconds) {
        this.lastReqTime = Calendar.getInstance();
        this.waittingSeconds = waittingSeconds;
        this.waittingMsgID = command;
    }

    private int getSeqNo() {
        if (this.sequenceNo > 65535)
            this.sequenceNo = 0;

        return this.sequenceNo++;
    }

    /**
     * 輸入:原始資料(StartCode ... )
     *
     * @param intArray 原始傳送封包資料內容
     */
    private void analysisFullPacket(int[] intArray) {
        try {
            int command = intArray[1];
            if (this.waittingMsgID == command) {
                Log.d(TAG, String.format("ETM get waitting command..0x%02X", this.waittingMsgID));
                this.waittingMsgID = -1;
            } else {
                if (this.waittingMsgID != -1) {
                    Log.w(TAG, String.format("ETM wrong sequence.0x%02X", command));
                    return;
                }
            }

            long len = BitConverter.toUShort(intArray, 7);

            Message data = null;
            if (command == ETMMsgID.AuthenResult.getValue()) {
                data = AuthenResult.parse(intArray);

            } else if (command == ETMMsgID.StartRequestGpsInfo.getValue()) {
                data = StartRequestGpsInfo.parse(intArray);

            } else if (command == ETMMsgID.SendGpsInfoAck.getValue()) {
                data = SendGpsInfoAck.parse(intArray);

            } else if (command == ETMMsgID.StopRequestGpsInfo.getValue()) {
                data = StopRequestGpsInfo.parse(intArray);

            } else if (command == ETMMsgID.UploadData.getValue()) {
                data = UploadData.parse(intArray);

            } else if (command == ETMMsgID.DataDownloadReady.getValue()) {
                data = DataDownloadReady.parse(intArray);

            } else if (command == ETMMsgID.DownloadDataAck.getValue()) {
                data = DownloadDataAck.parse(intArray);

            } else if (command == ETMMsgID.StartODDataAck.getValue()) {
                data = StartODDataAck.parse(intArray);

            } else if (command == ETMMsgID.StopODDataAck.getValue()) {
                data = StopODDataAck.parse(intArray);

            } else if (command == ETMMsgID.ODData.getValue()) {
                ODData d = new ODData();
                data = d;
                try {
                    data = ODData.parse(intArray);
                    // TODO
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String s = "";
                for (int b : intArray) {
                    s = s + String.format("%2X", b & 0xff) + " ";
                }
                Log.e(TAG, s);
            }

            data.setLastMessage(intArray[4]);
            data.setIDStorage(intArray[5]);
            Log.d(TAG, String.format("ETM receive cmd=0x%02X, len=%s, EOT:%s", command & 0xff, len, Utility.dateDiffNow(lastReqTime)));
            if (data != null) {
                if (this.dataReceive != null) {
                    dataReceive.onETMDataReceived(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
