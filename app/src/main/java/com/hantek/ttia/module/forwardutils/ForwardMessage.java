package com.hantek.ttia.module.forwardutils;

import java.util.Date;

public class ForwardMessage {
    private int msgID;
    private int sequence;
    private Date sendTime;
    private int sendCount;
    private byte[] data;
    private int comm1Ack;
    private int comm2Ack;

    public ForwardMessage(int msgID, int sequence, byte[] data, int comm1Ack, int comm2Ack) {
        this.msgID = msgID;
        this.sequence = sequence;
        this.data = data;
        this.sendCount = 1;
        this.sendTime = new Date();
        this.comm1Ack = comm1Ack;
        this.comm2Ack = comm2Ack;
    }

    public int getMsgID() {
        return msgID;
    }

    public int getSequence() {
        return sequence;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void addSendCount() {
        this.sendCount++;
    }

    public byte[] getData() {
        return data;
    }

    public int getComm1Ack() {
        return comm1Ack;
    }

    public int getComm2Ack() {
        return comm2Ack;
    }

    public void setComm1Ack() {
        comm1Ack = 1;
    }

    public void setComm2Ack() {
        comm2Ack = 1;
    }

    @Override
    public String toString() {
        return "ForwardMessage [msgID=" + msgID + ", sequence=" + sequence + ", sendCount=" + sendCount + ", comm1=" + comm1Ack + ", comm2=" + comm2Ack + "]";
    }
}
