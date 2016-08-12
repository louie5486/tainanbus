package com.hantek.ttia.protocol.a1a4;


/**
 * 定時回報訊息
 */
public class RegularReport {
	/**
	 * 回傳訊息筆數，一個封包最大以512 bytes 計算，扣除Header 與Payload 之其他欄位，MonitorData 個數最多為4 個。
	 */
	public byte monitorData;

	/**
	 * 保留
	 */
	public byte reserved;

	/**
	 * 定位訊息資料清單
	 */
	public java.util.ArrayList<MonitorStructType1> monitorDataList;

	public RegularReport() {
		this.monitorDataList = new java.util.ArrayList<MonitorStructType1>();
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[this.monitorData * MonitorStructType1.Length + 2];
		int index = 0;

		bytes[index++] = this.monitorData;
		bytes[index++] = this.reserved;

		if (this.monitorDataList.size() > 0) {
			for (int i = 0; i < this.monitorDataList.size(); i++) {
				MonitorStructType1 data = this.monitorDataList.get(i);
				System.arraycopy(data.getBytes(), 0, bytes, index, MonitorStructType1.Length);
				index += MonitorStructType1.Length;
			}
		}

		return bytes;
	}

	public RegularReport clone() {
		RegularReport varCopy = new RegularReport();

		varCopy.monitorData = this.monitorData;
		varCopy.reserved = this.reserved;
		varCopy.monitorDataList = this.monitorDataList;

		return varCopy;
	}
}
