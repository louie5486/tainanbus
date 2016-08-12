package com.hantek.ttia.protocol.d3;

/**
 * 0xA5, OD資料收到
 */
public class ODDataAck extends Message {

	private int result;

	public ODDataAck(int result) {
		super(ETMMsgID.ODDataAck, FrameType.Ack);
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
