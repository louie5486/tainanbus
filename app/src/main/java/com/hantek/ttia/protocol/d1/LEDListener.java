package com.hantek.ttia.protocol.d1;

public interface LEDListener {
    void onLCDDataReceived(byte[] bytes);
}