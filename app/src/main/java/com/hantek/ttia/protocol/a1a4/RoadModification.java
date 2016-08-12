package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;


/**
 * 修改路線請求訊息
 */
public class RoadModification {
	/**
	 * 路線代號
	 */
	public int routeID;

	/**
	 * 路線方向 0:其他, 1:去程, 2:回程
	 */
	public byte routeDirect;

	/**
	 * 路線種類0x30:主線0x, 41~0x5A:支線
	 */
	public byte routeBranch;

	public RoadModification() {
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[4];
		int index = 0;

		System.arraycopy(BitConverter.toUShortByteArray(this.routeID), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.routeDirect;
		bytes[index++] = this.routeBranch;

		return bytes;
	}
}
