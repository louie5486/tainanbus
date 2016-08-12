package com.hantek.ttia.protocol.d3;

/**
 * 0x0D, 系統資料下載準備完成
 */
public class DataDownloadReady extends Message {

	/**
	 * 0:成功 1~255:失敗之錯誤碼
	 */
	private int result;

	public int getResult() {
		return result;
	}

	public DataDownloadReady() {
		super(ETMMsgID.DataDownloadReady, FrameType.Response);
	}

	public static DataDownloadReady parse(int[] intArray) {
		int index = 9;
		DataDownloadReady data = new DataDownloadReady();

		data.result = intArray[index++];

		return data;
	}

	@Override
	public String toString() {
		return "DataDownloadReady [result=" + result + "]";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<DataDownloadReady>" + "\n");
	// sb.append("</DataDownloadReady>" + "\n");
	// return sb.toString();
	// }
}
