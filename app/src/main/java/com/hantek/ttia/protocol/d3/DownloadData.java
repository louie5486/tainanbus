package com.hantek.ttia.protocol.d3;

import com.hantek.ttia.module.BitConverter;

/**
 * 0x0E, 下載系統資料
 */
public class DownloadData extends Message {

	private int dataID;
	private int serialNumber;
	private int lastMessage;
	private byte[] dataBytes;
	private int dataBytesLength;

	public DownloadData(int dataID, int serialNumber, int lastMessage, byte[] dataBytes) {
		super(ETMMsgID.DownloadData, FrameType.Request);
		this.dataID = dataID;
		this.serialNumber = serialNumber;
		this.lastMessage = lastMessage;
		this.dataBytes = dataBytes;
		if (this.dataBytes != null) {
			this.dataBytesLength = this.dataBytes.length;
		}
	}

	@Override
	public byte[] getBytes() {
		byte[] payload = new byte[9 + this.dataBytesLength];
		int index = 0;

		System.arraycopy(BitConverter.toUShortByteArray(this.dataID), 0, payload, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		System.arraycopy(BitConverter.toUShortByteArray(this.serialNumber), 0, payload, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		payload[index++] = (byte) this.lastMessage;

		System.arraycopy(BitConverter.toUIntegerByteArray(this.dataBytesLength), 0, payload, index, BitConverter.UintSIZE);
		index += BitConverter.UintSIZE;

		if (dataBytesLength > 0) {
			System.arraycopy(this.dataBytes, 0, payload, index, dataBytesLength);
			index += dataBytesLength;
		}

		this.setPayload(payload);
		return super.getBytes();
	}

}
