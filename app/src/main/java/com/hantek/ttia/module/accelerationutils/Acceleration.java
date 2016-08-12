package com.hantek.ttia.module.accelerationutils;

import java.util.Calendar;

import com.hantek.ttia.module.Utility;

/**
 * 急加/減速 判斷
 */
public class Acceleration {
    private static Acceleration instance = new Acceleration();

    /**
     * 加速度限制（3 秒內加速度超越值）（預設值：30）
     */
    private int accelerate = 30;

    /**
     * 減速度限制（3 秒內減速度超越值）（預設值：30）
     */
    private int decelerate = 30;

    private AccelerationInterface interfaces;
    private Calendar lastCheckTime = Calendar.getInstance();
    private double lastSpeed = 0;

    public static Acceleration getInstance() {
        return instance;
    }

    public void setLimited(int accelerate, int decelerate) {
        this.accelerate = accelerate;
        this.decelerate = decelerate;
    }

    public void setInterface(AccelerationInterface interfaces) {
        this.interfaces = interfaces;
    }

    public boolean check(double currentSpeed) {
        if (Utility.dateDiffNow(this.lastCheckTime) >= 3000) {
            this.lastCheckTime = Calendar.getInstance();
            try {
                currentSpeed = currentSpeed * 1.852d;
                if (currentSpeed - lastSpeed > accelerate) {
                    if (this.interfaces != null)
                        this.interfaces.onAccelerationTrigger((byte) 0x01, accelerate);
                } else if (lastSpeed - currentSpeed > decelerate) {
                    if (this.interfaces != null)
                        this.interfaces.onAccelerationTrigger((byte) 0x02, decelerate);
                }

                this.lastSpeed = currentSpeed;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
