package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

/**
 * 異常發車回報
 */
public class EventReport0x0040 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 設定之移動距離(單位：10 公尺)
	 */
	public int movement;

	public EventReport0x0040() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 2];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		System.arraycopy(BitConverter.toUShortByteArray(this.movement), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0040 clone() {
		EventReport0x0040 varCopy = new EventReport0x0040();

		varCopy.monitorData = this.monitorData;
		varCopy.movement = this.movement;

		return varCopy;
	}
}
