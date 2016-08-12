package com.hantek.ttia.protocol.e2;

/**
 * 停車前第n秒 0.2
 */
public class InstantSpeedDataListData {

	private int speed;
	private int rpm;

	/**
	 * 數位輸入訊號
	 */
	private int digitalInputSignal;
	private boolean D7;
	private boolean D6;
	private boolean D5;
	private boolean D4;
	private boolean D3;
	private boolean D2;
	private boolean D1;
	private boolean D0;

	public int getSpeed() {
		return speed;
	}

	public int getRpm() {
		return rpm;
	}

	public int getDigitalInputSignal() {
		return digitalInputSignal;
	}

	public boolean isD7() {
		return D7;
	}

	public boolean isD6() {
		return D6;
	}

	public boolean isD5() {
		return D5;
	}

	public boolean isD4() {
		return D4;
	}

	public boolean isD3() {
		return D3;
	}

	public boolean isD2() {
		return D2;
	}

	public boolean isD1() {
		return D1;
	}

	public boolean isD0() {
		return D0;
	}

	public InstantSpeedDataListData(int speed, int rpm, int digitalInput) {
		this.speed = speed;
		this.rpm = rpm;
		this.digitalInputSignal = digitalInput;

		this.D7 = bitCheck(this.digitalInputSignal, 0x80);
		this.D6 = bitCheck(this.digitalInputSignal, 0x40);
		this.D5 = bitCheck(this.digitalInputSignal, 0x20);
		this.D4 = bitCheck(this.digitalInputSignal, 0x10);
		this.D3 = bitCheck(this.digitalInputSignal, 0x08);
		this.D2 = bitCheck(this.digitalInputSignal, 0x04);
		this.D1 = bitCheck(this.digitalInputSignal, 0x02);
		this.D0 = bitCheck(this.digitalInputSignal, 0x01);
	}

	private boolean bitCheck(int value, int condition) {
		return (value & condition) == condition ? true : false;
	}

	@Override
	public String toString() {
		return "[speed=" + speed + ", rpm=" + rpm + ", digitalInputSignal=" + digitalInputSignal + "]";
	}

}
