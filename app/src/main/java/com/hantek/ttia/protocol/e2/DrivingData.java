package com.hantek.ttia.protocol.e2;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.module.BitConverter;

/**
 * A2h 最近2,160h 車輛行駛資料
 */
public class DrivingData extends Message {

	/**
	 * 資料即時起始時間
	 */
	private long startTime;

	/**
	 * 2,160h 累計行駛里程
	 */
	private int totalMilieage;

	/**
	 * 129,600 筆速度資料
	 */
	private List<DriveData> driveDataList;

	public long getStartTime() {
		return startTime;
	}

	public int getTotalMilieage() {
		return totalMilieage;
	}

	public List<DriveData> getDriveDataList() {
		return driveDataList;
	}

	public DrivingData() {
		super(DCRMsgID.DrivingData);
		this.driveDataList = new LinkedList<DriveData>();
	}

	public static DrivingData parse(int[] intArray) {
		long len = BitConverter.toUInteger(intArray, 4);

		int index = 8; // data block
		DrivingData data = new DrivingData();
		if (len > 0) {
			data.startTime = BitConverter.toUInteger(intArray, index);
			index += BitConverter.UintSIZE;

			data.totalMilieage = BitConverter.toUShort(intArray, index);
			index += BitConverter.UshortSIZE;

			while (index < len + 8) {
				try {
					DriveData tmp = new DriveData(intArray[index], intArray[index + 1]);
					data.driveDataList.add(tmp);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					index += 2;
				}
			}
		}

		return data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<DrivingData>" + "\n");
		sb.append("startTime = " + this.toDateTime(this.startTime).toString() + "\n");
		sb.append("totalMilieage = " + this.totalMilieage + "\n");
		sb.append("driveDataList = " + this.driveDataList.size() + "\n");
		if (this.driveDataList.size() > 0) {
			sb.append("driveData1 " + this.driveDataList.get(0).toString() + "\n");
			sb.append("driveDataN " + this.driveDataList.get(this.driveDataList.size() - 1).toString() + "\n");
		}
		sb.append("</DrivingData>");
		return sb.toString();
	}
}