package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x0B, 上傳系統資料收到
 */
public class UploadDataAck extends Message {

	private int result;
	private int serialNumber;

	public UploadDataAck(int result, int serialNumber) {
		super(ETMMsgID.UploadDataAck, FrameType.Ack);
		this.result = result;
		this.serialNumber = serialNumber;
	}

	@Override
	public byte[] getBytes() {
		byte[] payload = new byte[3];
		int index = 0;

		payload[index++] = (byte) this.result;

		System.arraycopy(BitConverter.toUShortByteArray(this.serialNumber), 0, payload, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		this.setPayload(payload);
		return super.getBytes();
	}
}
