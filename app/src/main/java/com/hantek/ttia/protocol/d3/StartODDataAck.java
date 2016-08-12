package com.hantek.ttia.protocol.d3;

/**
 * 0xA1, 開始OD資料請求收到
 */
public class StartODDataAck extends Message {

	/**
	 * 0:成功 1~255:失敗之錯誤碼
	 */
	private int result;

	public int getResult() {
		return result;
	}

	public StartODDataAck() {
		super(ETMMsgID.StartODDataAck, FrameType.Ack);
	}

	public static StartODDataAck parse(int[] intArray) {
		int index = 9;
		StartODDataAck data = new StartODDataAck();

		data.result = intArray[index++];

		return data;
	}

	@Override
	public String toString() {
		return "StartODDataAck [result=" + result + "]";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<StartODDataAck>" + "\n");
	// sb.append("</StartODDataAck>" + "\n");
	// return sb.toString();
	// }
}
