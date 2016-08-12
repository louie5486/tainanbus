package com.hantek.ttia.protocol.d3;

public enum ETMMsgID {
	/**
	 * 0x01, 身分認證要求
	 */
	AuthenRequest(0x01),

	/**
	 * 0x02, 身分認證結果
	 */
	AuthenResult(0x02),

	/**
	 * 0x03, 開始請求GPS資訊
	 */
	StartRequestGpsInfo(0x03),

	/**
	 * 0x04,開始請求GPS資訊收到
	 */
	StartRequestGpsInfoAck(0x04),

	/**
	 * 0x05, 送出GPS資訊
	 */
	SendGpsInfo(0x05),

	/**
	 * 0x06, 送出GPS資訊收到
	 */
	SendGpsInfoAck(0x06),

	/**
	 * 0x07, 終止請求GPS資訊
	 */
	StopRequestGpsInfo(0x07),

	/**
	 * 0x08, 終止請求GPS資訊收到
	 */
	StopRequestGpsInfoAck(0x08),

	/**
	 * 0x09, 系統資料上傳要求
	 */
	UploadDataReq(0x09),

	/**
	 * 0x0A, 上傳系統資料
	 */
	UploadData(0x0A),

	/**
	 * 0x0B, 上傳系統資料收到
	 */
	UploadDataAck(0x0B),

	/**
	 * 0x0C, 系統資料下載要求
	 */
	DownloadDataReq(0x0C),

	/**
	 * 0x0D, 系統資料下載準備完成
	 */
	DataDownloadReady(0x0D),

	/**
	 * 0x0E, 下載系統資料
	 */
	DownloadData(0x0E),

	/**
	 * 0x0F, 下載系統資料收到
	 */
	DownloadDataAck(0x0F),

	/**
	 * 0xA0, 開始OD資料請求
	 */
	StartODDataReq(0xA0),

	/**
	 * 0xA1, 開始OD資料請求收到
	 */
	StartODDataAck(0xA1),

	/**
	 * 0xA2, 停止OD資料請求
	 */
	StopODDataReq(0xA2),

	/**
	 * 0xA3, 停止OD資料請求收到
	 */
	StopODDataAck(0xA3),

	/**
	 * 0xA4, OD資料
	 */
	ODData(0xA4),

	/**
	 * 0xA5, OD資料收到
	 */
	ODDataAck(0xA5);

	private int intValue;
	private static java.util.HashMap<Integer, ETMMsgID> mappings;

	public static java.util.HashMap<Integer, ETMMsgID> getMappings() {
		if (mappings == null) {
			synchronized (ETMMsgID.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, ETMMsgID>();
				}
			}
		}
		return mappings;
	}

	private ETMMsgID(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static ETMMsgID forValue(int value) {
		return getMappings().get(value);
	}
}
