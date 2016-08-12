package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

/**
 * 進出特定區域回報
 */
public class EventReport0x0100 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 管制區域代碼
	 */
	public int regionID;

	public EventReport0x0100() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 2];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		System.arraycopy(BitConverter.toUShortByteArray(this.regionID), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0100 clone() {
		EventReport0x0100 varCopy = new EventReport0x0100();

		varCopy.monitorData = this.monitorData;
		varCopy.regionID = this.regionID;

		return varCopy;
	}
}
