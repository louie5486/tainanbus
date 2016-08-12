package com.hantek.ttia.protocol.d3;

/**
 * 0x05, 送出GPS資訊
 */
public class SendGpsInfo extends Message {

	private GpsInfo gpsInfo;

	public SendGpsInfo(GpsInfo gpsinfo) {
		super(ETMMsgID.SendGpsInfo, FrameType.Request);
		this.gpsInfo = gpsinfo;
	}

	@Override
	public byte[] getBytes() {
		byte[] payload = this.gpsInfo.getBytes();
		this.setPayload(payload);
		return super.getBytes();
	}
}
