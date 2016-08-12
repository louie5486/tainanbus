package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

/**
 * 司機回覆回報
 */
public class EventReport0x0080 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 提示訊息代碼
	 */
	public int infoID;

	/**
	 * 回覆結果 0:確認, 1:接受, 2:拒絕
	 */
	public byte type;

	/**
	 * 保留
	 */
	public byte reserved;

	public EventReport0x0080() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 4];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		System.arraycopy(BitConverter.toUShortByteArray(this.infoID), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.type;

		bytes[index++] = this.reserved;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0080 clone() {
		EventReport0x0080 varCopy = new EventReport0x0080();

		varCopy.monitorData = this.monitorData;
		varCopy.infoID = this.infoID;
		varCopy.type = this.type;
		varCopy.reserved = this.reserved;

		return varCopy;
	}
}
