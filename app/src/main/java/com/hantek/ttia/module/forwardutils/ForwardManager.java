package com.hantek.ttia.module.forwardutils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.hantek.ttia.module.NetworkUtils;
import com.hantek.ttia.module.Utility;
import com.hantek.ttia.module.sqliteutils.DatabaseHelper;
import com.hantek.ttia.module.sqliteutils.PacketEntity;
import com.hantek.ttia.protocol.a1a4.BackendMsgID;
import com.hantek.ttia.protocol.a1a4.Header;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import component.LogManager;

public class ForwardManager implements Runnable {
    static final String TAG = ForwardManager.class.getName();
    static final int CHECK_MS = 100;
    static final int RETRY_MS = 2500;
    static final int ONE_TIME_MAX_SEND = 5;
    static final int MAX_SEND = 5;

    private Thread thread;
    private static ForwardManager instance = new ForwardManager(null);
    private List<ForwardMessage> forwardList = new ArrayList<>();
    private final Object forwardObj = new Object();

    private List<ForwardMessage> removeList = new ArrayList<>();
    private final Object removeObj = new Object();

    private Calendar lastCheckTime = Calendar.getInstance();
    private List<BackendMsgID> retryList = new ArrayList<>();

    private ForwardInterface forwardInterface;
    private boolean isForwardStart = false;
    private Context mContext;

    private boolean checking = false;
    private Calendar lastGetPacket = Calendar.getInstance();

    public static ForwardManager getInstance() {
        return instance;
    }

    public ForwardManager(Context context) {
        mContext = context;
        // 設定需要補傳的MessageID
//        retryList.add(BackendMsgID.RegularReport);
        retryList.add(BackendMsgID.EventReport);
//        retryList.add(BackendMsgID.DeviceAlarm);
    }

    public void setInterface(ForwardInterface forwardInterface) {
        this.forwardInterface = forwardInterface;
    }

    public boolean forwardContains(BackendMsgID msgID) {
        return retryList.contains(msgID);
    }

    public void open(Context context) {
        if (this.isForwardStart)
            return;

        mContext = context;
        this.isForwardStart = true;
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void close() {
        this.isForwardStart = false;
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            thread.interrupt();
        }
        thread = null;
        System.gc();
    }

    public int getForwardCount() {
        return this.forwardList.size();
    }

    public void add(ForwardMessage message) {
        synchronized (this.forwardObj) {
            // Log.d(TAG, "Add: " + message.toString());
            this.forwardList.add(message);
        }
    }

    public void remove(ForwardMessage message) {
        synchronized (this.removeObj) {
            // Log.d(TAG, "Remove: " + message.toString());
            this.removeList.add(message);
            checking = true;
        }
    }

    public void check(boolean useMobileData) {
        if (Utility.dateDiffNow(this.lastCheckTime) > CHECK_MS) {
            this.lastCheckTime = Calendar.getInstance();

            ArrayList<ForwardMessage> tmpRemoveArrayList = new ArrayList();
            synchronized (this.forwardObj) {
                int maxSendCount = ONE_TIME_MAX_SEND;
                for (int i = 0; i < this.forwardList.size(); i++) {
                    ForwardMessage msg = this.forwardList.get(i);
                    // Log.d(TAG, "Check: " + msg.toString());
                    Date d = msg.getSendTime();
                    int sendTime = msg.getSendCount() * RETRY_MS;
//                    if (sendTime == 0)
                    sendTime = RETRY_MS;
//                    else if (sendTime >= 300000)
//                        sendTime = 300000;

                    if ((new Date().getTime() - d.getTime()) > sendTime) {
                        maxSendCount -= 1;
                        msg.setSendTime(new Date());
                        if (useMobileData)
                            msg.addSendCount();

                        // Log.d(TAG, "Retry: " + msg.toString());
                        if (this.forwardInterface != null)
                            this.forwardInterface.retry(msg);

                        if (msg.getComm1Ack() == 0) {
                            if (msg.getSendComm1Count() > 2) {
                                ForwardMessage forwardMessage = new ForwardMessage(msg.getMsgID(), msg.getSequence(), null, 1, 0);
                                tmpRemoveArrayList.add(forwardMessage);
                            }
                        }

                        if (msg.getComm2Ack() == 0) {
                            if (msg.getSendComm2Count() > 2) {
                                ForwardMessage forwardMessage = new ForwardMessage(msg.getMsgID(), msg.getSequence(), null, 0, 1);
                                tmpRemoveArrayList.add(forwardMessage);
                            }
                        }
                    }

//                    if (msg.getSendCount() >= MAX_SEND) {
//                        Log.w(TAG, "Over max send: " + msg.toString());
//                        tmpRemoveArrayList.add(msg);
//                    }

                    if (maxSendCount <= 0) {
                        Log.w(TAG, "Over max send loop.");
                        break;
                    }
                }
            }

            if (tmpRemoveArrayList.size() > 0) {
                synchronized (this.removeObj) {
                    Log.w(TAG, "TODO Remove List=" + this.removeList.size());
                    this.removeList.addAll(tmpRemoveArrayList);
                    checking = true;
                }
            }
        }
    }

    @Override
    public void run() {
        DatabaseHelper.getInstance(mContext).deletePacket();

        loadPacket();

        while (this.isForwardStart) {
            try {
                Thread.sleep(100);

                if (NetworkUtils.isOnline(mContext)) {
                    check(true);

//                    if (Utility.dateDiffNow(this.lastGetPacket) > 60000) {
//                        this.lastGetPacket = Calendar.getInstance();
//                        if (ForwardManager.getInstance().getForwardCount() <= 10) {
//                            loadPacket();
//                        }
//                    }
                }

                if (checking) {
                    checking = false;
                    checkRemoveList();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int[] convertToIntArray(byte[] input, int size) {
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }

    private void loadPacket() {
        try {
            int count = DatabaseHelper.getInstance(mContext).countPacket();
            int countAll = DatabaseHelper.getInstance(mContext).count();
            LogManager.write("db", "Get total packet: " + count + " / " + countAll, null);

            if (count <= 10)
                return;

            Calendar sTime = Calendar.getInstance();
            Cursor cursor = DatabaseHelper.getInstance(mContext).getPacket(0, 100);
            if (cursor != null && cursor.getCount() >= 1) {
                load(cursor);
                cursor.close();
            }

            long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
            Log.d(TAG, String.format("LoadPacket EOT:%04d.", EOT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load(Cursor cursor) {
        if (cursor != null && cursor.getCount() >= 1) {
            cursor.moveToFirst();
            do {
                String hexString = cursor.getString(cursor.getColumnIndex(PacketEntity.MESSAGE)).trim();
                int sequence = cursor.getInt(cursor.getColumnIndex(PacketEntity.SEQ));
                int msgID = cursor.getInt(cursor.getColumnIndex(PacketEntity.MSGID));
                int ack = cursor.getInt(cursor.getColumnIndex(PacketEntity.ACK));
                int ack2 = cursor.getInt(cursor.getColumnIndex(PacketEntity.ACK2));

                byte[] data = Utility.hexStringToByteArray(hexString);
                int[] arr = convertToIntArray(data, data.length);

                // check customer id.
                Header h = Header.Parse(arr);
                if (h.customerID == 0) {
                    LogManager.write("db", "ignore msg:" + h.toString(), null);
                    return;
                }

                if (ack == 0 || ack2 == 0) {
                    ForwardMessage message = new ForwardMessage(msgID, sequence, data, ack, ack2);
                    ForwardManager.getInstance().add(message);
                }
            } while (cursor.moveToNext());
        }
    }

    private void checkRemoveList() {
        Calendar checkTime = Calendar.getInstance();
        int forwardCount = 0;
        int removeCount;
        synchronized (this.removeObj) {
            removeCount = this.removeList.size();
            for (int i = 0; i < removeCount; i++) {
                ForwardMessage removeMsg = this.removeList.remove(0);
                synchronized (this.forwardObj) {
                    forwardCount = this.forwardList.size();
                    for (int j = 0; j < forwardCount; j++) {
                        ForwardMessage tmpMsg = this.forwardList.get(j);
                        if (removeMsg.getMsgID() == tmpMsg.getMsgID() && removeMsg.getSequence() == tmpMsg.getSequence()) {
                            Calendar sTime = Calendar.getInstance();
                            if (removeMsg.getComm1Ack() == 1 && tmpMsg.getComm1Ack() == 0) {
                                tmpMsg.setComm1Ack();
//                                DBUpdater.getInstance().addComm1(tmpMsg);
                            } else if (removeMsg.getComm2Ack() == 1 && tmpMsg.getComm2Ack() == 0) {
                                tmpMsg.setComm2Ack();
//                                DBUpdater.getInstance().addComm2(tmpMsg);
                            }

                            if (tmpMsg.getComm2Ack() == 1 && tmpMsg.getComm1Ack() == 1) {
                                forwardList.remove(j);
                                long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
                                Log.d(TAG, String.format("Forward list remove:%s, EOT:%04d.", tmpMsg.toString(), EOT));
                                break;
                            }
                        }
                    }
                }
            }
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - checkTime.getTimeInMillis();
        Log.d(TAG, String.format("Forward list check remove:%s, forward:%s, EOT:%04d.", removeCount, forwardCount, EOT));
    }
}
