package com.hantek.ttia.protocol.d1;

public interface LEDInterface {

	boolean sendMessage(String cmd, Animation animation, char c1, char c2, char c3, String data);

	boolean pause();
	
	boolean resume();
	
	boolean forceStopPlay();
}