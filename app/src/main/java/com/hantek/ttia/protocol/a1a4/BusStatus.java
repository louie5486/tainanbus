package com.hantek.ttia.protocol.a1a4;

public enum BusStatus {
	/**
	 * 正常
	 */
	OnRoad(0x01), // 可開啟與解除此裝態

	/**
	 * 車禍
	 */
	Accident(0x02), // 可開啟與解除此裝態

	/**
	 * 故障
	 */
	BreakDown(0x04), // 可開啟與解除此裝態

	/**
	 * 塞車
	 */
	Jam(0x08), // 可開啟與解除此裝態

	/**
	 * 緊急求援
	 */
	Emergency(0x10), // 可開啟與解除此裝態

	/**
	 * 加油洗車
	 */
	OnService(0x20), // 可開啟與解除此裝態

	/**
	 * 非營運
	 */
	Offline(0x40); // 可開啟與解除此裝態

	private int intValue;
	private static java.util.HashMap<Integer, BusStatus> mappings;

	private static java.util.HashMap<Integer, BusStatus> getMappings() {
		if (mappings == null) {
			synchronized (BusStatus.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, BusStatus>();
				}
			}
		}
		return mappings;
	}

	private BusStatus(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static BusStatus forValue(int value) {
		return getMappings().get(value);
	}
}
