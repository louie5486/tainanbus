package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

public class Shutdown {
	public static final int Length = 34;

	/**
	 * 定位訊息資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 數據連線重建次數
	 */
	public int psdReconnect;

	/**
	 * 訊息傳送成功比例( 收到的訊息總數除以送出之訊息總數)
	 */
	public int packetRatio;

	/**
	 * 開機期間GPS Active比例(以定時回報之頻率取樣計算, 回報時GPS Active數量除以回報筆數)
	 */
	public int gpsRatio;

	public Shutdown() {
		this.monitorData = new MonitorStructType2();
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[Length];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		System.arraycopy(BitConverter.toUShortByteArray(this.psdReconnect), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = (byte) this.packetRatio;
		bytes[index++] = (byte) this.gpsRatio;

		return bytes;
	}
}
