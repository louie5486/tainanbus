package com.hantek.ttia.protocol.a1a4;

public final class Card {
	public static final short Length = 2;

	/**
	 * 票卡種類
	 */
	public byte id;

	/**
	 * 票卡種類之統計人數
	 */
	public byte number;

	public byte[] getBytes() {
		byte[] bytes = new byte[Length];
		int index = 0;

		bytes[index++] = this.id;
		
		bytes[index++] = this.number;
		
		return bytes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<Card>" + "\n");
		sb.append("id=" + this.id + " ");
		sb.append("number=" + this.number + "\n");
		sb.append("</Card>" + "\n");
		return sb.toString();
	}

	public Card clone() {
		Card varCopy = new Card();

		varCopy.id = this.id;
		varCopy.number = this.number;

		return varCopy;
	}
}
