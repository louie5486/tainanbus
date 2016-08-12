package com.hantek.ttia.protocol.d3;

/**
 * 0xA3, 停止OD資料請求收到
 */
public class StopODDataAck extends Message {

	/**
	 * 0:成功 1~255:失敗之錯誤碼
	 */
	private int result;

	public int getResult() {
		return result;
	}

	public StopODDataAck() {
		super(ETMMsgID.StopODDataAck, FrameType.Ack);
	}

	public static Message parse(int[] intArray) {
		int index = 9;
		StopODDataAck data = new StopODDataAck();

		data.result = intArray[index++];

		return data;
	}

	@Override
	public String toString() {
		return "StopODDataAck [result=" + result + "]";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<StopODDataAck>" + "\n");
	// sb.append("</StopODDataAck>" + "\n");
	// return sb.toString();
	// }
}
