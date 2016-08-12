package com.hantek.ttia.protocol.e2;

/**
 * 行駛資料
 */
public class DriveData {

	/**
	 * 平均速度 單位:km/hour
	 */
	private int speed;

	/**
	 * RPM 單位:50rpm
	 */
	private int rpm;

	public int getSpeed() {
		return speed;
	}

	public int getRpm() {
		return rpm;
	}

	public DriveData(int speed, int rpm) {
		this.speed = speed;
		this.rpm = rpm;
	}

	@Override
	public String toString() {
		return "DriveData [speed=" + speed + ", rpm=" + rpm + "]";
	}
}
