package com.hantek.ttia.debug;

import com.hantek.ttia.comms.DataListener;
import com.hantek.ttia.comms.PhysicalInterface;

import java.util.Calendar;

public class SP implements DataListener {
    private static final String TAG = SP.class.getName();

    private static SP instance = new SP();
    private PhysicalInterface comm;
    private SPListener dataReceive;

    public static SP getInstance() {
        return instance;
    }

    @Override
    public boolean open(String options) {
        this.comm.setListener(instance);
        return this.comm.open(options);
    }

    @Override
    public boolean close() {
        return this.comm.close();
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
        if (this.dataReceive != null)
            this.dataReceive.onSPDataReceived(new String(rawData));
    }

    public void setInterface(PhysicalInterface comm) {
        this.comm = comm;
    }

    public void setListener(SPListener listener) {
        this.dataReceive = listener;
    }
}
