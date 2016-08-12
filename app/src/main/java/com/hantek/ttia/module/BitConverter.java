package com.hantek.ttia.module;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitConverter {
	public final static int UintSIZE = 4;
	public final static int UshortSIZE = 2;
	public final static int IntSIZE = 4;
	public final static int ShortSIZE = 2;

	public static byte[] toUIntegerByteArray(long value) throws NumberFormatException {
		if (value >= Math.pow(2, 32) || value < 0)
			throw new NumberFormatException("uint format error:" + value);

		byte[] bytes = new byte[4];
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) (value >> 8 & 0xff);
		bytes[2] = (byte) (value >> 16 & 0xff);
		bytes[3] = (byte) (value >> 24 & 0xff);
		return bytes;
	}

	public static byte[] toUShortByteArray(int value) throws NumberFormatException {
		if (value >= Math.pow(2, 16) || value < 0)
			throw new NumberFormatException("ushort format error:" + value);

		byte[] bytes = new byte[2];
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) (value >> 8 & 0xff);
		return bytes;
	}

	public static byte[] toIntegerByteArray(int value) throws NumberFormatException {
		return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
	}

	public static byte[] toShortByteArray(short value) {
		return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
	}

	public static long toUInteger(int[] intArray, int index) {
		return intArray[index + 3] << 24 | ((intArray[index + 2] & 0xff) << 16) | ((intArray[index + 1] & 0xff) << 8) | (intArray[index] & 0xff);
	}

	public static int toUShort(int[] intArray, int index) {
		return (intArray[index + 1] & 0xff) << 8 | (intArray[index] & 0xff);
	}

	public static int byteArrayToInt(int[] intArray, int index) {
		return intArray[index + 3] << 24 | (intArray[index + 2] & 0xff) << 16 | (intArray[index + 1] & 0xff) << 8 | (intArray[index] & 0xff);
	}

	/**
	 * BitConverter.toString(intArray, index, 2, "Big-5");
	 */
	public static String toString(int[] intArray, int index, int length, String encoding) throws UnsupportedEncodingException {
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = (byte) intArray[index + i];
		}

		return new String(bytes, encoding);
	}
}
