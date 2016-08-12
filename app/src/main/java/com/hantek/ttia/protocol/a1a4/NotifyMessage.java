package com.hantek.ttia.protocol.a1a4;

import java.io.UnsupportedEncodingException;

import com.hantek.ttia.module.BitConverter;

public class NotifyMessage {

	/**
	 * 是否需要司機回覆(0:不需要, 1:需要(確認), 2:需要(接受/拒絕)
	 */
	public int action;

	/**
	 * 提示訊息代碼，(需要司機回覆知訊息方須填入)
	 */
	public int infoID;

	/**
	 * 保留
	 */
	public int reserved;

	/**
	 * 提示訊息，長度為Header中之「Len-4」，最長180bytes
	 */
	public String information;

	public NotifyMessage() {

	}

	public static NotifyMessage Parse(int[] intArray) {
		int index = 0;
		NotifyMessage message = new NotifyMessage();
		message.action = intArray[index++];

		message.infoID = BitConverter.toUShort(intArray, index);
		index += BitConverter.UshortSIZE;

		message.reserved = intArray[index++];

		try {
			message.information = BitConverter.toString(intArray, index, intArray.length - 4, "Big-5");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return message;
	}

	@Override
	public String toString() {
		return "NotifyMessage [action=" + action + ", infoID=" + infoID + ", reserved=" + reserved + ", information=" + information + "]";
	}
		
}
