package com.hantek.ttia.protocol.e2;

import java.util.Date;

import com.hantek.ttia.module.BitConverter;

/**
 * A8h
 */
public class ReportGps extends Message {
	private Date gpsTime;
	private double lon;
	private double lat;
	private float angle;
	private int speed;
	private boolean fixed;

	public ReportGps(Date gpsTime, double lon, double lat, float angle, int speed, boolean fixed) {
		super(DCRMsgID.ReportGPS);
		this.gpsTime = gpsTime;
		this.lon = lon;
		this.lat = lat;
		this.angle = angle;
		this.speed = speed;
		this.fixed = fixed;
	}

	@Override
	public byte[] getBytes() {
		byte[] dataBlock = new byte[16];
		int index = 0;

		long time = this.gpsTime.getTime() / 1000;
		System.arraycopy(BitConverter.toUIntegerByteArray(time), 0, dataBlock, index, BitConverter.UintSIZE);
		index += BitConverter.UintSIZE;

		int lon = (int) (this.lon * 600000);
		System.arraycopy(BitConverter.toIntegerByteArray(lon), 0, dataBlock, index, BitConverter.IntSIZE);
		index += BitConverter.IntSIZE;

		int lat = (int) (this.lat * 600000);
		System.arraycopy(BitConverter.toIntegerByteArray(lat), 0, dataBlock, index, BitConverter.IntSIZE);
		index += BitConverter.IntSIZE;

		int angle = (int) (this.angle / 10);
		System.arraycopy(BitConverter.toUShortByteArray(angle), 0, dataBlock, index, BitConverter.UshortSIZE);
		index += BitConverter.UshortSIZE;

		dataBlock[index++] = (byte) this.speed;
		dataBlock[index++] = (byte) (this.fixed ? 0 : 1);

		super.setDataBlock(dataBlock);
		return super.getBytes();
	}

	@Override
	public String toString() {
		return "ReportGps [gpsTime=" + gpsTime + ", lon=" + lon + ", lat=" + lat + ", angle=" + angle + ", speed=" + speed + ", fixed=" + fixed + "]";
	}
}
