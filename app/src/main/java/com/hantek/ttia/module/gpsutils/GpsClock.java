package com.hantek.ttia.module.gpsutils;

import java.util.Calendar;
import java.util.Date;

public class GpsClock {
	private static GpsClock instance;

	public static GpsClock getInstance() {
		if (instance == null) {
			instance = new GpsClock();
		}

		return instance;
	}

	public Date getTime() {
		return Calendar.getInstance().getTime();
	}
}
