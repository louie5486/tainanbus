package com.hantek.ttia.protocol.hantek;

import java.util.Calendar;

import com.hantek.ttia.comms.DataListener;
import com.hantek.ttia.comms.PhysicalInterface;
import com.hantek.ttia.module.BitConverter;
import com.hantek.ttia.protocol.a1a4.BackendMsgID;
import com.hantek.ttia.protocol.a1a4.Header;
import com.hantek.ttia.protocol.a1a4.Message;
import com.hantek.ttia.protocol.a1a4.NotifyMessage;
import com.hantek.ttia.protocol.a1a4.RegisterResponse;

public class HantekController implements DataListener {
    private static HantekController controller = new HantekController();
    private PhysicalInterface comm = null;
    private HantekListener dataReceive;
    private Calendar lastReceTime = Calendar.getInstance();

    public static HantekController getInstance() {
        return controller;
    }

    public void setInterface(PhysicalInterface comm) {
        this.comm = comm;
    }

    public void setListener(HantekListener listener) {
        this.dataReceive = listener;
    }

    @Override
    public boolean open(String options) {
        this.comm.setListener(controller);
        return this.comm.open(options);
    }

    @Override
    public boolean close() {
        return this.comm.close();
    }

    @Override
    public boolean isOpen() {
        return this.comm.isOpen();
    }

    @Override
    public Calendar getLastReceiveTime() {
        return this.lastReceTime;
    }

    @Override
    public void onDataReceived(byte[] rawData, int size) {
        int index = 0;
        try {
            int[] dataArray = convertToIntArray(rawData, size);

            do {
                // header array
                int[] headerArray = new int[20];
                System.arraycopy(dataArray, index, headerArray, 0, headerArray.length);
                index += 20;

                // payload length
                int payloadLength = BitConverter.toUShort(dataArray, 18);

                // payload array
                int[] payloadArray = new int[payloadLength];
                System.arraycopy(dataArray, index, payloadArray, 0, payloadArray.length);
                index += payloadLength;

                System.out.println(String.format("Receive: comm2 Len=%s", size));

                Message message = new Message();
                message.header = Header.Parse(headerArray);
                if (message.header.messageID == BackendMsgID.RegisterResponse.getValue()) {
                    message.payload = RegisterResponse.Parse(payloadArray);
                } else if (message.header.messageID == BackendMsgID.RoadModificationConfirm.getValue()) {
                    // no payload
                } else if (message.header.messageID == BackendMsgID.RegularReportConfirm.getValue()) {
                    // no payload
                } else if (message.header.messageID == BackendMsgID.NotifyMessage.getValue()) {
                    message.payload = NotifyMessage.Parse(payloadArray);
                } else if (message.header.messageID == BackendMsgID.EventReportConfirm.getValue()) {
                    // no payload
                } else if (message.header.messageID == BackendMsgID.ShutdownConfirm.getValue()) {
                    // no payload
                } else if (message.header.messageID == BackendMsgID.DeviceAlarmConfirm.getValue()) {
                    // no payload
                } else if (message.header.messageID == BackendMsgID.ReportOD.getValue()) {
                    // no payload
                } else if (message.header.messageID == BackendMsgID.Reserved1.getValue()) {
                    message.payload = UploadLog.Parse(payloadArray);
                }

                System.out.println(String.format("Receive: comm2 client cmd=%s", message.header.messageID));
                this.lastReceTime = Calendar.getInstance();
                if (dataReceive != null) {
                    this.dataReceive.onReceivedHantek(message);
                }
            } while (index < dataArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] convertToIntArray(byte[] input, int size) {
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }

    private byte[] combineByteArray(byte[] headerBytes, byte[] payloadBytes) {
        byte[] bytes = new byte[headerBytes.length + payloadBytes.length];
        int index = 0;
        System.arraycopy(headerBytes, 0, bytes, index, headerBytes.length);
        index += headerBytes.length;

        System.arraycopy(payloadBytes, 0, bytes, index, payloadBytes.length);
        index += payloadBytes.length;

        return bytes;
    }

    public byte[] sendUploadConfirm(Header header, UploadConfirm confirm) {
        byte[] payloadBytes = confirm.getBytes();
        header.payLoadLength = payloadBytes.length;
        byte[] headerBytes = header.getBytes();
        byte[] rawData = this.combineByteArray(headerBytes, payloadBytes);
//		this.comm.send(rawData);
        return rawData;
    }
}
