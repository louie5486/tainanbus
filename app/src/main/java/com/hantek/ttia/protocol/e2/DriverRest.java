package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * A7h, 駕駛員休息紀錄
 */
public class DriverRest extends Message {

	private List<DriverRestRecord> driverRestList;

	public DriverRest() {
		super(DCRMsgID.DriverRest);
		this.driverRestList = new LinkedList<DriverRestRecord>();
	}

	public static DriverRest parse(int[] intArray) {
		long len = BitConverter.toUInteger(intArray, 4);

		int index = 8; // data block
		DriverRest data = new DriverRest();
		if (len > 0) {
			int tmpIndex = 0;
			do {
				data.driverRestList.add(DriverRestRecord.parse(intArray, index));
				index += 27;
				tmpIndex += 27;
			} while (tmpIndex < len);
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<DriverRest>\n");
		if (this.driverRestList.size() > 0) {
			for (int i = 0; i < this.driverRestList.size(); i++) {
				sb.append(String.format("Record(%s) %s\n", i + 1, this.driverRestList.get(i).toString()));
			}
		}
		sb.append("</DriverRest>");
		return sb.toString();
	}
}
