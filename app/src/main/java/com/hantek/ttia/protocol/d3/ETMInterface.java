package com.hantek.ttia.protocol.d3;

public interface ETMInterface {
	/**
	 * 身分認證要求
	 * 
	 * @param operatorAuthenType
	 * @param operatorType
	 * @param operatorID
	 * @param password
	 * @return
	 */
	boolean authenRequest(int operatorAuthenType, int operatorType, long operatorID, String password);

	/**
	 * 開始請求GPS資訊收到
	 * 
	 * @param result
	 * @return
	 */
	boolean startRequestGpsInfoAck(int result);

	/**
	 * 送出GPS資訊
	 * 
	 * @param gpsInfo
	 * @return
	 */
	boolean sendGpsInfo(GpsInfo gpsInfo);

	/**
	 * 終止請求GPS資訊收到
	 * 
	 * @param result
	 * @return
	 */
	boolean stopRequestGpsInfoAck(int result);

	/**
	 * 系統資料上傳要求
	 * 
	 * @param dataID
	 * @param encryptionFlag
	 * @return
	 */
	boolean uploadDataReq(int dataID, Encryption encryptionFlag);

	/**
	 * 上傳系統資料收到
	 * 
	 * @param result
	 * @param serialNumber
	 * @return
	 */
	boolean uploadDataAck(int result, int serialNumber);

	/**
	 * 系統資料下載要求
	 * 
	 * @param dataID
	 * @param encryptionFlag
	 * @return
	 */
	boolean downloadDataReq(int dataID, Encryption encryptionFlag);

	/**
	 * 下載系統資料
	 * 
	 * @param dataID
	 * @param serialNumber
	 * @param lastMessage
	 * @param dataByte
	 * @return
	 */
	boolean downloadData(int dataID, int serialNumber, int lastMessage, byte[] dataByte);

	/**
	 * 開始OD資料請求
	 * 
	 * @param encryptionFlag
	 * @param reportFlag
	 * @return
	 */
	boolean startODDataReq(Encryption encryptionFlag, int reportFlag);

	/**
	 * 停止OD資料請求
	 * 
	 * @return
	 */
	boolean stopODDataReq();

	/**
	 * OD資料收到
	 * 
	 * @param result
	 * @return
	 */
	boolean ODDataAck(int result);
}
