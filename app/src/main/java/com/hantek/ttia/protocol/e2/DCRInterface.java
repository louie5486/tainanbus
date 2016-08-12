package com.hantek.ttia.protocol.e2;

public interface DCRInterface {
	/**
	 * 紀錄器基本資訊
	 * 
	 * @return
	 */
	boolean basicDataReq();

	/**
	 * 行駛資料
	 * 
	 * @return
	 */
	boolean drivingDataReq();

	/**
	 * 瞬時速度資料
	 * 
	 * @return
	 */
	boolean instantSpeedReq();

	/**
	 * 停車前5分鐘內每秒種最高速度
	 * 
	 * @return
	 */
	boolean highSpeedReq();

	/**
	 * 疲勞駕駛記錄
	 * 
	 * @return
	 */
	boolean tiredDrivingReq();

	/**
	 * 讀取即時速度
	 * 
	 * @param seconds
	 * @return
	 * @throws Exception 
	 */
	boolean readIntSpeedReq(int seconds) throws Exception;

	/**
	 * 駕駛員休息紀錄
	 * 
	 * @return
	 */
	boolean driverRestReq();

	/**
	 * GPS回報
	 * 
	 * @return
	 */
	boolean reportGPS(ReportGps data); // Date gpsTime, double lon, double lat, float angle, int speed, boolean fixed);

}
