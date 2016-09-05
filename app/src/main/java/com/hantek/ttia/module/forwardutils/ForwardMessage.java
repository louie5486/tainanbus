package com.hantek.ttia.module.forwardutils;

import java.util.Calendar;
import java.util.Date;

public class ForwardMessage {
    private int msgID;
    private int sequence;
    private Date sendTime;
    private Date firstTime;
    private int sendCount;
    private byte[] data;
    private int comm1Ack;
    private int comm2Ack;

    //20180902 為每個message設定一個容忍值(秒)，超過這個時間就移除不傳送.
    private int toln = 5;
    //間隔傳送時間(秒)
    private int period = 3;

    public ForwardMessage(int msgID, int sequence, byte[] data, int comm1Ack, int comm2Ack) {
        this.msgID = msgID;
        this.sequence = sequence;
        this.data = data;
        this.sendCount = 1;
        this.sendTime = new Date();
        this.comm1Ack = comm1Ack;
        this.comm2Ack = comm2Ack;
    }


    public boolean isUncheck(){
        boolean check = false;
        if(sendTime == null) return check;
        Date now = Calendar.getInstance().getTime();
        if (now.getTime() - firstTime.getTime() > (toln * 1000)){
            check = true;
        }
        return check;
    }

    public boolean isRetry(){
        boolean retry = true;
        if(sendTime == null) return retry;
        Date now = Calendar.getInstance().getTime();
        if (now.getTime() - firstTime.getTime() > (period * 1000)){
            retry = false;
        }
        return retry;

    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getToln() {
        return toln;
    }

    public void setToln(int toln) {
        this.toln = toln;
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
        if (this.sendTime == null){
            firstTime = sendTime;
        }
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
