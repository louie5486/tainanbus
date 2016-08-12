package com.hantek.ttia.protocol.a1a4;

/**
 * 車輛狀態回報
 */
public class EventReport0x0020 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 車輛現在狀態
	 */
	public byte type;

	/**
	 * 車輛現在狀態
	 */
	public byte preType;

	public EventReport0x0020() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 2];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		bytes[index++] = this.type;
		bytes[index++] = this.preType;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0020 clone() {
		EventReport0x0020 varCopy = new EventReport0x0020();

		varCopy.monitorData = this.monitorData;
		varCopy.type = this.type;
		varCopy.preType = this.preType;

		return varCopy;
	}
}
