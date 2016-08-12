package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * A1h 紀錄器基本資訊
 */
public class BasicData extends Message {

	private List<DriverRecord> driverDataList;

	public List<DriverRecord> getDriverDataList() {
		return driverDataList;
	}

	public BasicData() {
		super(DCRMsgID.BasicData);
		this.driverDataList = new LinkedList<DriverRecord>();
	}

	public static BasicData parse(int[] intArray) {
		long len = BitConverter.toUInteger(intArray, 4);

		int index = 8; // data block
		BasicData data = new BasicData();
		if (len > 0) {
			int tmpIndex = 0;
			do {
				data.driverDataList.add(DriverRecord.parse(intArray, index));
				index += 54;
				tmpIndex += 54;
			} while (tmpIndex < len);
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<BasicData>" + "\n");
		if (this.driverDataList.size() > 0) {
			for (int i = 0; i < this.driverDataList.size(); i++) {
				sb.append(String.format("Record(%s) %s\n", i + 1, this.driverDataList.get(i).toString()));
			}
		}
		sb.append("</BasicData>");
		return sb.toString();
	}
}
