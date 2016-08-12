package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * A6h, 讀取即時速度
 */
public class ReadIntSpeed extends Message {

	// send
	private int second;

	private List<SpeedData> speedDataList;

	public List<SpeedData> getSpeedDataList() {
		return speedDataList;
	}

	public ReadIntSpeed() {
		super(DCRMsgID.ReadIntSpeed);
		this.speedDataList = new LinkedList<SpeedData>();
	}

	public ReadIntSpeed(int second) {
		super(DCRMsgID.ReadIntSpeed);
		this.second = second;
	}

	@Override
	public byte[] getBytes() {
		setDataBlock(BitConverter.toUShortByteArray(this.second));
		return super.getBytes();
	}

	public static ReadIntSpeed parse(int[] intArray) {
		long len = BitConverter.toUInteger(intArray, 4);

		int index = 8; // data block
		ReadIntSpeed data = new ReadIntSpeed();
		if (len > 0) {
			int tmpIndex = 0;
			do {
				data.speedDataList.add(SpeedData.parse(intArray, index));
				index += 20;
				tmpIndex += 20;
			} while (tmpIndex < len);
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<ReadIntSpeed>\n");
		if (this.speedDataList.size() > 0) {
			for (int i = 0; i < this.speedDataList.size(); i++) {
				sb.append(String.format("Record(%s) %s\n", i + 1, this.speedDataList.get(i).toString()));
			}
		}
		sb.append("</ReadIntSpeed>");
		return sb.toString();
	}
}
