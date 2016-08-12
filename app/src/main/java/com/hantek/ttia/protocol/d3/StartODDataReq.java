package com.hantek.ttia.protocol.d3;

/**
 * 0xA0, 開始OD資料請求
 */
public class StartODDataReq extends Message {

	private Encryption encryptionFlag;
	private int reportFlag;

	public StartODDataReq(Encryption encryptionFlag, int reportFlag) {
		super(ETMMsgID.StartODDataReq, FrameType.Request);
		this.encryptionFlag = encryptionFlag;
		this.reportFlag = reportFlag;
	}

	@Override
	public byte[] getBytes() {
		byte[] payload = new byte[2];
		int index = 0;

		payload[index++] = (byte) this.encryptionFlag.getValue();
		payload[index++] = (byte) this.reportFlag;

		this.setPayload(payload);
		return super.getBytes();
	}

}
