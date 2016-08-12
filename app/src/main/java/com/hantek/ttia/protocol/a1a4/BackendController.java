package com.hantek.ttia.protocol.a1a4;

import java.util.Calendar;

import com.hantek.ttia.comms.DataListener;
import com.hantek.ttia.comms.PhysicalInterface;
import com.hantek.ttia.module.BitConverter;

public class BackendController implements DataListener, BackendInterface {
    private static BackendController controller = new BackendController();
    private PhysicalInterface comm = null;
    private BackendListener dataReceive;
    private Calendar lastReceTime = Calendar.getInstance();

    public static BackendController getInstance() {
        return controller;
    }

    public void setInterface(PhysicalInterface comm) {
        this.comm = comm;
    }

    public void setListener(BackendListener listener) {
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
    public byte[] sendRegisterRequest(Header header, RegisterRequest request) {
        byte[] payloadBytes = request.getBytes();
        header.payLoadLength = payloadBytes.length;
        byte[] headerBytes = header.getBytes();
        byte[] rawData = this.combineByteArray(headerBytes, payloadBytes);
//		this.comm.send(rawData);
        return rawData;
    }

    @Override
    public byte[] sendRoadModification(Header header, RoadModification roadModification) {
        byte[] payloadBytes = roadModification.getBytes();
        header.payLoadLength = payloadBytes.length;
        byte[] headerBytes = header.getBytes();
        byte[] rawData = combineByteArray(headerBytes, payloadBytes);
//		this.comm.send(rawData);
        return rawData;
    }

    @Override
    public byte[] sendRegularReport(Header header, RegularReport report) {
        byte[] payloadBytes = report.getBytes();
        header.payLoadLength = payloadBytes.length;
        byte[] headerBytes = header.getBytes();
        byte[] rawData = combineByteArray(headerBytes, payloadBytes);
//		this.comm.send(rawData);
        return rawData;
    }

    @Override
    public byte[] sendNotifyMessageConfirm(Header header) {
        byte[] headerBytes = header.getBytes();
//		this.comm.send(headerBytes);
        return headerBytes;
    }

    @Override
    public byte[] sendEventReport(Header header, EventReportBase event, Object eventContent) {
        byte[] payloadBytes = null;
        switch (EventCode.forValue(event.eventType)) {
            case InOutStation:
                payloadBytes = ((EventReport0x0001) eventContent).getBytes();
                break;
            case OverSpeedAndRPM:
                payloadBytes = ((EventReport0x0002) eventContent).getBytes();
                break;
            case Acceleration:
                payloadBytes = ((EventReport0x0004) eventContent).getBytes();
                break;
            case DoorOpen:
                payloadBytes = ((EventReport0x0008) eventContent).getBytes();
                break;
            case CarAlarm:
                payloadBytes = ((EventReport0x0010) eventContent).getBytes();
                break;
            case UpdateCarStatus:
                payloadBytes = ((EventReport0x0020) eventContent).getBytes();
                break;
            case NoScheduleOnMove:
                payloadBytes = ((EventReport0x0040) eventContent).getBytes();
                break;
            case DriverReport:
                payloadBytes = ((EventReport0x0080) eventContent).getBytes();
                break;
            case InOutSpecialArea:
                payloadBytes = ((EventReport0x0100) eventContent).getBytes();
                break;
            case EventCode200:
                break;
            case EventCode400:
                break;
            case EventCode800:
                break;
            case EventCode1000:
                break;
            case EventCode2000:
                break;
            case EventCode4000: // 大台南公車
                payloadBytes = ((EventReport0x4000) eventContent).getBytes();
                break;
            case NotOnSchedule:
                payloadBytes = ((EventReport0x8000) eventContent).getBytes();
                break;
            default:
                break;
        }

        if (payloadBytes != null) {
            header.payLoadLength = payloadBytes.length;
            byte[] headerBytes = header.getBytes();
            byte[] rawData = combineByteArray(headerBytes, payloadBytes);
//			this.comm.send(rawData);
            return rawData;
        }

        return null;
    }

    @Override
    public byte[] sendShutdown(Header header, Shutdown shutdown) {
        byte[] payloadBytes = shutdown.getBytes();
        header.payLoadLength = payloadBytes.length;
        byte[] headerBytes = header.getBytes();
        byte[] rawData = combineByteArray(headerBytes, payloadBytes);
//		this.comm.send(rawData);
        return rawData;
    }

    @Override
    public byte[] sendDeviceAlarm(Header header, DeviceAlarm deviceAlarm) {
        byte[] payloadBytes = deviceAlarm.getBytes();
        header.payLoadLength = payloadBytes.length;
        byte[] headerBytes = header.getBytes();
        byte[] rawData = combineByteArray(headerBytes, payloadBytes);
//		this.comm.send(rawData);
        return rawData;
    }

    @Override
    public byte[] sendODReport(Header header, ODReport odReport) {
        byte[] payloadBytes = odReport.getBytes();
        header.payLoadLength = payloadBytes.length;
        byte[] headerBytes = header.getBytes();
        byte[] rawData = combineByteArray(headerBytes, payloadBytes);
//		this.comm.send(rawData);
        return rawData;
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

                System.out.println(String.format("Receive: comm1 Len=%s", size));

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
                }

                System.out.println(String.format("Receive: comm1 client cmd=%s", message.header.messageID));
                this.lastReceTime = Calendar.getInstance();
                if (dataReceive != null) {
                    this.dataReceive.onReceivedBackend(message);
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
}
