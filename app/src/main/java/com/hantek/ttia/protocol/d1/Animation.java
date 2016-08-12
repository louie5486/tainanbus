package com.hantek.ttia.protocol.d1;

public enum Animation {

	/**
	 * 左移進入
	 */
	Left(0x41),

	/**
	 * 上移進入
	 */
	Top(0x42),

	/**
	 * 下移進入
	 */
	Bottom(0x43),

	/**
	 * 立即顯示
	 */
	Now(0x44);

	private int intValue;
	private static java.util.HashMap<Integer, Animation> mappings;

	private static java.util.HashMap<Integer, Animation> getMappings() {
		if (mappings == null) {
			synchronized (Animation.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, Animation>();
				}
			}
		}
		return mappings;
	}

	private Animation(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static Animation forValue(int value) {
		return getMappings().get(value);
	}
}
