package com.hantek.ttia.protocol.e2;

import java.util.Arrays;

import com.hantek.ttia.module.BitConverter;

public class DriverRestRecord {

	/**
	 * 駕駛員(證)號碼
	 */
	private char[] driverID;

	/**
	 * 休息時間類別
	 */
	private int type;

	/**
	 * 休息開始時間
	 */
	private long startTime;

	/**
	 * 休息結束時間
	 */
	private long stopTime;

	/**
	 * 休息累計時間
	 */
	private long totalTime;

	/**
	 * 行車紀錄器累計里程數
	 */
	private long totalMileage;

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

	public long getTotalTime() {
		return totalTime;
	}

	public long getTotalMileage() {
		return totalMileage;
	}

	public DriverRestRecord() {
		this.driverID = new char[10];
	}

	public static DriverRestRecord parse(int[] intArray, int index) {
		DriverRestRecord data = new DriverRestRecord();
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

		data.totalTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.totalMileage = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		return data;
	}

	@Override
	public String toString() {
		return "[driverID=" + Arrays.toString(driverID) + ", type=" + type + ", startTime=" + startTime + ", stopTime=" + stopTime + ", totalTime=" + totalTime + ", totalMileage="
				+ totalMileage + "]";
	}

}
