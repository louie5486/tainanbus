package com.hantek.ttia.comms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.util.Log;

import android_serialport_api.SerialPort;

public class SerialPortHandler implements PhysicalInterface {
    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private ReadThread mReadThread;
    private DataListener dataListener;
    private Calendar lastRecvDataTime;

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null)
                        return;
                    byte[] buffer = new byte[512];
                    size = mInputStream.read(buffer);

                    if (size > 0) {
                        lastRecvDataTime = Calendar.getInstance();
                        Obs(buffer, size);
                    }
                } catch (IOException e) {
                    Log.e("SerialError", e.getMessage());
                    return;
                }
            }
        }
    }

    public SerialPortHandler() {
        this.lastRecvDataTime = Calendar.getInstance();
    }

    @Override
    public boolean open(String options) {
        String[] tmp = options.split(",");// /dev/ttyUSB1,9600
        try {
            mSerialPort = new SerialPort(new File(tmp[0]), Integer.parseInt(tmp[1]), 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.setName("SerialPort-" + tmp[0]);
            mReadThread.start();
        } catch (NumberFormatException | SecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean close() {
        if (mReadThread != null) {
            try {
                this.mReadThread.join(1000);
            } catch (InterruptedException e) {
                this.mReadThread.interrupt();
            }
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }

        return false;
    }

    @Override
    public boolean isOpen() {
        return mSerialPort != null ? true : false;
    }

    @Override
    public Calendar getLastReceiveTime() {
        return this.lastRecvDataTime;
    }

    @Override
    public boolean send(final byte[] dataByte) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                callSend(dataByte);
            }

        }).start();

        return false;
    }

    @Override
    public void setListener(DataListener listener) {
        this.dataListener = listener;
    }

    private void Obs(byte[] rawData, int size) {
        if (this.dataListener != null)
            this.dataListener.onDataReceived(rawData, size);
    }

    private void callSend(byte[] dataByte) {
        try {
            if (mOutputStream != null) {
                try {
                    mOutputStream.write(dataByte);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
