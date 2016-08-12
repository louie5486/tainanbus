package com.hantek.ttia.protocol.d3;

/**
 * 0x07, 終止請求GPS資訊
 */
public class StopRequestGpsInfo extends Message {

	public StopRequestGpsInfo() {
		super(ETMMsgID.StopRequestGpsInfo, FrameType.Request);
	}

	public static StopRequestGpsInfo parse(int[] intArray) {
		StopRequestGpsInfo data = new StopRequestGpsInfo();
		return data;
	}

	@Override
	public String toString() {
		return "StopRequestGpsInfo []";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<StopRequestGpsInfo>" + "\n");
	// sb.append("</StopRequestGpsInfo>" + "\n");
	// return sb.toString();
	// }
}
