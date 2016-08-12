package com.hantek.ttia.protocol.d3;

/**
 * 0x06, 送出GPS資訊收到
 */
public class SendGpsInfoAck extends Message {

	/**
	 * 0:成功 1~255:失敗之錯誤碼
	 */
	private int result;

	public int getResult() {
		return result;
	}

	public SendGpsInfoAck() {
		super(ETMMsgID.SendGpsInfoAck, FrameType.Ack);
	}

	public static SendGpsInfoAck parse(int[] intArray) {
		int index = 9;
		SendGpsInfoAck data = new SendGpsInfoAck();

		data.result = intArray[index++];

		return data;
	}

	@Override
	public String toString() {
		return "SendGpsInfoAck [result=" + result + "]";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<SendGpsInfoAck>" + "\n");
	// sb.append("result = " + this.result + "\n");
	// sb.append("</SendGpsInfoAck>" + "\n");
	// return sb.toString();
	// }
}
