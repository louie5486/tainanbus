package com.hantek.ttia.protocol.d1;

public class SpecialMessage {

	/**
	 * 顯示暫停(55P)、繼續(55O)、結束(55Q)
	 */
	private final String command = "55";

	/**
	 * 動畫控制
	 */
	public SpecialCommand animation;

	/**
	 * 檢查碼
	 */
	private int checksum = 0;

	public SpecialMessage(SpecialCommand command) {
		this.animation = command;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[6];
		int index = 0;

		bytes[index++] = 0x01;
		checksum(0x01);

		for (byte b : this.command.getBytes()) {
			bytes[index++] = b;
			checksum(b & 0xff);
		}
		bytes[index++] = (byte) this.animation.getValue();
		checksum(this.animation.getValue() & 0xff);

		bytes[index++] = 0x02;
		checksum(0x02);

		bytes[index++] = (byte) this.checksum;

		return bytes;
	}

	private void checksum(int b) {
		this.checksum = this.checksum ^ b;
	}
}
