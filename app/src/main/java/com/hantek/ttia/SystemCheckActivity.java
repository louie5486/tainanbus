package com.hantek.ttia;

import com.hantek.ttia.module.handshake.RequestTask;
import com.hantek.ttia.module.handshake.RoadRequest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SystemCheckActivity extends Activity {
    static final String TAG = SystemCheckActivity.class.getName();
    public final static int HDL_CONNECTED = 0;
    public final static int HDL_DISCONNECTED = 1;

    private final Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HDL_CONNECTED) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(getApplication(), MainActivity.class);
                        startActivity(intent);

                        finish();
                    }
                });
            } else if (msg.what == HDL_DISCONNECTED) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.system_check);

        RoadRequest request = new RoadRequest(getApplication(), mMainHandler);
        RequestTask task = new RequestTask(this, request);
        task.execute("路線檢查");
    }
}
