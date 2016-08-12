package com.hantek.ttia.comms;

import java.util.Calendar;

public interface DataListener {
	
	/**
	 * path,baudrate ie:/dev/ttyUSB1,57600
	 * @param options
	 * @return
	 */
	boolean open(String options);

	boolean close();

	boolean isOpen();

	Calendar getLastReceiveTime();

	void onDataReceived(byte[] rawData, int size);
}
