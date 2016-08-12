package com.hantek.ttia.protocol.d1;

import java.io.UnsupportedEncodingException;

public class Message {
	/**
	 * 位址 "00" ~ "09"
	 */
	private String command;

	/**
	 * 動畫控制
	 */
	private Animation animation;

	/**
	 * 效果控制 第一碼 "0" - "1"，字串停留不繼續下一筆。"1"為永久停留。
	 */
	private char run;

	/**
	 * 效果控制 第二碼 "0" ~ "F"，字串停留時間。單位為秒(若第一碼為"1")，則本命令碼無效。
	 */
	private char stayTime;

	/**
	 * 效果控制 第三碼 "0" ~ "1"，字串停留時閃爍。"0"為不閃，"1"為閃爍。
	 */
	private char twinkling;

	/**
	 * 效果控制 第四碼 保留碼。"0"
	 */
	private char reserved;

	/**
	 * 顯示資料碼
	 */
	private String Data;

	/**
	 * 檢查碼
	 */
	private int checksum = 0;

	public Message(String cmd, Animation ani, char c1, char c2, char c3) {
		this.command = cmd;
		this.animation = ani;
		this.run = c1;
		this.stayTime = c2;
		this.twinkling = c3;
		this.reserved = '0';
	}

	public void setData(String data) {
		this.Data = data;
	}

	public byte[] getBytes() {
		try {
			byte[] dataBytes = this.Data.getBytes("big5");

			byte[] bytes = new byte[10 + dataBytes.length];
			int index = 0;
			this.checksum = 0;

			bytes[index++] = 0x01; // 起始
			this.checksum(0x01);

			for (byte b : this.command.getBytes()) {
				bytes[index++] = b;
				this.checksum(b & 0xff);
			}
			bytes[index++] = (byte) this.animation.getValue();
			this.checksum(this.animation.getValue() & 0xff);

			bytes[index++] = (byte) this.run;
			this.checksum(this.run & 0xff);

			bytes[index++] = (byte) this.stayTime;
			this.checksum(this.stayTime & 0xff);

			bytes[index++] = (byte) this.twinkling;
			this.checksum(this.twinkling & 0xff);

			bytes[index++] = (byte) this.reserved;
			this.checksum(this.reserved & 0xff);

			for (int i = 0; i < dataBytes.length; i++) {
				bytes[index++] = dataBytes[i];
				this.checksum(dataBytes[i] & 0xff);
			}

			bytes[index++] = 0x02; // 結束
			this.checksum(0x02);

			bytes[index++] = (byte) (this.checksum);

			return bytes;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void checksum(int b) {
		this.checksum = this.checksum ^ b;
	}
}
