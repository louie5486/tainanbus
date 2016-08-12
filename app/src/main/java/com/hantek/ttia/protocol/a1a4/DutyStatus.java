package com.hantek.ttia.protocol.a1a4;

public enum DutyStatus {
	/**
	 * 正常
	 */
	Ready(0x01), // 可開啟與解除此裝態

	/**
	 * 開始
	 */
	Start(0x02), // 可開啟與解除此裝態

	/**
	 * 結束
	 */
	Stop(0x04), // 可開啟與解除此裝態

	/**
	 * 客滿
	 */
	Full(0x08), // 可開啟與解除此裝態

	/**
	 * 包車出租
	 */
	OnRoad(0x10); // 可開啟與解除此裝態

	private int intValue;
	private static java.util.HashMap<Integer, DutyStatus> mappings;

	private static java.util.HashMap<Integer, DutyStatus> getMappings() {
		if (mappings == null) {
			synchronized (DutyStatus.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, DutyStatus>();
				}
			}
		}
		return mappings;
	}

	private DutyStatus(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static DutyStatus forValue(int value) {
		return getMappings().get(value);
	}
}
