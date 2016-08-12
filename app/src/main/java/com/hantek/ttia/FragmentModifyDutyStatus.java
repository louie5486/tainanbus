package com.hantek.ttia;

import com.hantek.ttia.protocol.a1a4.DutyStatus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 變更勤務
 */
public class FragmentModifyDutyStatus extends GlobalFragment implements View.OnClickListener {
    private static final String TAG = FragmentModifyDutyStatus.class.getName();

    private Button dutyStatusGoBackButton;
    private Button dutyModifyButton1, dutyModifyButton2, dutyModifyButton3, dutyModifyButton4, dutyModifyButton5;

    public FragmentModifyDutyStatus() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_modify_duty_status_night, container, false);

        this.dutyStatusGoBackButton = (Button) rootView.findViewById(R.id.dutyStatusGoBackButton);
        this.dutyStatusGoBackButton.setOnClickListener(this);

        this.dutyModifyButton1 = (Button) rootView.findViewById(R.id.dutyModifyButton1);
        this.dutyModifyButton1.setOnClickListener(this);

        this.dutyModifyButton2 = (Button) rootView.findViewById(R.id.dutyModifyButton2);
        this.dutyModifyButton2.setOnClickListener(this);

        this.dutyModifyButton3 = (Button) rootView.findViewById(R.id.dutyModifyButton3);
        this.dutyModifyButton3.setOnClickListener(this);

        this.dutyModifyButton4 = (Button) rootView.findViewById(R.id.dutyModifyButton4);
        this.dutyModifyButton4.setOnClickListener(this);

        this.dutyModifyButton5 = (Button) rootView.findViewById(R.id.dutyModifyButton5);
        this.dutyModifyButton5.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        DutyStatus status = null;
        switch (v.getId()) {
            case R.id.dutyModifyButton1:
                status = DutyStatus.Ready;
                break;
            case R.id.dutyModifyButton2:
                status = DutyStatus.Start;
                break;
            case R.id.dutyModifyButton3:
                status = DutyStatus.Stop;
                break;
            case R.id.dutyModifyButton4:
                status = DutyStatus.Full;
                break;
            case R.id.dutyModifyButton5:
                status = DutyStatus.OnRoad;
                break;
            case R.id.dutyStatusGoBackButton:
                onBackPressed();
                return;
            default:
                return;
        }

        Log.d(TAG, "Choose Duty:" + status);
        ((MainActivity) getActivity()).updateDutyStatus(status);
        if (status != DutyStatus.Stop)
            onBackPressed();
    }
}