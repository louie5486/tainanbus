package com.hantek.ttia.protocol.d3;

/**
 * 0x04,開始請求GPS資訊收到
 */
public class StartRequestGpsInfoAck extends Message {

	private int result;

	public StartRequestGpsInfoAck(int result) {
		super(ETMMsgID.StartRequestGpsInfoAck, FrameType.Ack);
		this.result = result;
	}

	@Override
	public byte[] getBytes() {
		byte[] payload = new byte[1];
		payload[0] = (byte) this.result;
		this.setPayload(payload);
		return super.getBytes();
	}

}
