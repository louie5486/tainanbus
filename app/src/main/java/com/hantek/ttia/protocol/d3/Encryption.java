package com.hantek.ttia.protocol.d3;

/**
 * 密文旗標, 上傳之資料是否要加密
 */
public enum Encryption {

	/**
	 * 0x00 明文
	 */
	Plaintext(0x00),

	/**
	 * 0x01 密文
	 */
	Ciphertext(0x01);

	private int intValue;
	private static java.util.HashMap<Integer, Encryption> mappings;

	private static java.util.HashMap<Integer, Encryption> getMappings() {
		if (mappings == null) {
			synchronized (ETMMsgID.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, Encryption>();
				}
			}
		}
		return mappings;
	}

	private Encryption(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static Encryption forValue(int value) {
		return getMappings().get(value);
	}
}
