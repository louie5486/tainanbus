package com.hantek.ttia.protocol.e2;

import java.util.Arrays;

import com.hantek.ttia.module.BitConverter;

public class TiredRecord {
	/**
	 * 駕駛員(證)號碼
	 */
	private char[] driverID;

	/**
	 * 疲勞駕駛類別
	 */
	private int type;

	/**
	 * 開始時間
	 */
	private long startTime;

	/**
	 * 結束時間
	 */
	private long stopTime;

	/**
	 * 疲勞駕駛累計時間
	 */
	private long totalSeconds;

	/**
	 * 累計行駛里程
	 */
	private long totalMileage;

	/**
	 * 累計休息時間
	 */
	private long totalBreakTime;

	/**
	 * 最長休息時間
	 */
	private long maxBreakTime;

	public char[] getDriverID() {
		return driverID;
	}

	public int getType() {
		return type;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public long getTotalSeconds() {
		return totalSeconds;
	}

	public long getTotalMileage() {
		return totalMileage;
	}

	public long getTotalBreakTime() {
		return totalBreakTime;
	}

	public long getMaxBreakTime() {
		return maxBreakTime;
	}

	public TiredRecord() {
		this.driverID = new char[10];
	}

	public static TiredRecord parse(int[] intArray, int index) {
		TiredRecord data = new TiredRecord();
		data.driverID = new char[10];
		for (int i = 0; i < 10; i++) {
			try {
				data.driverID[i] = (char) intArray[index];
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				index++;
			}
		}

		data.type = intArray[index++];

		data.startTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.stopTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.totalSeconds = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.totalMileage = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.totalBreakTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.maxBreakTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		return data;
	}

	@Override
	public String toString() {
		return "[driverID=" + Arrays.toString(driverID) + ", type=" + type + ", startTime=" + startTime + ", stopTime=" + stopTime + ", totalSeconds=" + totalSeconds + ", totalMileage="
				+ totalMileage + ", totalBreakTime=" + totalBreakTime + ", maxBreakTime=" + maxBreakTime + "]";
	}

}
