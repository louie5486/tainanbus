package com.hantek.ttia.protocol.d3;

/**
 * 0xA2, 停止OD資料請求
 */
public class StopODDataReq extends Message {

	public StopODDataReq() {
		super(ETMMsgID.StopODDataReq, FrameType.Request);
	}

}
