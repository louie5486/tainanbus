package com.hantek.ttia.module;

public class SystemConfig {
    public Version advertVersion;
    public Version roadVersion;
    public Version welcomeVersion;
    public Version radiusVersion;

    public Radius inRadius;
    public Radius outRadius;
    public int report;
    public int acc;
    public String gender;
    public String lang;
    public boolean speakWelcome;
    public String welcomeVoice;

    public boolean speakStop;
    public String stopVoice;

    /**
     * 轉速限制（預設值：3000）
     */
    public int RPM = 3000;

    /**
     * 加速度限制（3 秒內加速度超越值）（預設值：30）
     */
    public int accelerate = 30;

    /**
     * 減速度限制（3 秒內減速度超越值）（預設值：30）
     */
    public int decelerate = 30;

    /**
     * 停車不熄火時間限制（單位：分）（預設值：10）
     */
    public int halt = 10;

    /**
     * 異常發車移動距離（單位：10 公尺）（預設值：10）
     */
    public int movement = 10;

    public SystemConfig() {
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "acc=" + acc +
                ", advertVersion=" + advertVersion.toString() +
                ", roadVersion=" + roadVersion.toString() +
                ", welcomeVersion=" + welcomeVersion.toString() +
                ", radiusVersion=" + radiusVersion.toString() +
                ", inRadius=" + inRadius.toString() +
                ", outRadius=" + outRadius.toString() +
                ", report=" + report +
                ", gender='" + gender + '\'' +
                ", lang='" + lang + '\'' +
                ", speakWelcome=" + speakWelcome +
                ", welcomeVoice='" + welcomeVoice + '\'' +
                ", RPM=" + RPM +
                ", accelerate=" + accelerate +
                ", decelerate=" + decelerate +
                ", halt=" + halt +
                ", movement=" + movement +
                '}';
    }
}
