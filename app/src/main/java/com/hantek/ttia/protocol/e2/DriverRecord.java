package com.hantek.ttia.protocol.e2;

import java.util.Arrays;

import com.hantek.ttia.module.BitConverter;

/**
 * 駕駛員紀錄
 */
public class DriverRecord {
	/**
	 * 紀錄器即時時間
	 */
	private long time;

	/**
	 * 車牌號碼
	 */
	private char[] carNo;

	/**
	 * 特徵係數
	 */
	private long carParameter;

	/**
	 * 駕駛員(證)號碼
	 */
	private char[] driverID;

	/**
	 * 駕駛員姓名
	 */
	private char[] driverName;

	/**
	 * 駕駛員登入時間
	 */
	private long driverLogonTime;

	/**
	 * 駕駛員登出時間
	 */
	private long driverLogoutTime;

	public long getTime() {
		return time;
	}

	public char[] getCarNo() {
		return carNo;
	}

	public long getCarParameter() {
		return carParameter;
	}

	public char[] getDriverID() {
		return driverID;
	}

	public char[] getDriverName() {
		return driverName;
	}

	public long getDriverLogonTime() {
		return driverLogonTime;
	}

	public long getDriverLogoutTime() {
		return driverLogoutTime;
	}

	public DriverRecord() {
		this.carNo = new char[8];
		this.driverName = new char[20];
	}

	public static DriverRecord parse(int[] intArray, int index) {
		DriverRecord data = new DriverRecord();

		data.time = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		for (int i = 0; i < 8; i++) {
			try {
				data.carNo[i] = (char) intArray[index];
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				index++;
			}
		}

		data.carParameter = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.driverID = new char[10];
		for (int i = 0; i < 10; i++) {
			try {
				data.driverID[i] = (char) intArray[index];
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				index++;
			}
		}

		for (int i = 0; i < 20; i++) {
			try {
				data.driverName[i] = (char) intArray[index];
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				index++;
			}
		}

		data.driverLogonTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.driverLogoutTime = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		return data;
	}

	@Override
	public String toString() {
		return "[time=" + time + ", carNo=" + Arrays.toString(carNo) + ", carParameter=" + carParameter + ", driverID=" + Arrays.toString(driverID) + ", driverName=" + Arrays.toString(driverName)
				+ ", driverLogonTime=" + driverLogonTime + ", driverLogoutTime=" + driverLogoutTime + "]";
	}

}
