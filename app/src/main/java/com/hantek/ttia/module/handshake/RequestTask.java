package com.hantek.ttia.module.handshake;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;

public class RequestTask extends AsyncTask<String, String, String> {
    static final String OK = "OK";
    static final String PING = "PING";
    static final String FAIL = "FAIL";
    static final String ERROR = "ERROR";

    static final int CHECK_CONNECTION_MS = 10 * 1000;
    private Context mContext;
    private ProgressDialog mProgressDlg;
    private IRequest mRequest;

    public RequestTask(Context context, IRequest request) {
        mContext = context;
        mRequest = request;
        showProgressDlg("", mContext);
    }

    @Override
    protected String doInBackground(String... params) {
        // 提示文字
        String enabling = params[0];

        publishProgress(enabling);
        long time = System.currentTimeMillis();

        // 先確認是否連線
        while (!ping()) {
            if ((System.currentTimeMillis() - time) < CHECK_CONNECTION_MS)
                publishProgress(enabling);
            else {
                // 如果逾時的話..
                return PING;
            }
        }

        try {
            // 發送訊息
            publishProgress(enabling);
            if (mRequest.request()) {
                publishProgress(enabling + "\r\n等候確認..");
                Thread.sleep(1000);
                if (mRequest.waitResp()) {
                    return OK;
                }
                return FAIL;
            } else {
                // 如果發送失敗的話..
            	return FAIL;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR;
        }        
    }

    @Override
    protected void onPostExecute(String result) {
        closeProgressDlg();
        if (result.equals(OK)) {
            if (mRequest.getResponse() != null)
                mRequest.execute(mRequest.getResponse());
            else
                mRequest.rollBack();
            // Toast.makeText(mContext, "You got message.\r\n" + mRequest.getResponse(), Toast.LENGTH_SHORT).show();
        } else if (result.equals(PING)) {
            mRequest.rollBack();
        } else {
            mRequest.rollBack();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (mProgressDlg != null)
            mProgressDlg.setMessage(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        closeProgressDlg();
    }

    private boolean ping() {
        try {
            Thread.sleep(10);
            if (mRequest == null)
                return false;

            return mRequest.checkConnection();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showProgressDlg(String msg, Context context) {
        closeProgressDlg();
        mProgressDlg = new ProgressDialog(context);
        mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDlg.setTitle("");

        mProgressDlg.setMessage(msg);
        mProgressDlg.setCancelable(false);
        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDlg.setIndeterminate(true);
        mProgressDlg.show();
    }

    private void closeProgressDlg() {
        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            try {
                mProgressDlg.dismiss();
            }catch (Exception e){

            }
            mProgressDlg = null;
        }
    }
}