package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x03, 開始請求GPS資訊
 */
public class StartRequestGpsInfo extends Message {

	/**
	 * GPS座標資訊回報時間間隔, 單位為秒(sec)
	 */
	private int gpsInfoReportInternal;

	/**
	 * 0:代表GPS座標資訊回報次數無限制, 回報座標直到驗票模組下達"終止請求GPS資訊"為止 1~65535:代表要求車載機回報GPS座標資訊次數, 車機回報次數到達此數目後即自動停止再回報GPS座標
	 */
	private int numOfReport;

	public int getGpsInfoReportInternal() {
		return gpsInfoReportInternal;
	}

	public int getNumOfReport() {
		return numOfReport;
	}

	public StartRequestGpsInfo() {
		super(ETMMsgID.StartRequestGpsInfo, FrameType.Request);
	}

	public static StartRequestGpsInfo parse(int[] intArray) {
		int index = 9;
		StartRequestGpsInfo data = new StartRequestGpsInfo();

		data.gpsInfoReportInternal = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		data.numOfReport = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		return data;
	}

	@Override
	public String toString() {
		return "StartRequestGpsInfo [gpsInfoReportInternal=" + gpsInfoReportInternal + ", numOfReport=" + numOfReport + "]";
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<StartRequestGpsInfo>" + "\n");
	// sb.append("gpsInfoReportInternal = " + this.gpsInfoReportInternal + "\n");
	// sb.append("numOfReport = " + this.numOfReport + "\n");
	// sb.append("</StartRequestGpsInfo>" + "\n");
	// return sb.toString();
	// }
}
