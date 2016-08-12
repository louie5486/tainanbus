package com.hantek.ttia.protocol.hantek;

import com.hantek.ttia.protocol.a1a4.Message;

public interface HantekListener {
	void onReceivedHantek(Message message);
}
