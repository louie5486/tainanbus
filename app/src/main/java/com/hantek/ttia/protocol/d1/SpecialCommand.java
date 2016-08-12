package com.hantek.ttia.protocol.d1;

public enum SpecialCommand {

	/**
	 * 顯示暫停
	 */
	Pause(0x50),

	/**
	 * 顯示繼續
	 */
	Resume(0x4F),

	/**
	 * 強制結束
	 */
	Stop(0x51);

	private int intValue;
	private static java.util.HashMap<Integer, SpecialCommand> mappings;

	private static java.util.HashMap<Integer, SpecialCommand> getMappings() {
		if (mappings == null) {
			synchronized (SpecialCommand.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, SpecialCommand>();
				}
			}
		}
		return mappings;
	}

	private SpecialCommand(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static SpecialCommand forValue(int value) {
		return getMappings().get(value);
	}
}
