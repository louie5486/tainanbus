package com.hantek.ttia.protocol.e2;

import java.util.Arrays;

import com.hantek.ttia.module.BitConverter;

/**
 * A4h 停車前5 分鐘內每秒鐘最高速度
 */
public class HighSpeed extends Message {

	/**
	 * 停車時間
	 */
	private long stopTime;

	/**
	 * 停車經度
	 */
	private int stopLongitude;

	/**
	 * 停車緯度
	 */
	private int stopLatitude;

	/**
	 * 停車方位角
	 */
	private int stopAngle;

	/**
	 * 每秒速度最高速度
	 */
	private int[] speedArray;

	public long getStopTime() {
		return stopTime;
	}

	public int getStopLongitude() {
		return stopLongitude;
	}

	public int getStopLatitude() {
		return stopLatitude;
	}

	public int getStopAngle() {
		return stopAngle;
	}

	public int[] getSpeedArray() {
		return speedArray;
	}

	public HighSpeed() {
		super(DCRMsgID.HighSpeed);
	}

	public static HighSpeed parse(int[] intArray) {
		long len = BitConverter.toUInteger(intArray, 4);

		int index = 8; // data block
		HighSpeed data = new HighSpeed();
		if (len > 0) {
			data.stopTime = BitConverter.toUInteger(intArray, index);
			index += BitConverter.UintSIZE;

			data.stopLongitude = BitConverter.byteArrayToInt(intArray, index);
			index += BitConverter.IntSIZE;

			data.stopLatitude = BitConverter.byteArrayToInt(intArray, index);
			index += BitConverter.IntSIZE;

			data.stopAngle = BitConverter.toUShort(intArray, index);
			index += BitConverter.UshortSIZE;

			data.speedArray = new int[300];
			System.arraycopy(intArray, index, data.speedArray, 0, 300);
			index += 300;
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<HighSpeed>\n");
		sb.append("stopTime = " + this.toDateTime(this.stopTime) + "\n");
		sb.append("stopLongitude = " + this.stopLongitude + "\n");
		sb.append("stopLatitude = " + this.stopLatitude + "\n");
		sb.append("stopAngle = " + this.stopAngle + "\n");
		sb.append("speedArray = " + Arrays.toString(this.speedArray) + "\n");
		sb.append("</HighSpeed>");
		return sb.toString();
	}
}
