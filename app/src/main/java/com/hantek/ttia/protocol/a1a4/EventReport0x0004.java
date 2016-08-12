package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

/**
 * 急加/減速回報
 */
public class EventReport0x0004 extends EventReportBase {
	/**
	 * 發生地點GPS 資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 0x01:急加速, 0x02:急減速
	 */
	public byte type;

	/**
	 * 設定之加/減速度(km/hr)
	 */
	public int speed;

	/**
	 * 保留
	 */
	public byte reserved;

	public EventReport0x0004() {
		this.monitorData = new MonitorStructType2();
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[MonitorStructType2.Length + 4];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		bytes[index++] = this.type;

		System.arraycopy(BitConverter.toUShortByteArray(this.speed), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.reserved;

		bytes = this.combineByteArray(super.getBytes(), bytes);
		return bytes;
	}

	public EventReport0x0004 clone() {
		EventReport0x0004 varCopy = new EventReport0x0004();

		varCopy.monitorData = this.monitorData;
		varCopy.type = this.type;
		varCopy.speed = this.speed;
		varCopy.reserved = this.reserved;

		return varCopy;
	}
}
