package com.hantek.ttia.protocol.d3;

import java.util.ArrayList;

import com.hantek.ttia.module.BitConverter;

public abstract class Message {
	private static final int StartCode = 0x02;
	private static final int EndCode = 0x03;
	private static final int DLE = 0x10;

	private FrameType frameType;
	private ETMMsgID messageID;
	private int sequenceNo;
	private int lastMessage;
	private int idStorage;
	private int reserved;
	private int payloadLength;
	private Byte[] payLoad;

	private int checksum;

	public Message(ETMMsgID msgID, FrameType frmType) {
		this.messageID = msgID;
		this.frameType = frmType;
		this.lastMessage = 0;
		this.idStorage = 0;
		this.reserved = 0;
	}

	public void setSeqNo(int seqNo) {
		this.sequenceNo = seqNo;
	}

	public void setLastMessage(int lastMsg) {
		this.lastMessage = lastMsg;
	}

	public void setIDStorage(int idStorage) {
		this.idStorage = idStorage;
	}

	public void setPayload(byte[] bytes) {
		this.calcChecksum(bytes.length); // 原始長度
		this.payLoad = this.checkPayload(bytes);
		this.payloadLength = this.payLoad.length;
	}

	public int getMessageID() {
		return this.messageID.getValue();
	}

	public FrameType getFrameType() {
		return this.frameType;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[11 + this.payloadLength];
		int index = 0;
		// ----- Header -----
		bytes[index++] = StartCode;
		this.calcChecksum(StartCode);

		bytes[index++] = (byte) this.messageID.getValue();
		this.calcChecksum(bytes[index - 1]);

		System.arraycopy(BitConverter.toUShortByteArray(this.sequenceNo), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;
		this.calcChecksum(bytes[index - 2]);
		this.calcChecksum(bytes[index - 1]);

		bytes[index++] = (byte) this.lastMessage;
		this.calcChecksum(bytes[index - 1]);
		bytes[index++] = (byte) this.idStorage;
		this.calcChecksum(bytes[index - 1]);
		bytes[index++] = (byte) this.reserved;
		this.calcChecksum(bytes[index - 1]);

		System.arraycopy(BitConverter.toUShortByteArray(this.payloadLength), 0, bytes, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;
		// ----- Header -----

		for (int i = 0; i < this.payloadLength; i++) {
			bytes[index++] = payLoad[i];
		}
		bytes[index++] = (byte) this.checksum;
		bytes[index++] = EndCode;
		return bytes;
	}

	// 只計算原始資料
	private void calcChecksum(int b) {
		this.checksum ^= b;
	}

	// payload含有控制字元處理
	private Byte[] checkPayload(byte[] bytes) {
		ArrayList<Byte> tmpArrayList = new ArrayList<Byte>();
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] & 0xff) == StartCode || (bytes[i] & 0xff) == EndCode || (bytes[i] & 0xff) == DLE) {
				tmpArrayList.add((byte) DLE);
			}
			tmpArrayList.add(bytes[i]);
			this.calcChecksum(bytes[i]);
		}

		Byte[] tmp = new Byte[tmpArrayList.size()];
		tmpArrayList.toArray(tmp);
		return tmp;
	}
}
