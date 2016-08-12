package com.hantek.ttia;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import component.LogManager;

public class NotifyMessageDialog extends Dialog implements android.view.View.OnClickListener {
    private static final String TAG = NotifyMessageDialog.class.getName();

    private Context mContext;
    private int type = 0;
    private int infoID = 0;
    private String info = "";

    private Button okButton, cancelButton;
    private TextView textView;

    public NotifyMessageDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public void setParameter(int action, int infoID, String info) {
        this.type = action;
        this.infoID = infoID;
        this.info = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        Log.d(TAG, "Dialog onCreate");
        this.setTitle("訊息通知 [" + this.infoID + "]");

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);

        textView = (TextView) findViewById(R.id.messageTextView);
        okButton = (Button) findViewById(R.id.okButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        textView.setText(info);
        okButton.setOnClickListener(this);

        switch (this.type) {
            case 1:
                // 確認
                cancelButton.setVisibility(View.INVISIBLE);
                break;
            case 2:
                cancelButton.setOnClickListener(this);
                // 接收/拒絕
                break;
            default:
                cancelButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.okButton:
                    switch (this.type) {
                        case 1:
                            // 確認
                            ((MainActivity) this.mContext).notifyMessage(this.infoID, 0);
                            break;
                        case 2:
                            // 接收
                            ((MainActivity) this.mContext).notifyMessage(this.infoID, 1);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.cancelButton:
                    // 拒絕
                    ((MainActivity) this.mContext).notifyMessage(this.infoID, 2);
                    break;
            }
        } catch (Exception e) {
            LogManager.write("audit", String.format("notify message reply: infoID=%s, err=%s", this.infoID, e.toString()), null);
        }

        dismiss();
    }
}
