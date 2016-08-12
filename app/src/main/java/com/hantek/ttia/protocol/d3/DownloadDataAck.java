package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x0F, 下載系統資料收到
 */
public class DownloadDataAck extends Message {

	/**
	 * 0:成功 1~255:失敗之錯誤碼
	 */
	private int result;

	/**
	 * 訊息序號
	 */
	private int serialNumber;

	public int getResult() {
		return result;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public DownloadDataAck() {
		super(ETMMsgID.DownloadDataAck, FrameType.Ack);
	}

	public static DownloadDataAck parse(int[] intArray) {
		int index = 9;
		DownloadDataAck data = new DownloadDataAck();

		data.result = intArray[index++];

		data.serialNumber = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		return data;
	}

	@Override
	public String toString() {
		return "DownloadDataAck [result=" + result + ", serialNumber=" + serialNumber + "]";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<DownloadDataAck>" + "\n");
	// sb.append("</DownloadDataAck>" + "\n");
	// return sb.toString();
	// }
}
