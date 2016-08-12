package com.hantek.ttia.module.sqliteutils;

import android.content.Context;
import android.util.Log;

import com.hantek.ttia.module.forwardutils.ForwardMessage;

import java.util.ArrayList;

/**
 * Created by wsh on 2015/11/20.
 */
public class DBUpdater extends Thread {
    static final String TAG = DBUpdater.class.getName();

    private static DBUpdater ourInstance = new DBUpdater();

    private boolean running = false;
    private ArrayList<ForwardMessage> alComm1 = new ArrayList<>();
    private ArrayList<ForwardMessage> alComm2 = new ArrayList<>();
    private Context mContext;

    public static DBUpdater getInstance() {
        return ourInstance;
    }

    private DBUpdater() {
    }

    public synchronized void open(Context context) {
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

    public void addComm1(ForwardMessage message) {
        synchronized (alComm1) {
            alComm1.add(message);
            Log.d(TAG, "add Comm1: " + message.getSequence() + " Size:" + alComm1.size() + ", Comm2: " + message.getSequence() + " Size:" + alComm2.size());
        }
    }

    public void addComm2(ForwardMessage message) {
        synchronized (alComm2) {
            alComm2.add(message);
            Log.d(TAG, "add Comm1: " + message.getSequence() + " Size:" + alComm1.size() + ", Comm2: " + message.getSequence() + " Size:" + alComm2.size());
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10);
                if (alComm1.size() > 0) {
                    ArrayList<ForwardMessage> al;
                    synchronized (alComm1) {
                        al = new ArrayList<>(alComm1);
                        alComm1.clear();
                    }

                    int size = al.size();
                    for (int i = 0; i < size; i++) {
                        ForwardMessage tmpMsg = al.get(i);
                        DatabaseHelper.getInstance(mContext).updateTTIAAck(tmpMsg.getMsgID(), tmpMsg.getSequence());
                    }
//                    DatabaseHelper.getInstance(mContext).updateTTIAAckList(al);

                    Log.d(TAG, "updateTTIAAck: " + size);
                }

                if (alComm2.size() > 0) {
                    ArrayList<ForwardMessage> al;
                    synchronized (alComm2) {
                        al = new ArrayList<>(alComm2);
                        alComm2.clear();
                    }

                    int size = al.size();
                    for (int i = 0; i < size; i++) {
                        ForwardMessage tmpMsg = al.get(i);
                        DatabaseHelper.getInstance(mContext).updateHantekAck(tmpMsg.getMsgID(), tmpMsg.getSequence());
                    }
//                    DatabaseHelper.getInstance(mContext).updateHantekList(al);

                    Log.d(TAG, "updateHantekAck: " + size);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
