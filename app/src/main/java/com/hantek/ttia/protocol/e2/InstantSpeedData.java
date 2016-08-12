package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * 停車時刻 7804 bytes
 */
public class InstantSpeedData {

	private long stopTime;

	private List<InstantSpeedDataList> instantSpeedDataList;

	public long getStopTime() {
		return stopTime;
	}

	public List<InstantSpeedDataList> getIntSpeedDataList() {
		return instantSpeedDataList;
	}

	public InstantSpeedData() {
		this.instantSpeedDataList = new LinkedList<InstantSpeedDataList>();
	}

	public static InstantSpeedData Parse(int[] intArray, int index) {
		InstantSpeedData data = new InstantSpeedData();

		data.stopTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		for (int i = 0; i < 300; i++) {
			try {
				InstantSpeedDataList tmp = InstantSpeedDataList.Parse(intArray, index);
				data.instantSpeedDataList.add(tmp);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				index += 26;
			}
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[stopTime=" + stopTime + "]\n");
		if (this.instantSpeedDataList.size() > 0) {
			sb.append("5min data1 " + this.instantSpeedDataList.get(0).toString() + "\n");
			sb.append("5min dataN " + this.instantSpeedDataList.get(this.instantSpeedDataList.size() - 1).toString());
		}
		return sb.toString();
	}
}
