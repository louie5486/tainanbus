package com.hantek.ttia.protocol.a1a4;

public enum EventCode {
	/**
	 * 0x0001, 進出站(圓形偵測) 進出站回報
	 */
	InOutStation(0x0001),

	/**
	 * 0x0002, 超轉超速 瞬時轉速超過特定值 瞬時時速超過特定值
	 */
	OverSpeedAndRPM(0x0002),

	/**
	 * 0x0004, 急加/減速 單位時間加速/減速超過限制速度範圍
	 */
	Acceleration(0x0004),

	/**
	 * 0x0008, 行駛中前門/後門 開啟 前門信號為1 且速度>0 後門信號為1 且速度>0
	 */
	DoorOpen(0x0008),

	/**
	 * 0x0010, 車輛異常回報 停車不熄火（轉速不為0 且速度為0 的時間超過特定範圍）異常移動（轉速為0 速度大於0）
	 */
	CarAlarm(0x0010),

	/**
	 * 0x0020, 車輛狀態 司機由螢幕改變車輛狀態（包含緊急事件）
	 */
	UpdateCarStatus(0x0020),

	/**
	 * 0x0040, 異常發車 後端回覆「無班表」，且司機沒有選擇特殊狀態（狀態停留在"未知"），以及車輛已移動特定距離
	 */
	NoScheduleOnMove(0x0040),

	/**
	 * 0x0080, 司機回覆 司機回覆提示訊息
	 */
	DriverReport(0x0080),

	/**
	 * 0x0100, 進出特定區域 進出特定區域回報，用於禁止遊覽車行駛之特定區域偵測（此一特定區域相關資訊須由中控中心事前提供車機載入判斷）
	 */
	InOutSpecialArea(0x0100),

	/**
	 * 0x0200, 待增
	 */
	EventCode200(0x0200),

	/**
	 * 0x0400, 待增
	 */
	EventCode400(0x0400),

	/**
	 * 0x0800, 待增
	 */
	EventCode800(0x0800),

	/**
	 * 0x1000, 待增
	 */
	EventCode1000(0x1000),

	/**
	 * 0x2000, 待增
	 */
	EventCode2000(0x2000),

	/**
	 * 0x4000, 待增 勤務狀態回報(大台南公車)
	 */
	EventCode4000(0x4000),

	/**
	 * 0x8000, 路線外營運 車輛進入非核定許可之經營路線（此一路線外營運相關資訊須由中控中心事前提供車機載入判斷）
	 */
	NotOnSchedule(0x8000);

	private int intValue;
	private static java.util.HashMap<Integer, EventCode> mappings;

	private static java.util.HashMap<Integer, EventCode> getMappings() {
		if (mappings == null) {
			synchronized (EventCode.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, EventCode>();
				}
			}
		}
		return mappings;
	}

	private EventCode(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static EventCode forValue(int value) {
		return getMappings().get(value);
	}
}
