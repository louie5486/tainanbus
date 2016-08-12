package com.hantek.ttia.protocol.d3;

public class Card {
	public static final short Length = 2;

	/**
	 * 票卡種類
	 */
	public int id;

	/**
	 * 票卡種類之統計人數
	 */
	public int number;

	// public byte[] getBytes() {
	// byte[] bytes = new byte[Length];
	// int index = 0;
	//
	// bytes[index++] = this.id;
	//
	// bytes[index++] = this.number;
	//
	// return bytes;
	// }

	public static Card parse(int[] intArray, int index) {
		Card card = new Card();
		card.id = intArray[index++];
		card.number = intArray[index++];
		return card;
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// sb.append("<Card>" + "\n");
	// sb.append("id=" + this.id + " ");
	// sb.append("number=" + this.number + "\n");
	// sb.append("</Card>" + "\n");
	// return sb.toString();
	// }

	@Override
	public String toString() {
		return " [id=" + id + ", number=" + number + "]\n";
	}

	public Card clone() {
		Card varCopy = new Card();

		varCopy.id = this.id;
		varCopy.number = this.number;

		return varCopy;
	}
}
