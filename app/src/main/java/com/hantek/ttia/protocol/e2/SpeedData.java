package com.hantek.ttia.protocol.e2;

import com.hantek.ttia.module.BitConverter;

/**
 * 即時速度
 */
public class SpeedData {

	private long time;
	private int gpsLongitude;
	private int gpsLatitude;
	private int gpsAngle;
	private int gpsSpeed;
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

	private int totalMileage;

	public long getTime() {
		return time;
	}

	public int getGpsLongitude() {
		return gpsLongitude;
	}

	public int getGpsLatitude() {
		return gpsLatitude;
	}

	public int getGpsAngle() {
		return gpsAngle;
	}

	public int getGpsSpeed() {
		return gpsSpeed;
	}

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

	public int getTotalMileage() {
		return totalMileage;
	}

	public SpeedData() {

	}

	public static SpeedData parse(int[] intArray, int index) {
		SpeedData data = new SpeedData();
		data.time = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.gpsLongitude = BitConverter.byteArrayToInt(intArray, index);
		index += BitConverter.IntSIZE;

		data.gpsLatitude = BitConverter.byteArrayToInt(intArray, index);
		index += BitConverter.IntSIZE;

		data.gpsAngle = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		data.gpsSpeed = intArray[index++];

		data.speed = intArray[index++];

		data.rpm = intArray[index++];

		data.digitalInputSignal = intArray[index++];
		data.D7 = bitCheck(data.digitalInputSignal, 0x80);
		data.D6 = bitCheck(data.digitalInputSignal, 0x40);
		data.D5 = bitCheck(data.digitalInputSignal, 0x20);
		data.D4 = bitCheck(data.digitalInputSignal, 0x10);
		data.D3 = bitCheck(data.digitalInputSignal, 0x08);
		data.D2 = bitCheck(data.digitalInputSignal, 0x04);
		data.D1 = bitCheck(data.digitalInputSignal, 0x02);
		data.D0 = bitCheck(data.digitalInputSignal, 0x01);

		data.totalMileage = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		return data;
	}

	private static boolean bitCheck(int value, int condition) {
		return (value & condition) == condition ? true : false;
	}

	@Override
	public String toString() {
		return "[time=" + time + ", gpsLongitude=" + gpsLongitude + ", gpsLatitude=" + gpsLatitude + ", gpsAngle=" + gpsAngle + ", gpsSpeed=" + gpsSpeed + ", speed=" + speed + ", rpm=" + rpm
				+ ", digitalInputSignal=" + digitalInputSignal + "]";
	}
}
