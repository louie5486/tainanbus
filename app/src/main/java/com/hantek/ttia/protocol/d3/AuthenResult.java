package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x02, 身分認證結果
 */
public class AuthenResult extends Message {

	/**
	 * 0:成功 1~255:失敗之錯誤碼
	 */
	private int result;

	/**
	 * 操作人員代碼
	 */
	private long operatorID;

	/**
	 * 操作人員卡片之卡號
	 */
	private long operatorCardID;

	public int getResult() {
		return result;
	}

	public long getOperatorID() {
		return operatorID;
	}

	public long getOperatorCardID() {
		return operatorCardID;
	}

	public AuthenResult() {
		super(ETMMsgID.AuthenResult, FrameType.Response);
	}

	public static AuthenResult parse(int[] intArray) {
		int index = 9;
		AuthenResult data = new AuthenResult();

		data.result = intArray[index++];

		data.operatorID = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		data.operatorCardID = BitConverter.toUInteger(intArray, index);
		index += BitConverter.UintSIZE;

		return data;
	}

	@Override
	public String toString() {
		return "AuthenResult [result=" + result + ", operatorID=" + operatorID + ", operatorCardID=" + operatorCardID + "]";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<AuthenResult>" + "\n");
	// sb.append("result = " + this.result + "\n");
	// sb.append("operatorID = " + this.operatorID + "\n");
	// sb.append("operatorCardID = " + this.operatorCardID + "\n");
	// sb.append("</AuthenResult>" + "\n");
	// return sb.toString();
	// }
}
