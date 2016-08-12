package com.hantek.ttia.protocol.a1a4;

public class DeviceAlarm {
	/**
	 * 異常模組
	 */
	public byte module;

	/**
	 * 狀態碼
	 */
	public byte code;

	public DeviceAlarm() {

	}

	public byte[] getBytes() {
		byte[] bytes = new byte[2];
		int index = 0;

		bytes[index++] = this.module;
		bytes[index++] = this.code;

		return bytes;
	}
}
