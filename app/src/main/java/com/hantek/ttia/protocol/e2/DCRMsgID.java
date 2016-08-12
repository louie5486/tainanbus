package com.hantek.ttia.protocol.e2;

public enum DCRMsgID {

	/**
	 * 0xA1, 紀錄器基本資訊
	 */
	BasicData(0xA1),

	/**
	 * 0xA2, 最近2160h車輛行駛資料
	 */
	DrivingData(0xA2),

	/**
	 * 0xA3, 瞬時速度資料
	 */
	InstantSpeed(0xA3),

	/**
	 * 0xA4, 停車前5分鐘內每秒鐘最高速度
	 */
	HighSpeed(0xA4),

	/**
	 * 0xA5, 疲勞駕駛紀錄
	 */
	TiredDriving(0xA5),

	/**
	 * 0xA6, 讀取即時速度
	 */
	ReadIntSpeed(0xA6),

	/**
	 * 0xA7, 駕駛員休息紀錄
	 */
	DriverRest(0xA7),

	/**
	 * 0xA8, GPS位置、時間、方位資訊
	 */
	ReportGPS(0xA8);

	private int intValue;
	private static java.util.HashMap<Integer, DCRMsgID> mappings;

	public static java.util.HashMap<Integer, DCRMsgID> getMappings() {
		if (mappings == null) {
			synchronized (DCRMsgID.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, DCRMsgID>();
				}
			}
		}
		return mappings;
	}

	private DCRMsgID(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static DCRMsgID forValue(int value) {
		return getMappings().get(value);
	}
}
