package com.hantek.ttia.protocol.a1a4;

public interface BackendInterface {

	/**
	 * 註冊 0x00
	 */
	byte[] sendRegisterRequest(Header header, RegisterRequest request);

	/**
	 * 路線修改 0x02
	 */
	byte[] sendRoadModification(Header header, RoadModification roadModification);

	/**
	 * 定時回報 0x04
	 */
	byte[] sendRegularReport(Header header, RegularReport report);

	/**
	 * 提示訊息確認 0x07
	 */
	byte[] sendNotifyMessageConfirm(Header header);

	/**
	 * 事件回報訊息 0x08
	 */
	byte[] sendEventReport(Header header, EventReportBase eventReport, Object eventContent);

	/**
	 * 關機回報 0x0A
	 */
	byte[] sendShutdown(Header header, Shutdown shutdown);

	/**
	 * 障礙回報 0xF0
	 */
	byte[] sendDeviceAlarm(Header header, DeviceAlarm deviceAlarm);

	/**
	 * OD回報訊息 0xF2
	 */
	byte[] sendODReport(Header header, ODReport odReport);

}