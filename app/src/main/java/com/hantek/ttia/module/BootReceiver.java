package com.hantek.ttia.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hantek.ttia.LoginActivity;

public class BootReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		System.out.println("BootReceiver Boot Complete success!!");
		Intent i = new Intent(context, LoginActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
}
