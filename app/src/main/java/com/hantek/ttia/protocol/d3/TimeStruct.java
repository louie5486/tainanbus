package com.hantek.ttia.protocol.d3;

public class TimeStruct {
	public static final short Length = 6;

	/**
	 * UTC時間之年, 西元2000年起始(2009->9)
	 */
	public int year;

	/**
	 * UTC時間之月
	 */
	public int month;

	/**
	 * UTC時間之日
	 */
	public int day;

	/**
	 * UTC時間之時
	 */
	public int hour;

	/**
	 * UTC時間之分
	 */
	public int minute;

	/**
	 * UTC時間之秒
	 */
	public int second;

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

		bytes[index++] = (byte) this.year;
		bytes[index++] = (byte) this.month;
		bytes[index++] = (byte) this.day;
		bytes[index++] = (byte) this.hour;
		bytes[index++] = (byte) this.minute;
		bytes[index++] = (byte) this.second;

		return bytes;
	}

	public static TimeStruct parse(int[] intArray, int index) {
		TimeStruct time = new TimeStruct();
		time.year = intArray[index++];
		time.month = intArray[index++];
		time.day = intArray[index++];
		time.hour = intArray[index++];
		time.minute = intArray[index++];
		time.second = intArray[index++];
		return time;
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<TimeStruct>" + "\n");
	// sb.append("year=" + this.year + " ");
	// sb.append("month=" + this.month + " ");
	// sb.append("day=" + this.day + " ");
	// sb.append("hour=" + this.hour + " ");
	// sb.append("minute=" + this.minute + " ");
	// sb.append("second=" + this.second + "\n");
	// sb.append("</TimeStruct>" + "\n");
	// return sb.toString();
	// }

	@Override
	public String toString() {
		return " [year=" + year + ", month=" + month + ", day=" + day + ", hour=" + hour + ", minute=" + minute + ", second=" + second + "]";
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
