package com.hantek.ttia.protocol.a1a4;

public enum DeviceModule {
	/**
	 * GPS
	 */
	GPS(0x01),

	/**
	 * LCD
	 */
	LCD(0x02),

	/**
	 * 站名顯示器
	 */
	LED(0x03),

	/**
	 * 行車紀錄器
	 */
	DCR(0x04),

	/**
	 * 電子票證機
	 */
	ETM(0x05);

	private int intValue;
	private static java.util.HashMap<Integer, DeviceModule> mappings;

	private static java.util.HashMap<Integer, DeviceModule> getMappings() {
		if (mappings == null) {
			synchronized (DeviceModule.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, DeviceModule>();
				}
			}
		}
		return mappings;
	}

	private DeviceModule(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static DeviceModule forValue(int value) {
		return getMappings().get(value);
	}
}
