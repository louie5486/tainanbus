package com.hantek.ttia.protocol.d3;

import java.util.ArrayList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * 0xA4, OD資料
 */
public class ODData extends Message {

	/**
	 * 路線代號
	 */
	public int routeID;

	/**
	 * 路線方向 0:其他, 1:去程, 2:回程
	 */
	public int routeDirect;

	/**
	 * 路線種類0x30:主線0x, 41~0x5A:支線
	 */
	public int routeBranch;

	/**
	 * OD Record筆數
	 */
	public int ODRecord;

	/**
	 * 保留
	 */
	public int Reserved;

	/**
	 * OD Record List
	 */
	public List<ODStruct> odReportList = new ArrayList<ODStruct>();

	public ODData() {
		super(ETMMsgID.ODData, FrameType.Response);
	}

	public static ODData parse(int[] intArray) {
		int index = 9;
		ODData data = new ODData();

		data.routeID = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		data.routeDirect = intArray[index++];

		data.routeBranch = intArray[index++];

		data.ODRecord = intArray[index++];

		data.Reserved = intArray[index++];

		int record = data.ODRecord;
		if (record > 0) {
			// do {
			// ODStruct od = ODStruct.parse(intArray, index);
			// data.odReportList.add(od);
			// index += ODStruct.Length + (od.cardList.size() * Card.Length);
			// } while (index < intArray.length);
			while (record > 0) {
				ODStruct od = ODStruct.parse(intArray, index);
				data.odReportList.add(od);
				index += ODStruct.Length + (od.cardList.size() * Card.Length);
				record -= 1;
			}
		}
		return data;
	}

	@Override
	public String toString() {
		String s = "ODData [routeID=" + routeID + ", routeDirect=" + routeDirect + ", routeBranch=" + routeBranch + ", ODRecord=" + ODRecord + ", Reserved=" + Reserved + "]\n";

		if (this.ODRecord > 0) {
			for (int i = 0; i < this.odReportList.size(); i++) {
				ODStruct od = this.odReportList.get(i);
				s += String.format("OD(%s) %s", i + 1, od.toString());
			}
		}
		return s;
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<ODData>" + "\n");
	// sb.append("</ODData>" + "\n");
	// return sb.toString();
	// }
}
