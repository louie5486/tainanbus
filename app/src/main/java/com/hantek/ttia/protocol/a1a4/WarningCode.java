package com.hantek.ttia.protocol.a1a4;

public enum WarningCode {
	/**
	 * 恢復正常
	 */
	Recovery(0x00),

	/**
	 * 模組無回應
	 */
	ModuleNoResponse(0x01),

	/**
	 * 天線不良
	 */
	PoorAntenna(0x02);

	private int intValue;
	private static java.util.HashMap<Integer, WarningCode> mappings;

	private static java.util.HashMap<Integer, WarningCode> getMappings() {
		if (mappings == null) {
			synchronized (WarningCode.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, WarningCode>();
				}
			}
		}
		return mappings;
	}

	private WarningCode(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static WarningCode forValue(int value) {
		return getMappings().get(value);
	}
}
