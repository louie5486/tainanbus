package com.hantek.ttia.protocol.a1a4;

public class TimeStruct {
	public static final short Length = 6;

	/**
	 * UTC時間之年, 西元2000年起始(2009->9)
	 */
	public byte year;

	/**
	 * UTC時間之月
	 */
	public byte month;

	/**
	 * UTC時間之日
	 */
	public byte day;

	/**
	 * UTC時間之時
	 */
	public byte hour;

	/**
	 * UTC時間之分
	 */
	public byte minute;

	/**
	 * UTC時間之秒
	 */
	public byte second;

	public TimeStruct() {
		this.year = 0;
		this.month = 0;
		this.day = 0;
		this.hour = 0;
		this.minute = 0;
		this.second = 0;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[Length];
		int index = 0;

		bytes[index++] = this.year;
		bytes[index++] = this.month;
		bytes[index++] = this.day;
		bytes[index++] = this.hour;
		bytes[index++] = this.minute;
		bytes[index++] = this.second;

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<TimeStruct>" + "\n");
		sb.append("year=" + this.year + " ");
		sb.append("month=" + this.month + " ");
		sb.append("day=" + this.day + " ");
		sb.append("hour=" + this.hour + " ");
		sb.append("minute=" + this.minute + " ");
		sb.append("second=" + this.second + "\n");
		sb.append("</TimeStruct>" + "\n");
		return sb.toString();
	}

	public TimeStruct clone() {
		TimeStruct varCopy = new TimeStruct();

		varCopy.year = this.year;
		varCopy.month = this.month;
		varCopy.day = this.day;
		varCopy.hour = this.hour;
		varCopy.minute = this.minute;
		varCopy.second = this.second;

		return varCopy;
	}
}
