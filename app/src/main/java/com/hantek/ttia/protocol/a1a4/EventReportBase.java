package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

/**
 * 事件回報訊息
 */
public class EventReportBase {
	/**
	 * 事件種類
	 */
	public int eventType;

	/**
	 * 路線代號
	 */
	public int routeID;

	/**
	 * 路線方向 0:其他, 1:去程, 2:回程
	 */
	public byte routeDirect;

	/**
	 * 路線種類 0x30:主線, 0x41~0x5A:支線
	 */
	public byte routeBranch;

	public EventReportBase() {
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[6];
		int index = 0;

		System.arraycopy(BitConverter.toUShortByteArray(this.eventType), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		System.arraycopy(BitConverter.toUShortByteArray(this.routeID), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.routeDirect;
		bytes[index++] = this.routeBranch;

		return bytes;
	}

	@Override
	public String toString() {
		return "EventReport [eventType=" + eventType + ", routeID=" + routeID + ", routeDirect=" + routeDirect + ", routeBranch=" + routeBranch + "]";
	}

	protected byte[] combineByteArray(byte[] headerBytes, byte[] payloadBytes) {
		byte[] bytes = new byte[headerBytes.length + payloadBytes.length];
		int index = 0;
		System.arraycopy(headerBytes, 0, bytes, index, headerBytes.length);
		index += headerBytes.length;

		System.arraycopy(payloadBytes, 0, bytes, index, payloadBytes.length);
		index += payloadBytes.length;

		return bytes;
	}
}
