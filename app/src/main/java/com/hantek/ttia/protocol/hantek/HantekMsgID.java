package com.hantek.ttia.protocol.hantek;

public enum HantekMsgID {
	/**
	 * 0xE0, 業者自行定義(保留)
	 */
	Reserved1(0xE0),

	/**
	 * 0xE1, 業者自行定義(保留)
	 */
	Reserved2(0xE1),

	/**
	 * 0xE2, 業者自行定義(保留)
	 */
	Reserved3(0xE2),

	/**
	 * 0xE3, 業者自行定義(保留)
	 */
	Reserved4(0xE3),

	/**
	 * 0xE4, 業者自行定義(保留)
	 */
	Reserved5(0xE4),

	/**
	 * 0xE5, 業者自行定義(保留)
	 */
	Reserved6(0xE5),

	/**
	 * 0xE6, 業者自行定義(保留)
	 */
	Reserved7(0xE6),

	/**
	 * 0xE7, 業者自行定義(保留)
	 */
	Reserved8(0xE7),

	/**
	 * 0xE8, 業者自行定義(保留)
	 */
	Reserved9(0xE8),

	/**
	 * 0xE9, 業者自行定義(保留)
	 */
	Reserved10(0xE9),

	/**
	 * 0xEA, 業者自行定義(保留)
	 */
	Reserved11(0xEA),

	/**
	 * 0xEB, 業者自行定義(保留)
	 */
	Reserved12(0xEB),

	/**
	 * 0xEC, 業者自行定義(保留)
	 */
	Reserved13(0xEC),

	/**
	 * 0xED, 業者自行定義(保留)
	 */
	Reserved14(0xED),

	/**
	 * 0xEE, 業者自行定義(保留)
	 */
	Reserved15(0xEE),

	/**
	 * 0xEF, 業者自行定義(保留)
	 */
	Reserved16(0xEF);

	private int intValue;
	private static java.util.HashMap<Integer, HantekMsgID> mappings;

	public static java.util.HashMap<Integer, HantekMsgID> getMappings() {
		if (mappings == null) {
			synchronized (HantekMsgID.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, HantekMsgID>();
				}
			}
		}
		return mappings;
	}

	private HantekMsgID(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static HantekMsgID forValue(int value) {
		return getMappings().get(value);
	}
}
