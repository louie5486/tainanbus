package com.hantek.ttia.comms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

import component.LogManager;

public class UdpSocketClient implements Runnable, PhysicalInterface {
    private boolean isStart = false;
    private String ip = "";
    private int port = 0;
    private DataListener dataRecvInterface;
    private Thread readThread = null;
    private DatagramSocket serverSocket = null;
    private Calendar lastRecvDataTime = Calendar.getInstance();

    public UdpSocketClient() {
        this.isStart = false;
        this.lastRecvDataTime = Calendar.getInstance();
        this.lastRecvDataTime.add(Calendar.SECOND, -60);
    }

    /**
     * options=[ip,port]. ie:127.0.0.0,80
     */
    @Override
    public boolean open(String options) {
        if (this.isStart) {
            return false;
        }

        this.isStart = true;
        String[] data = options.split(",");
        this.ip = data[0];
        this.port = Integer.parseInt(data[1]);

		/* Create a receiving thread */
        this.readThread = new Thread(this);
        this.readThread.setName("UdpClient");
        this.readThread.start();
        return true;
    }

    @Override
    public boolean close() {
        if (!this.isStart) {
            return false;
        }

        this.isStart = false;
        return true;
    }

    @Override
    public boolean isOpen() {
        return this.isStart;
    }

    @Override
    public Calendar getLastReceiveTime() {
        return lastRecvDataTime;
    }

    @Override
    public boolean send(final byte[] dataByte) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                callSend(dataByte);
            }

        }).start();

        return true;
    }

    @Override
    public void setListener(DataListener listener) {
        this.dataRecvInterface = listener;
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new DatagramSocket(this.port);
            LogManager.write("udp", "Run~" + this.port, null);
        } catch (SocketException e) {
            e.printStackTrace();
            LogManager.write("udp", e.toString(), null);
        }

        byte[] buffer = new byte[512];
        while (this.isStart) {
            try {
                Thread.sleep(10);
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                this.serverSocket.receive(receivePacket);
                this.lastRecvDataTime = Calendar.getInstance();
                System.out.println(String.format("Receive:%s, Length=%s, Offset=%s.", this.port, receivePacket.getLength(), receivePacket.getOffset()));
                Obs(receivePacket.getData(), receivePacket.getLength());
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                LogManager.write("udp", e.toString(), null);
            }
        }

        if (this.serverSocket != null) {
            LogManager.write("udp", "Stop~", null);
            this.serverSocket.close();
            this.serverSocket = null;
        }
    }

    private boolean callSend(byte[] dataByte) {
        InetAddress server = null;
        try {
            server = InetAddress.getByName(this.ip);
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.write("udp", e.toString(), null);
            return false;
        }

        if (server == null)
            return false;

        try {
            DatagramPacket packet = new DatagramPacket(dataByte, dataByte.length, server, this.port);
            this.serverSocket.send(packet); // 用listen socket傳送

//            DatagramSocket ds = new DatagramSocket();
//            ds.send(packet);
//            ds.close();
//            System.out.println("Send UDP");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.write("udp", e.toString(), null);
            return false;
        }
    }

    private void Obs(byte[] rawData, int size) {
        if (this.dataRecvInterface != null)
            this.dataRecvInterface.onDataReceived(rawData, size);
    }
}
