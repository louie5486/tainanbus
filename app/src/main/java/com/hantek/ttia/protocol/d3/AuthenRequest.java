package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x01, 身分認證要求
 */
public class AuthenRequest extends Message {

	private int operatorAuthenType;
	private int operatorType;
	private long operatorID;
	private int passwordLen;
	private String password;

	public AuthenRequest(int operatorAuthenType, int operatorType, long operatorID, String password) {
		super(ETMMsgID.AuthenRequest, FrameType.Request);
		this.operatorAuthenType = operatorAuthenType;
		this.operatorType = operatorType;
		this.operatorID = operatorID;
		this.password = password;
	}

	@Override
	public byte[] getBytes() {
		byte[] tmpPasswordArray = this.password.getBytes();
		this.passwordLen = tmpPasswordArray.length;

		byte[] payload = new byte[7 + this.passwordLen];
		int index = 0;
		payload[index++] = (byte) this.operatorAuthenType;
		payload[index++] = (byte) this.operatorType;
		System.arraycopy(BitConverter.toUIntegerByteArray(this.operatorID), 0, payload, index, BitConverter.UintSIZE);
		index += BitConverter.UintSIZE;

		payload[index++] = (byte) this.passwordLen;
		System.arraycopy(tmpPasswordArray, 0, payload, index, tmpPasswordArray.length);
		index += tmpPasswordArray.length;

		this.setPayload(payload);
		return super.getBytes();
	}

}
