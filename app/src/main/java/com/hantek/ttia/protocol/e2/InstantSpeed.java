package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * A3h 瞬時速度資料
 */
public class InstantSpeed extends Message {

	private List<InstantSpeedData> intSpeedDataList;

	public List<InstantSpeedData> getIntSpeedDataList() {
		return intSpeedDataList;
	}

	public InstantSpeed() {
		super(DCRMsgID.InstantSpeed);
		this.intSpeedDataList = new LinkedList<InstantSpeedData>();
	}

	public static InstantSpeed parse(int[] intArray) {
		long len = BitConverter.toUInteger(intArray, 4);

		int index = 8; // data block
		InstantSpeed data = new InstantSpeed();
		if (len > 0) {
			for (int i = 0; i < 10; i++) {
				InstantSpeedData tmp = InstantSpeedData.Parse(intArray, index);
				data.intSpeedDataList.add(tmp);
				index += 7804;
			}
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<InstantSpeed>" + "\n");
		sb.append("intSpeedDataList = " + this.intSpeedDataList.size() + "\n");
		if (this.intSpeedDataList.size() > 0) {
			sb.append("intSpeedDataList1 = " + this.intSpeedDataList.get(0).toString() + "\n");
			sb.append("intSpeedDataListN = " + this.intSpeedDataList.get(this.intSpeedDataList.size() - 1).toString());
		}
		sb.append("</InstantSpeed>");
		return sb.toString();
	}
}
