package com.hantek.ttia.protocol.d3;

import java.util.Arrays;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x0A, 上傳系統資料
 */
public class UploadData extends Message {

	/**
	 * 上傳資料編號
	 */
	private int dataID;

	/**
	 * 訊息序號(由0 開始,每次加1)
	 */
	private int serialNumber;

	/**
	 * 0:成功 1~255:失敗之錯誤碼
	 */
	private int result;

	/**
	 * 0:後面尚有資料 1:最後一筆資料
	 */
	private int lastMessage;

	/**
	 * 資料長度
	 */
	private int dataLength;

	/**
	 * 資料
	 */
	private int[] dataArray;

	public int getDataID() {
		return dataID;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public int getResult() {
		return result;
	}

	public int getLastMessage() {
		return lastMessage;
	}

	public int getDataLength() {
		return dataLength;
	}

	public int[] getDataArray() {
		return dataArray;
	}

	public UploadData() {
		super(ETMMsgID.UploadData, FrameType.Request);
	}

	public static UploadData parse(int[] intArray) {
		int index = 9;
		UploadData data = new UploadData();

		data.dataID = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		data.serialNumber = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		data.result = intArray[index++];

		data.lastMessage = intArray[index++];

		data.dataLength = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		data.dataArray = new int[data.dataLength];
		System.arraycopy(intArray, index, data.dataArray, 0, data.dataArray.length);
		index += data.dataArray.length;

		return data;
	}

	@Override
	public String toString() {
		return "UploadData [dataID=" + dataID + ", serialNumber=" + serialNumber + ", result=" + result + ", lastMessage=" + lastMessage + ", dataLength=" + dataLength + ", dataArray="
				+ Arrays.toString(dataArray) + "]";
	}

//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("<UploadData>" + "\n");
//		sb.append("dataID=" + dataID + "\n");
//		sb.append("serialNumber=" + serialNumber + "\n");
//		sb.append("result=" + result + "\n");
//		sb.append("lastMessage=" + lastMessage + "\n");
//		sb.append("dataLength=" + dataLength + "\n");
//		sb.append("data="dataArray + "\n");
//		sb.append("</UploadData>" + "\n");
//		return sb.toString();
//	}
	
	
}
