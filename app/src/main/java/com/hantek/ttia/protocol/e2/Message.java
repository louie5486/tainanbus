package com.hantek.ttia.protocol.e2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.hantek.ttia.module.BitConverter;

public abstract class Message {
	static final int DLE = 0x10;
	static final int STX = 0x02;
	static final int ETX = 0x03;

	private DCRMsgID messageID;
	private int BCC;
	private byte[] dataBlock;
	private byte state;

	public Message(DCRMsgID msgID) {
		this.messageID = msgID;
	}

	protected void setDataBlock(byte[] bytes) {
		this.dataBlock = bytes;
	}

	public byte getState() {
		return this.state;
	}

	public int getMessageID() {
		return this.messageID.getValue();
	}

	public byte[] getBytes() {		
		int dataBlockLength = 0;
		if (this.dataBlock != null) {
			dataBlockLength = this.dataBlock.length;
		}
		
		byte[] bytes = new byte[7 + dataBlockLength];
		int index = 0;
		bytes[index++] = DLE;
		bytes[index++] = STX;
		bytes[index++] = (byte) this.messageID.getValue();
		this.checkBBS(bytes[index - 1] & 0xff);

		byte[] tmpLenBytes = BitConverter.toUShortByteArray(dataBlockLength);
		for (int i = 0; i < tmpLenBytes.length; i++) {
			this.checkBBS(tmpLenBytes[i] & 0xff);
			bytes[index++] = tmpLenBytes[i];
		}

		if (dataBlockLength > 0) {
			for (int i = 0; i < dataBlockLength; i++) {
				this.checkBBS(dataBlock[i] & 0xff);
				bytes[index++] = dataBlock[i];
			}
		}

		bytes[index++] = ETX;
		this.checkBBS(ETX & 0xff);

		bytes[index++] = (byte) this.BCC; // 從msgID到ETX

		return bytes;
	}

	private void checkBBS(int b) {
		this.BCC ^= b;
	}

	protected String toDateTime(long time) {
		Date tmp = new Date(time * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // 指定顯示UTC時間
		String formattedDate = sdf.format(tmp.getTime());
		return formattedDate;
	}
}
