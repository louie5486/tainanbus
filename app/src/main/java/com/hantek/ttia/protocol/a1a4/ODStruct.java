package com.hantek.ttia.protocol.a1a4;

import java.util.ArrayList;

public class ODStruct {
	/**
	 * 起站代碼
	 */
	public byte orgStopID;

	/**
	 * 到站代碼
	 */
	public byte dstStopID;

	/**
	 * 起站上車時間
	 */
	public TimeStruct orgODTime;

	/**
	 * 到站下車時間
	 */
	public TimeStruct dstODTime;

	/**
	 * 剩餘乘客數
	 */
	public byte remainingNumber;

	/**
	 * 票務記錄筆數
	 */
	public byte recordNumber;

	/**
	 * 票卡清單
	 */
	public java.util.ArrayList<Card> cardList;

	public ODStruct() {
		this.orgStopID = 0;
		this.dstStopID = 0;
		this.orgODTime = new TimeStruct();
		this.dstODTime = new TimeStruct();
		this.remainingNumber = 0;
		this.recordNumber = 0;
		this.cardList = new ArrayList<Card>();
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[16 + 2 * this.cardList.size()];
		int index = 0;

		bytes[index++] = this.orgStopID;
		bytes[index++] = this.dstStopID;

		System.arraycopy(this.orgODTime.getBytes(), 0, bytes, index, TimeStruct.Length);
		index += TimeStruct.Length;

		System.arraycopy(this.dstODTime.getBytes(), 0, bytes, index, TimeStruct.Length);
		index += TimeStruct.Length;

		bytes[index++] = this.remainingNumber;
		bytes[index++] = this.recordNumber;

		for (int i = 0; i < this.cardList.size(); i++) {
			System.arraycopy(this.cardList.get(i).getBytes(), 0, bytes, index, Card.Length);
			index += Card.Length;
		}

		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<ODStruct>" + "\n");
		sb.append("orgStopID=" + this.orgStopID + "\n");
		sb.append("dstStopID=" + this.dstStopID + "\n");
		sb.append("orgODTime=\n" + this.orgODTime.toString());
		sb.append("dstODTime=\n" + this.dstODTime.toString());
		sb.append("remainingNumber=" + this.remainingNumber + "\n");
		sb.append("recordNumber=" + this.recordNumber + "\n");
		for (Card card : this.cardList) {
			sb.append(card.toString());
		}

		sb.append("</ODStruct>" + "\n");
		return sb.toString();
	}

	public ODStruct clone() {
		ODStruct varCopy = new ODStruct();

		varCopy.orgStopID = this.orgStopID;
		varCopy.dstStopID = this.dstStopID;
		varCopy.orgODTime = this.orgODTime;
		varCopy.dstODTime = this.dstODTime;
		varCopy.remainingNumber = this.remainingNumber;
		varCopy.recordNumber = this.recordNumber;
		varCopy.cardList = this.cardList;

		return varCopy;
	}
}
