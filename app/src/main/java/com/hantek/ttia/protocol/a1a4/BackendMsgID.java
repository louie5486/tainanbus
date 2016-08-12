package com.hantek.ttia.protocol.a1a4;

public enum BackendMsgID {
	/**
	 * 0x00, 註冊請求訊息
	 */
	RegisterRequest(0x00),

	/**
	 * 0x01, 註冊回覆訊息
	 */
	RegisterResponse(0x01),

	/**
	 * 0x02, 修改路線請求訊息
	 */
	RoadModification(0x02),

	/**
	 * 0x03, 修改路線回覆訊息
	 */
	RoadModificationConfirm(0x03),

	/**
	 * 0x04, 定時回報訊息
	 */
	RegularReport(0x04),

	/**
	 * 0x05, 定時回報訊息確認
	 */
	RegularReportConfirm(0x05),

	/**
	 * 0x06, 提示訊息
	 */
	NotifyMessage(0x06),

	/**
	 * 0x07, 提示訊息確認
	 */
	NotifyMessageConfirm(0x07),

	/**
	 * 0x08, 事件回報訊息
	 */
	EventReport(0x08),

	/**
	 * 0x09, 事件回報訊息確認
	 */
	EventReportConfirm(0x09),

	/**
	 * 0x0A, 關機訊息
	 */
	Shutdown(0x0A),

	/**
	 * 0x0B, 關機回覆確認
	 */
	ShutdownConfirm(0x0B),

	/**
	 * 0xE0, 業者自行定義(保留)
	 */
	Reserved1(0xE0),

	/**
	 * 0xE1, 業者自行定義(保留)
	 */
	Reserved2(0xE1),

	/**
	 * 0xE2, 業者自行定義(保留)
	 */
	Reserved3(0xE2),

	/**
	 * 0xE3, 業者自行定義(保留)
	 */
	Reserved4(0xE3),

	/**
	 * 0xE4, 業者自行定義(保留)
	 */
	Reserved5(0xE4),

	/**
	 * 0xE5, 業者自行定義(保留)
	 */
	Reserved6(0xE5),

	/**
	 * 0xE6, 業者自行定義(保留)
	 */
	Reserved7(0xE6),

	/**
	 * 0xE7, 業者自行定義(保留)
	 */
	Reserved8(0xE7),

	/**
	 * 0xE8, 業者自行定義(保留)
	 */
	Reserved9(0xE8),

	/**
	 * 0xE9, 業者自行定義(保留)
	 */
	Reserved10(0xE9),

	/**
	 * 0xEA, 業者自行定義(保留)
	 */
	Reserved11(0xEA),

	/**
	 * 0xEB, 業者自行定義(保留)
	 */
	Reserved12(0xEB),

	/**
	 * 0xEC, 業者自行定義(保留)
	 */
	Reserved13(0xEC),

	/**
	 * 0xED, 業者自行定義(保留)
	 */
	Reserved14(0xED),

	/**
	 * 0xEE, 業者自行定義(保留)
	 */
	Reserved15(0xEE),

	/**
	 * 0xEF, 業者自行定義(保留)
	 */
	Reserved16(0xEF),

	/**
	 * 0xF0, 障礙回報訊息
	 */
	DeviceAlarm(0xF0),

	/**
	 * 0xF1, 障礙回報訊息確認
	 */
	DeviceAlarmConfirm(0xF1),

	/**
	 * 0xF2, OD回報訊息
	 */
	ReportOD(0xF2),

	/**
	 * 0xF3, OD回報確認訊息
	 */
	ReportODConfirm(0xF3);

	private int intValue;
	private static java.util.HashMap<Integer, BackendMsgID> mappings;

	public static java.util.HashMap<Integer, BackendMsgID> getMappings() {
		if (mappings == null) {
			synchronized (BackendMsgID.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, BackendMsgID>();
				}
			}
		}
		return mappings;
	}

	private BackendMsgID(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static BackendMsgID forValue(int value) {
		return getMappings().get(value);
	}
}
