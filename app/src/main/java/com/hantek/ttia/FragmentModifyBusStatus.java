package com.hantek.ttia;

import com.hantek.ttia.protocol.a1a4.BusStatus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 變更行車狀態
 */
public class FragmentModifyBusStatus extends GlobalFragment implements View.OnClickListener {
    private static final String TAG = FragmentModifyBusStatus.class.getName();

    private Button busStatusGoBackButton;
    private Button carModifyButton1, carModifyButton2, carModifyButton3, carModifyButton4, carModifyButton5, carModifyButton6, carModifyButton7;

    public FragmentModifyBusStatus() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_modify_bus_status_night, container, false);

        this.busStatusGoBackButton = (Button) rootView.findViewById(R.id.busStatusGoBackButton);
        this.busStatusGoBackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        this.carModifyButton1 = (Button) rootView.findViewById(R.id.carModifyButton1);
        this.carModifyButton1.setOnClickListener(this);

        this.carModifyButton2 = (Button) rootView.findViewById(R.id.carModifyButton2);
        this.carModifyButton2.setOnClickListener(this);

        this.carModifyButton3 = (Button) rootView.findViewById(R.id.carModifyButton3);
        this.carModifyButton3.setOnClickListener(this);

        this.carModifyButton4 = (Button) rootView.findViewById(R.id.carModifyButton4);
        this.carModifyButton4.setOnClickListener(this);

        this.carModifyButton5 = (Button) rootView.findViewById(R.id.carModifyButton5);
        this.carModifyButton5.setOnClickListener(this);

        this.carModifyButton6 = (Button) rootView.findViewById(R.id.carModifyButton6);
        this.carModifyButton6.setOnClickListener(this);

        this.carModifyButton7 = (Button) rootView.findViewById(R.id.carModifyButton7);
        this.carModifyButton7.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        BusStatus status = null;
        switch (v.getId()) {
            case R.id.carModifyButton1:
                status = BusStatus.OnRoad;
                break;
            case R.id.carModifyButton2:
                status = BusStatus.Accident;
                break;
            case R.id.carModifyButton3:
                status = BusStatus.BreakDown;
                break;
            case R.id.carModifyButton4:
                status = BusStatus.Jam;
                break;
            case R.id.carModifyButton5:
                status = BusStatus.Emergency;
                break;
            case R.id.carModifyButton6:
                status = BusStatus.OnService;
                break;
            case R.id.carModifyButton7:
                status = BusStatus.Offline;
                break;
            default:
                return;
        }

        ((MainActivity) getActivity()).updateBusStatus(status);
        onBackPressed();
    }
}