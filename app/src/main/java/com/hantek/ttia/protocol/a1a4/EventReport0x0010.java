package com.hantek.ttia.protocol.a1a4;

/**
 * 車輛異常回報
 */
public class EventReport0x0010 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 異常種類：0x01:停車不熄火, 0x02:異常移動
	 */
	public byte type;

	/**
	 * 針對停車不熄火事件：0x01:開始, 0x02:結束
	 */
	public byte flag;

	public EventReport0x0010() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 2];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		bytes[index++] = this.type;
		bytes[index++] = this.flag;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0010 clone() {
		EventReport0x0010 varCopy = new EventReport0x0010();

		varCopy.monitorData = this.monitorData;
		varCopy.type = this.type;
		varCopy.flag = this.flag;

		return varCopy;
	}
}
