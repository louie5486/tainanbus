package com.hantek.ttia.protocol.d3;

/**
 * 0x08, 終止請求GPS資訊收到
 */
public class StopRequestGpsInfoAck extends Message {

	private int result;

	public StopRequestGpsInfoAck(int result) {
		super(ETMMsgID.StopRequestGpsInfoAck, FrameType.Ack);
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
