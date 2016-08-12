package com.hantek.ttia.protocol.a1a4;

/**
 * 路線外營運回報
 */
public class EventReport0x8000 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	public EventReport0x8000() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x8000 clone() {
		EventReport0x8000 varCopy = new EventReport0x8000();

		varCopy.monitorData = this.monitorData;

		return varCopy;
	}
}
