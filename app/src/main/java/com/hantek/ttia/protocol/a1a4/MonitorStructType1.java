package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;


public final class MonitorStructType1 {
	public static final short Length = 110;

	/**
	 * GPS資料
	 */
	public GpsStruct gpsStruct;

	/**
	 * 平均速度(km/hr)(採DCR之速度)
	 */
	public int avgSpeed;

	/**
	 * 前20秒每秒之速度(採DCR之速度)
	 */
	public int[] intSpeed;

	/**
	 * 前20秒每秒之轉速
	 */
	public int[] RPM;

	/**
	 * 勤務狀態
	 */
	public byte dutyStatus;

	/**
	 * 行車狀態
	 */
	public byte busStatus;

	/**
	 * 里程數(單位10公尺)
	 */
	public long mileage;

	public MonitorStructType1() {
		this.gpsStruct = new GpsStruct();
		this.avgSpeed = 0;
		this.intSpeed = new int[20];
		this.RPM = new int[20];
		this.dutyStatus = (byte) DutyStatus.Ready.getValue();
		this.busStatus = (byte) BusStatus.Offline.getValue();
		this.mileage = 0;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[Length];
		int index = 0;

		System.arraycopy(this.gpsStruct.getBytes(), 0, bytes, index, GpsStruct.Length);
		index += GpsStruct.Length;

		System.arraycopy(BitConverter.toUShortByteArray(this.avgSpeed), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		for (int i = 0; i < 20; i++) {
			System.arraycopy(BitConverter.toUShortByteArray(this.intSpeed[i]), 0, bytes, index, BitConverter.UshortSIZE);
			index += BitConverter.UshortSIZE;
		}

		for (int i = 0; i < 20; i++) {
			System.arraycopy(BitConverter.toUShortByteArray(this.RPM[i]), 0, bytes, index, BitConverter.UshortSIZE);
			index += BitConverter.UshortSIZE;
		}

		bytes[index++] = this.dutyStatus;

		bytes[index++] = this.busStatus;

		System.arraycopy(BitConverter.toUIntegerByteArray(this.mileage), 0, bytes, index, BitConverter.UintSIZE);
		index += BitConverter.UintSIZE;

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<MonitorStructType1>" + "\n");
		sb.append(this.gpsStruct.toString());
		sb.append("avgSpeed=" + this.avgSpeed + "\n");
		sb.append("intSpeed=");
		for (int speed : this.intSpeed) {
			sb.append(speed + ",");
		}
		sb.append("\n");
		sb.append("RPM=");
		for (int rpm : this.RPM) {
			sb.append(rpm + ",");
		}
		sb.append("\n");
		sb.append("dutyStatus=" + this.dutyStatus + "\n");
		sb.append("busStatus=" + this.busStatus + "\n");
		sb.append("mileage=" + this.mileage + "\n");
		sb.append("</MonitorStructType1>" + "\n");
		return sb.toString();
	}

	public MonitorStructType1 clone() {
		MonitorStructType1 varCopy = new MonitorStructType1();

		varCopy.gpsStruct = this.gpsStruct;
		varCopy.avgSpeed = this.avgSpeed;
		varCopy.intSpeed = this.intSpeed;
		varCopy.RPM = this.RPM;
		varCopy.dutyStatus = this.dutyStatus;
		varCopy.busStatus = this.busStatus;
		varCopy.mileage = this.mileage;

		return varCopy;
	}
}
