package com.hantek.ttia.protocol.d1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

import com.hantek.ttia.comms.DataListener;
import com.hantek.ttia.comms.PhysicalInterface;

/**
 * 站名顯示器
 */
public class LED implements DataListener, LEDInterface {
    private static final int STX = 0x01;
    private static final int ETX = 0x02;

    private static LED led = new LED();
    private PhysicalInterface comm;
    LEDListener dataReceive;
    private Calendar lastReceTime = Calendar.getInstance();

    private List<Integer> dataArrayList = new ArrayList<Integer>();

    public static LED getInstance() {
        return led;
    }

    @Override
    public boolean open(String options) {
        this.comm.setListener(led);
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
        return this.lastReceTime;
    }

    @Override
    public void onDataReceived(byte[] rawData, int size) {
         this.lastReceTime = Calendar.getInstance();

        String s = "";
        synchronized (dataArrayList) {
            for (int i = 0; i < size; i++) {
                s = s + String.format("%2X", rawData[i] & 0xff) + " ";
                dataArrayList.add(rawData[i] & 0xff); // Range 0 to 255, not -128 to 127
            }

            try {
                boolean pktStart = false;
                ArrayList<Integer> msgBuffer = new ArrayList<Integer>();
                int checksum = 0;
                int count = dataArrayList.size();
                int removeIndex = -1;
                int startIndex = -1;

                for (int i = 0; i < count; i++) {
                    int data = dataArrayList.get(i);

                    // 起始字元
                    if (!pktStart && data == 0x01) {
                        pktStart = true;
                        msgBuffer.add(data);
                        startIndex = i;
                        System.out.println("*** LED Find STX(01) ***");
                        continue;
                    }

                    // 結束字元
                    if (pktStart && data == 0x02 && i > (startIndex + 3)) {
                        pktStart = false;
                        msgBuffer.add(data);
                        System.out.println("*** LED Find ETX(02) ***");
                        continue;
                    }

                    if (pktStart)
                        msgBuffer.add(data);

                    if (!pktStart && msgBuffer.size() > 4) {
                        removeIndex = i;

                        int[] intArrayList = new int[msgBuffer.size()];
                        for (int j = 0; j < msgBuffer.size(); j++) {
                            intArrayList[j] = msgBuffer.get(j);
                            checksum ^= intArrayList[j];
                        }

                        if (checksum == data) {
                            analysisLED(intArrayList);
                            System.out.println("*** LED checksum *** checksum:" + checksum);
                        } else {
                            String ss = "";
                            for (int ii = 0; ii < intArrayList.length; ii++) {
                                ss = ss + String.format("%2X", intArrayList[ii] & 0xff) + " ";
                            }
                            System.out.println("*** LED wrong checksum *** checksum:" + checksum + " " + ss);
                        }
                    }
                } // end for

                if (removeIndex != -1) {
                    // 從後面開始remove
                    for (int i = removeIndex; i >= 0; i--) {
                        dataArrayList.remove(i);
                    }
                }
            } catch (Exception e) {
                dataArrayList.clear();
                e.printStackTrace();
            }
        }

        System.out.println("LED data received EOT:" + " DataList:" + dataArrayList.size());
    }

    // start-------------------- LED protocol --------------------

    @Override
    public boolean sendMessage(String cmd, Animation animation, char stay, char staySecond, char lighting, String data) {
        Message message = new Message(cmd, animation, stay, staySecond, lighting);
        message.setData(data);

        String s = "";
        for (byte b : message.getBytes()) {
            s = s + String.format("%2X", b & 0xff) + " ";
        }

        Log.d("LED", s);
        return this.comm.send(message.getBytes());
    }

    @Override
    public boolean pause() {
        SpecialMessage cmd = new SpecialMessage(SpecialCommand.Pause);

        // String s = "";
        // for (byte b : cmd.getBytes()) {
        // s = s + String.format("%2X", b & 0xff) + " ";
        // }
        //
        // Log.d("LED", s);

        return this.comm.send(cmd.getBytes());
    }

    @Override
    public boolean resume() {
        SpecialMessage cmd = new SpecialMessage(SpecialCommand.Resume);

        // String s = "";
        // for (byte b : cmd.getBytes()) {
        // s = s + String.format("%2X", b & 0xff) + " ";
        // }
        //
        // Log.d("LED", s);

        return this.comm.send(cmd.getBytes());
    }

    @Override
    public boolean forceStopPlay() {
        SpecialMessage cmd = new SpecialMessage(SpecialCommand.Stop);

        // String s = "";
        // for (byte b : cmd.getBytes()) {
        // s = s + String.format("%2X", b & 0xff) + " ";
        // }
        //
        // Log.d("LED", s);

        return this.comm.send(cmd.getBytes());
    }

    // -------------------- LED protocol --------------------end

    public void setCommInterface(PhysicalInterface comm) {
        this.comm = comm;
    }

    public void setListener(LEDListener listener) {
        this.dataReceive = listener;
    }

    private static void analysisLED(int[] intArray) {
        String s = "";
        for (int i = 0; i < intArray.length; i++) {
            s = s + String.format("%02X", intArray[i] & 0xff) + " ";
        }

        // TODO log
        if (intArray[1] == 0x30) {
            if (intArray[3] == 0x44)
                System.out.println("*** LED Receive DONE ***" + s);
            else if (intArray[3] == 0x42)
                System.out.println("*** LED Receive ABORT ***" + s);
            else if (intArray[3] == 0x46)
                System.out.println("*** LED Receive FAIL ***" + s);
            else
                System.out.println("*** LED Receive ACK ***" + intArray[3]);
        } else {
            if (intArray[3] == 0x50)
                System.out.println("*** LED Receive PAUSE ***" + s);
            else if (intArray[3] == 0x4F)
                System.out.println("*** LED Receive RESUME ***" + s);
            else if (intArray[3] == 0x51)
                System.out.println("*** LED Receive END ***" + s);
            else
                System.out.println("*** LED Receive 55 ACK ***" + intArray[3]);
        }
    }
}
