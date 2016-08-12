package com.hantek.ttia.comms;

import java.util.Calendar;

public interface PhysicalInterface {
	boolean open(String options);

	boolean close();

	boolean isOpen();

	Calendar getLastReceiveTime();

	boolean send(byte[] dataByte);

	void setListener(DataListener listener);
}
