package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

/**
 * 超速超轉回報
 */
public class EventReport0x0002 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 站點編號
	 */
	public int stationID;

	/**
	 * 0x00:超轉, 0x01:超速
	 */
	public byte type;

	/**
	 * 設定之速度(km/hr)或轉速(rpm)
	 */
	public int value;

	/**
	 * 保留
	 */
	public byte reserved;

	public EventReport0x0002() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 6];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		System.arraycopy(BitConverter.toUShortByteArray(this.stationID), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.type;

		System.arraycopy(BitConverter.toUShortByteArray(this.value), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.reserved;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0002 clone() {
		EventReport0x0002 varCopy = new EventReport0x0002();

		varCopy.monitorData = this.monitorData;
		varCopy.stationID = this.stationID;
		varCopy.type = this.type;
		varCopy.value = this.value;
		varCopy.reserved = this.reserved;

		return varCopy;
	}
}
