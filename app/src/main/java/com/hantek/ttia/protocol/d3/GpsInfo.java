package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

public class GpsInfo {
	public static final short Length = 20;

	/**
	 * 衛星個數
	 */
	public byte satelliteNo;

	/**
	 * A=1, V=0
	 */
	public byte gpsStatus;

	/**
	 * 經度之度
	 */
	public byte longitudeDu;

	/**
	 * 經度之分的整數
	 */

	public byte longitudeFen;

	/**
	 * 經度之分的小數
	 */
	public int longitudeMiao;

	/**
	 * 緯度之度
	 */
	public byte latitudeDu;

	/**
	 * 緯度之分的整數
	 */
	public byte latitudeFen;

	/**
	 * 緯度之分的小數
	 */
	public int latitudeMiao;

	/**
	 * 瞬間方向
	 */
	public int direction;

	/**
	 * 瞬間速度(km/hr)(採DCR之速度)
	 */
	public int intSpeed;

	/**
	 * UTC時間之年, 西元2000年起始(2009->9)
	 */
	public byte year;

	/**
	 * UTC時間之月
	 */
	public byte month;

	/**
	 * UTC時間之日
	 */
	public byte day;

	/**
	 * UTC時間之時
	 */
	public byte hour;

	/**
	 * UTC時間之分
	 */
	public byte minute;

	/**
	 * UTC時間之秒
	 */
	public byte second;

	public GpsInfo() {
		this.satelliteNo = 0;
		this.gpsStatus = 0;

		this.longitudeDu = 0;
		this.longitudeFen = 0;
		this.longitudeMiao = 0;

		this.latitudeDu = 0;
		this.latitudeFen = 0;
		this.latitudeMiao = 0;

		this.direction = 0;
		this.intSpeed = 0;

		this.year = 0;
		this.month = 0;
		this.day = 0;
		this.hour = 0;
		this.minute = 0;
		this.second = 0;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[Length];
		int index = 0;

		bytes[index++] = this.satelliteNo;
		bytes[index++] = this.gpsStatus;

		bytes[index++] = this.longitudeDu;
		bytes[index++] = this.longitudeFen;
		System.arraycopy(BitConverter.toUShortByteArray(this.longitudeMiao), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.latitudeDu;
		bytes[index++] = this.latitudeFen;
		System.arraycopy(BitConverter.toUShortByteArray(this.latitudeMiao), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		System.arraycopy(BitConverter.toUShortByteArray(this.direction), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		System.arraycopy(BitConverter.toUShortByteArray(this.intSpeed), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.year;
		bytes[index++] = this.month;
		bytes[index++] = this.day;
		bytes[index++] = this.hour;
		bytes[index++] = this.minute;
		bytes[index++] = this.second;

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<GpsStruct>" + "\n");
		sb.append("SatelliteNo=" + this.satelliteNo + "\n");
		sb.append("GPSStatus=" + this.gpsStatus + "\n");
		sb.append("LongitudeDu=" + this.longitudeDu + "\n");
		sb.append("LongitudeFen=" + this.longitudeFen + "\n");
		sb.append("LongitudeMiao=" + this.longitudeMiao + "\n");
		sb.append("LatitudeDu=" + this.latitudeDu + "\n");
		sb.append("LatitudeFen=" + this.latitudeFen + "\n");
		sb.append("LatitudeMiao=" + this.latitudeMiao + "\n");
		sb.append("Direction=" + this.direction + "\n");
		sb.append("IntSpeed=" + this.intSpeed + "\n");
		sb.append("year=" + this.year + "\n");
		sb.append("month=" + this.month + "\n");
		sb.append("day=" + this.day + "\n");
		sb.append("hour=" + this.hour + "\n");
		sb.append("minute=" + this.minute + "\n");
		sb.append("second=" + this.second + "\n");
		sb.append("</GpsStruct>" + "\n");
		return sb.toString();
	}

	public GpsInfo clone() {
		GpsInfo varCopy = new GpsInfo();

		varCopy.satelliteNo = this.satelliteNo;
		varCopy.gpsStatus = this.gpsStatus;
		varCopy.longitudeDu = this.longitudeDu;
		varCopy.longitudeFen = this.longitudeFen;
		varCopy.longitudeMiao = this.longitudeMiao;
		varCopy.latitudeDu = this.latitudeDu;
		varCopy.latitudeFen = this.latitudeFen;
		varCopy.latitudeMiao = this.latitudeMiao;
		varCopy.direction = this.direction;
		varCopy.intSpeed = this.intSpeed;
		varCopy.year = this.year;
		varCopy.month = this.month;
		varCopy.day = this.day;
		varCopy.hour = this.hour;
		varCopy.minute = this.minute;
		varCopy.second = this.second;

		return varCopy;
	}
}