package com.hantek.ttia.module.ledutils;

import com.hantek.ttia.module.roadutils.Station;
import com.hantek.ttia.protocol.d1.Animation;
import com.hantek.ttia.protocol.d1.LED;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import component.LogManager;

public class LEDPlayer extends Thread {
    private static LEDPlayer ourInstance = new LEDPlayer();

    private final Object stringLock = new Object();
    private boolean running = false;
    private int waiting_halfSecond = 0;
    private int msgIndex = 0;

    private boolean looping = false;
    private ArrayList<LEDInfo> data = null;
    private ArrayList<LEDInfo> radiusData = null;
    private ArrayList<LEDInfo> welData = null;

    boolean playWelcome;

    boolean play;
    Station station;
    String gender;
    String type;
    String eventType;

    public static LEDPlayer getInstance() {
        return ourInstance;
    }

    private LEDPlayer() {
        this.setName("LEDPlayer");
    }

    public void open() {
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

    public void play(Station station, String gender, String type, String eventType) {
        synchronized (stringLock) {
            this.station = station;
            this.gender = gender;
            this.type = type;
            this.eventType = eventType;
            this.play = true;
        }
    }

    public void startWork(boolean manually) {
        if (manually) {
            playWelcome = true;
        }
        looping = true;
    }

    public void stopWork() {
        looping = false;
    }

    public void setData(ArrayList<LEDInfo> data) {

        Collections.sort(data, new Comparator() {

            @Override
            public int compare(Object lhs, Object rhs) {
                LEDInfo info1 = (LEDInfo) lhs;
                LEDInfo info2 = (LEDInfo) rhs;
                return info1.no - info2.no;
            }
        });

        ArrayList<LEDInfo> tmp = new ArrayList<>();
        for (LEDInfo info : data) {
//            if (info.type == 0 || info.type == 1)
            tmp.add(info);
        }
        this.data = tmp;
    }

    public void setRadiusData(ArrayList<LEDInfo> data) {

        Collections.sort(data, new Comparator() {

            @Override
            public int compare(Object lhs, Object rhs) {
                LEDInfo info1 = (LEDInfo) lhs;
                LEDInfo info2 = (LEDInfo) rhs;
                return info1.no - info2.no;
            }
        });

        ArrayList<LEDInfo> tmp = new ArrayList<>();
        for (LEDInfo info : data) {
//            if (info.type == 0 || info.type == 1)
            tmp.add(info);
        }
        this.radiusData = tmp;
    }

    public void setWelcome(ArrayList<LEDInfo> data) {

        Collections.sort(data, new Comparator() {

            @Override
            public int compare(Object lhs, Object rhs) {
                LEDInfo info1 = (LEDInfo) lhs;
                LEDInfo info2 = (LEDInfo) rhs;
                return info1.no - info2.no;
            }
        });

        ArrayList<LEDInfo> tmp = new ArrayList<>();
        for (LEDInfo info : data) {
//            if (info.type == 0 || info.type == 1)
            tmp.add(info);
        }
        this.welData = tmp;
    }

    @Override
    public void run() {
        ArrayList<LEDInfo> tmpData = new ArrayList<>();

        while (running) {
            try {
                Thread.sleep(500);//修改需同步改delay參數(waiting_halfSecond)
                if (!looping)
                    tmpData.clear();

                if (playWelcome) {
                    playWelcome = false;
                    tmpData.clear();
                    for (LEDInfo info : welData) {
                        tmpData.add(info);
                    }

                    if (tmpData.size() > 0)
                        waiting_halfSecond = 0;//插播
                }

                if (this.play) {
                    this.play = false;
                    tmpData.clear();
                    for (LEDInfo info : radiusData) {
                        if (eventType.equalsIgnoreCase("out") && info.type == 3) {
                            tmpData.add(info);
                        } else if (eventType.equalsIgnoreCase("in") && info.type == 2) {
                            tmpData.add(info);
                        }
                    }

                    if (tmpData.size() > 0)
                        waiting_halfSecond = 0;//插播
                }

                if (tmpData.size() > 0 && waiting_halfSecond <= 0) {
                    LEDInfo info = tmpData.remove(0);
                    String content = "";
                    if (info.type == 2 || info.type == 3) {
                        if (info.lang.equalsIgnoreCase("en"))
                            content = String.format(info.content, station.enName, station.zhName);
                        else
                            content = String.format(info.content, station.zhName, station.enName);
                    } else if (info.type == 4 || info.type == 5) {
                        content = info.content;
                    }

                    if (content.length() == 0)
                        continue;

                    Animation animation = Animation.forValue(info.animation);
                    char stay = String.valueOf(info.stay).charAt(0);
                    char second = String.valueOf(info.second).charAt(0);
                    char light = String.valueOf(info.light).charAt(0);
                    LED.getInstance().sendMessage("00", animation, stay, second, light, content);
                    waiting_halfSecond = info.wait;
                    LogManager.write("STA", "LED," + content, null);
                    continue;
                }

                if (waiting_halfSecond > 0)
                    waiting_halfSecond -= 1;

                if (this.play)
                    continue;

                if (looping && tmpData.size() == 0)
                    check();

                if (!looping)
                    tmpData.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void check() {
        if (waiting_halfSecond > 0)
            return;

        if (this.data != null && this.data.size() > 0) {
            try {
                LEDInfo info = this.data.get(msgIndex);
                String content = info.content;
                if (info.type == 1) {
                    content = String.format(info.content, Calendar.getInstance().getTime());
                }

                Animation animation = Animation.forValue(info.animation);
                char stay = String.valueOf(info.stay).charAt(0);
                char second = String.valueOf(info.second).charAt(0);
                char light = String.valueOf(info.light).charAt(0);
                LED.getInstance().sendMessage("00", animation, stay, second, light, content);
                waiting_halfSecond = info.wait;
                LogManager.write("led", "A," + content, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            msgIndex++;
            if (msgIndex >= this.data.size())
                msgIndex = 0;
        }
    }
}
