package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * 停車時刻 7804 bytes
 */
public class InstantSpeedDataList {

	private int longitude;
	private int lateitude;
	private int gpsAngle;
	private int gpsSpeed;
	private List<InstantSpeedDataListData> intSpeedDataList;

	public int getLongitude() {
		return longitude;
	}

	public int getLateitude() {
		return lateitude;
	}

	public int getGpsAngle() {
		return gpsAngle;
	}

	public int getGpsSpeed() {
		return gpsSpeed;
	}

	public List<InstantSpeedDataListData> getIntSpeedDataList() {
		return intSpeedDataList;
	}

	public InstantSpeedDataList() {
		this.intSpeedDataList = new LinkedList<InstantSpeedDataListData>();
	}

	public static InstantSpeedDataList Parse(int[] intArray, int index) {
		InstantSpeedDataList data = new InstantSpeedDataList();

		data.longitude = BitConverter.byteArrayToInt(intArray, index);
		index += BitConverter.IntSIZE;

		data.lateitude = BitConverter.byteArrayToInt(intArray, index);
		index += BitConverter.IntSIZE;

		data.gpsAngle = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		data.gpsSpeed = intArray[index++];

		for (int i = 0; i < 5; i++) {
			try {
				InstantSpeedDataListData tmp = new InstantSpeedDataListData(intArray[index], intArray[index + 1], intArray[index + 2]);
				data.intSpeedDataList.add(tmp);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				index += 3;
			}
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[longitude=" + longitude + ", lateitude=" + lateitude + ", gpsAngle=" + gpsAngle + ", gpsSpeed=" + gpsSpeed + "]\n");
		if (this.intSpeedDataList.size() > 0) {
			sb.append("Speed1 " + intSpeedDataList.get(0).toString() + "\n");
			sb.append("SpeedN " + intSpeedDataList.get(intSpeedDataList.size() - 1).toString());
		}
		return sb.toString();

	}
}
