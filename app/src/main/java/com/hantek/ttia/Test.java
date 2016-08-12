package com.hantek.ttia;

import java.util.LinkedList;
import java.util.List;

import com.hantek.ttia.protocol.a1a4.Card;
import com.hantek.ttia.protocol.a1a4.ODStruct;
import com.hantek.ttia.protocol.a1a4.TimeStruct;
import com.hantek.ttia.protocol.d1.Animation;
import com.hantek.ttia.protocol.d3.Encryption;

public class Test {

	// A1A4 註冊
	public static int regType = 0;
	public static byte driverIDType = 0;

	// A1A4路線修改
	public static String branch = "0";
	public static int direct = 0;
	public static int id = 65535;

	// A1A4 關機
	public static int PSDReconnect;
	public static int PacketRatio;
	public static int GPSRatio;

	// A1A4 障礙回報
	public static int module;
	public static int code;

	// D1 LED顯示文字
	public static String LEDText = "";
	public static Animation LEDanimation = Animation.Now;
	public static char cc1 = '1';
	public static char cc2 = '0';
	public static char cc3 = '0';

	// D3 authen request
	public static int oat;
	public static int ot;
	public static long oID;
	public static String pwd = "";

	// D3 upload data request
	public static int UDRid;
	public static Encryption UDRencryption = Encryption.Ciphertext;

	// D3 start od data request
	public static int reportFlag = 0;

	// D3 download data request data id
	public static int downloadDataReq;

	// D3 download data
	public static int dlDID;
	public static int dlDSN;
	public static int dlDLast;
	public static int dlDTotal;

	public static int Result = 0;

	// E2 A6
	public static int seconds;
	
	public static byte[] getdlD() {
		if (dlDID == 0x0101) {
			return new byte[] { 0x00 };
		} else if (dlDID == 0x0102) {
			return new byte[] { 0x00 };
		} else if (dlDID == 0x0103) {
			return new byte[] { 0x00 };
		} else if (dlDID == 0x0104) {
			return new byte[] { 0x00 };
		} else {
			return new byte[0];
		}
	}

	public static byte[] getdlD_2() {
		if (dlDID == 0x0101) {
			return new byte[] { 0x01 };
		} else if (dlDID == 0x0102) {
			return new byte[] { 0x01 };
		} else if (dlDID == 0x0103) {
			return new byte[] { 0x01 };
		} else if (dlDID == 0x0104) {
			return new byte[] { 0x01 };
		} else {
			return new byte[0];
		}
	}

	public static byte[] getdlD0102() {
		return new byte[0];
	}

	public static byte[] getdlD0103() {
		return new byte[0];
	}

	public static byte[] getdlD0104() {
		return new byte[0];
	}

	public static List<ODStruct> getReportList(int count) {
		List<ODStruct> list = new LinkedList<ODStruct>();
		for (int i = 0; i < count; i++) {
			ODStruct od = new ODStruct();
			od.orgStopID = 1;
			od.dstStopID = (byte) 255;
			od.orgODTime = new TimeStruct();
			od.dstODTime = new TimeStruct();
			od.remainingNumber = 100;

			Card card100 = new Card();
			card100.id = 100;
			card100.number = 101;

			Card card200 = new Card();
			card200.id = (byte) 200;
			card200.number = (byte) 201;

			od.cardList.add(card100);
			od.cardList.add(card200);

			od.recordNumber = (byte) od.cardList.size();
			list.add(od);
		}

		return list;
	}
}
