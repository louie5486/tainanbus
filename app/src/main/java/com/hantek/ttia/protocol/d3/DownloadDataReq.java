package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x0C, 系統資料下載要求
 */
public class DownloadDataReq extends Message {

	private int dataID;
	private Encryption encryptionFlag;

	public DownloadDataReq(int dataID, Encryption encryptionFlag) {
		super(ETMMsgID.DownloadDataReq, FrameType.Request);
		this.dataID = dataID;
		this.encryptionFlag = encryptionFlag;
	}

	@Override
	public byte[] getBytes() {
		byte[] payload = new byte[3];
		int index = 0;

		System.arraycopy(BitConverter.toUShortByteArray(this.dataID), 0, payload, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		payload[index++] = (byte) this.encryptionFlag.getValue();

		this.setPayload(payload);
		return super.getBytes();
	}
}
