package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * A5h疲勞駕駛紀錄
 */
public class TiredDriving extends Message {

	private List<TiredRecord> tiredList;

	public List<TiredRecord> getTiredList() {
		return tiredList;
	}

	public TiredDriving() {
		super(DCRMsgID.TiredDriving);
		this.tiredList = new LinkedList<TiredRecord>();
	}

	public static TiredDriving parse(int[] intArray) {
		long len = BitConverter.toUInteger(intArray, 4);

		int index = 8; // data block
		TiredDriving data = new TiredDriving();
		if (len > 0) {
			int tmpIndex = 0;
			do {
				data.tiredList.add(TiredRecord.parse(intArray, index));
				index += 35;
				tmpIndex += 35;
			} while (tmpIndex < len);
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<TiredDriving>\n");
		if (this.tiredList.size() > 0) {
			for (int i = 0; i < this.tiredList.size(); i++) {
				sb.append(String.format("Record(%s) %s\n", i + 1, this.tiredList.get(i).toString()));
			}
		}
		sb.append("</TiredDriving>");
		return sb.toString();
	}
}
