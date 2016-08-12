package com.hantek.ttia.protocol.d3;

import java.util.ArrayList;

public class ODStruct {
	/**
	 * 16 + card count * N
	 */
	public static final short Length = 16;

	/**
	 * 起站代碼
	 */
	public int orgStopID;

	/**
	 * 到站代碼
	 */
	public int dstStopID;

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
	public int remainingNumber;

	/**
	 * 票務記錄筆數
	 */
	public int recordNumber;

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

	// public byte[] getBytes() {
	// byte[] bytes = new byte[16 + 2 * this.cardList.size()];
	// int index = 0;
	//
	// bytes[index++] = (byte) this.orgStopID;
	// bytes[index++] = (byte) this.dstStopID;
	//
	// System.arraycopy(this.orgODTime.getBytes(), 0, bytes, index, TimeStruct.Length);
	// index += TimeStruct.Length;
	//
	// System.arraycopy(this.dstODTime.getBytes(), 0, bytes, index, TimeStruct.Length);
	// index += TimeStruct.Length;
	//
	// bytes[index++] = this.remainingNumber;
	// bytes[index++] = this.recordNumber;
	//
	// for (int i = 0; i < this.cardList.size(); i++) {
	// System.arraycopy(this.cardList.get(i).getBytes(), 0, bytes, index, Card.Length);
	// index += Card.Length;
	// }
	//
	// return bytes;
	// }

	public static ODStruct parse(int[] intArray, int index) {
		ODStruct od = new ODStruct();
		od.orgStopID = intArray[index++];
		od.dstStopID = intArray[index++];

		od.orgODTime = TimeStruct.parse(intArray, index);
		index += TimeStruct.Length;

		od.dstODTime = TimeStruct.parse(intArray, index);
		index += TimeStruct.Length;

		od.remainingNumber = intArray[index++];
		od.recordNumber = intArray[index++];

		int count = od.recordNumber;
		if (count > 0) {
			// do {
			// Card card = Card.parse(intArray, index);
			// od.cardList.add(card);
			// index += Card.Length;
			// } while (index < intArray.length);
			while (count > 0) {
				Card card = Card.parse(intArray, index);
				od.cardList.add(card);
				index += Card.Length;
				count -= 1;
			}
		}

		return od;
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<ODStruct>" + "\n");
	// sb.append("orgStopID=" + this.orgStopID + "\n");
	// sb.append("dstStopID=" + this.dstStopID + "\n");
	// sb.append("orgODTime=\n" + this.orgODTime.toString());
	// sb.append("dstODTime=\n" + this.dstODTime.toString());
	// sb.append("remainingNumber=" + this.remainingNumber + "\n");
	// sb.append("recordNumber=" + this.recordNumber + "\n");
	// for (Card card : this.cardList) {
	// sb.append(card.toString());
	// }
	//
	// sb.append("</ODStruct>" + "\n");
	// return sb.toString();
	// }

	@Override
	public String toString() {
		String s = " [orgStopID=" + orgStopID + ", dstStopID=" + dstStopID + ", orgODTime=" + orgODTime + ", dstODTime=" + dstODTime + ", remainingNumber=" + remainingNumber + ", recordNumber="
				+ recordNumber + "]\n";

		if (this.recordNumber > 0) {
			for (int i = 0; i < this.cardList.size(); i++) {
				Card card = this.cardList.get(i);
				s += String.format("Card(%s) %s", i + 1, card.toString());
			}
		}

		return s;
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
