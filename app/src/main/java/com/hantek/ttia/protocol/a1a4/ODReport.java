package com.hantek.ttia.protocol.a1a4;

import java.util.ArrayList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

public class ODReport {
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

	/**
	 * OD Record筆數
	 */
	public byte ODRecord;

	/**
	 * 保留
	 */
	public byte Reserved;

	/**
	 * OD Record List
	 */
	public List<ODStruct> odReportList = new ArrayList<ODStruct>();

	public byte[] getBytes() {
		int odLength = 0;
		if (this.odReportList.size() > 0) {
			for (int i = 0; i < this.odReportList.size(); i++) {
				byte[] tmpBytes = this.odReportList.get(i).getBytes();
				odLength += tmpBytes.length;
			}
		}
		
		byte[] bytes = new byte[6 + odLength];
		int index = 0;

		System.arraycopy(BitConverter.toUShortByteArray(this.routeID), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		bytes[index++] = this.routeDirect;
		bytes[index++] = this.routeBranch;

		bytes[index++] = this.ODRecord;
		bytes[index++] = this.Reserved;

		if (this.odReportList.size() > 0) {
			for (int i = 0; i < this.odReportList.size(); i++) {
				byte[] tmpBytes = this.odReportList.get(i).getBytes();
				System.arraycopy(tmpBytes, 0, bytes, index, tmpBytes.length);
				index += tmpBytes.length;
			}
		}

		return bytes;
	}
}
