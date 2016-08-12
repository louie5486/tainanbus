package com.hantek.ttia.protocol.a1a4;

/**
 * 行駛中前門/後門開啟回報
 */
public class EventReport0x0008 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 0x01:前門, 0x02:後門
	 */
	public byte type;

	/**
	 * 保留
	 */
	public byte reserved;

	public EventReport0x0008() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 2];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		bytes[index++] = this.type;

		bytes[index++] = this.reserved;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0008 clone() {
		EventReport0x0008 varCopy = new EventReport0x0008();

		varCopy.monitorData = this.monitorData;
		varCopy.type = this.type;
		varCopy.reserved = this.reserved;

		return varCopy;
	}
}
