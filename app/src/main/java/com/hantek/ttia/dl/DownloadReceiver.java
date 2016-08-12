package com.hantek.ttia.dl;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class DownloadReceiver extends ResultReceiver {
    ProgressDialog mProgressDialog;

    public DownloadReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        if (resultCode == DownloadService.UPDATE_PROGRESS) {
            int progress = resultData.getInt("progress");
            if (mProgressDialog != null) {
                mProgressDialog.setProgress(progress);
            }
        } else if (resultCode == DownloadService.CLOSE_PROGRESS) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        } else if (resultCode == DownloadService.NOTIFY_PROGRESS) {
            String message = resultData.getString("notify");
            if (mProgressDialog != null) {
                mProgressDialog.setMessage(message);
            }
        }
    }
}