package com.hantek.ttia.protocol.a1a4;


/**
 * 註冊請求訊息
 */
public class RegisterRequest {

	/**
	 * 定位訊息資料
	 */
	public MonitorStructType2 monitorData;

	/**
	 * 門號識別碼：15碼
	 */
	public String IMSI;

	/**
	 * 數據機識別碼：15碼
	 */
	public String IMEI;

	/**
	 * 車機製造商：0: 廠商代號1, 1: 廠商代號2, 2: 廠商代號3
	 */
	public byte manufacturer;

	/**
	 * 車機版本編號（各家車機廠商自訂）
	 */
	public byte[] OBUVersion;

	/**
	 * 註冊種類：0:冷開機註冊, 1:重發車註冊
	 */
	public byte regType;

	/**
	 * 司機ID 來源：0:身分識別裝置, 1:手動輸入, 2:未輸入
	 */
	public byte driverIDType;

	/**
	 * 程式檔案個數 Max:42
	 */
	public byte fileNumber;

	/**
	 * [Optional] 程式檔案清單
	 */
	public java.util.ArrayList<FileStruct> fileList;

	public RegisterRequest() {
		this.monitorData = new MonitorStructType2();
		this.IMSI = "";
		this.IMEI = "";
		this.manufacturer = 0;
		this.OBUVersion = new byte[8];
		this.regType = 0;
		this.driverIDType = 2;
		this.fileNumber = 0;
		this.fileList = new java.util.ArrayList<FileStruct>();
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[72 + 10 * this.fileList.size()];
		int index = 0;

		System.arraycopy(this.monitorData.getBytes(), 0, bytes, index, MonitorStructType2.Length);
		index += MonitorStructType2.Length;

		this.IMSI = String.format("%15s", this.IMSI);
		for (int i = 0; i < 15; i++) {
			try {
				bytes[index] = this.IMSI.getBytes()[i];
			} catch (Exception e) {
			}
			index++;
		}

		this.IMEI = String.format("%15s", this.IMEI);
		for (int i = 0; i < 15; i++) {
			try {
				bytes[index] = this.IMEI.getBytes()[i];
			} catch (Exception e) {
			}
			index++;
		}

		bytes[index++] = this.manufacturer;

		for (int i = 0; i < 8; i++) {
			try {
				bytes[index] = this.OBUVersion[i];
			} catch (Exception e) {
			}
			index++;
		}

		bytes[index++] = this.regType;
		bytes[index++] = this.driverIDType;
		bytes[index++] = this.fileNumber;

		if (this.fileList.size() > 0) {
			for (int i = 0; i < this.fileList.size(); i++) {
				FileStruct file = this.fileList.get(i);
				System.arraycopy(file.getBytes(), 0, bytes, index, FileStruct.Length);
				index += FileStruct.Length;
			}
		}

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<RegisterRequest>");
		sb.append(this.monitorData.toString());
		sb.append("IMSI=" + this.IMSI + "\n");
		sb.append("IMEI=" + this.IMEI + "\n");
		sb.append("manufacturer=" + this.manufacturer + "\n");
		sb.append("OBUVersion=");
		for (byte b : this.OBUVersion) {
			sb.append(b + ",");
		}
		sb.append("\n");
		sb.append("regType=" + this.regType + "\n");
		sb.append("driverIDType=" + this.driverIDType + "\n");
		sb.append("fileNumber=" + this.fileNumber + "\n");
		for (FileStruct file : this.fileList) {
			sb.append(file.toString());
		}
		sb.append("</RegisterRequest>");
		return sb.toString();
	}
}
