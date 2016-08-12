package com.hantek.ttia.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hantek.ttia.R;

public class StatusLayout extends LinearLayout {

    private ImageView gpsIV;
    private ImageView accIV;
    private ImageView modeIV;
    private ImageView signalIV;
    private ImageView comm1IV;
    private ImageView comm2IV;
    private ImageView di1IV;
    private ImageView di2IV;
    private ImageView ttyUSB1IV;
    private ImageView ttyUSB2IV;
    private ImageView ttyUSB3IV;
    private TextView timeTV;

    private TextView advertTV;

    private boolean gpsFlag = false;
    private int lastGps = -1;
    private int lastMode = -1;
    private int lastSignal = -1;

    private int lastComm1 = -1;
    private int lastComm2 = -1;

    private int lastDi1 = -2;
    private int lastDi2 = -2;

    private int lastttyUSB1 = -2;
    private int lastttyUSB2 = -2;
    private int lastttyUSB3 = -2;

    public StatusLayout(Context context) {
        super(context);
    }

    public StatusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(com.hantek.ttia.R.layout.status_night, this);
        gpsIV = (ImageView) view.findViewById(com.hantek.ttia.R.id.gpsImageView);
        accIV = (ImageView) view.findViewById(com.hantek.ttia.R.id.accImageView);

        di1IV = (ImageView) view.findViewById(com.hantek.ttia.R.id.di1ImageView);
        di2IV = (ImageView) view.findViewById(com.hantek.ttia.R.id.di2ImageView);

        comm1IV = (ImageView) view.findViewById(com.hantek.ttia.R.id.comm1ImageView);
        comm2IV = (ImageView) view.findViewById(com.hantek.ttia.R.id.comm2ImageView);

        modeIV = (ImageView) view.findViewById(com.hantek.ttia.R.id.modeImageView);
        signalIV = (ImageView) view.findViewById(com.hantek.ttia.R.id.signalImageView);

        timeTV = (TextView) view.findViewById(com.hantek.ttia.R.id.timeTextView);

        ttyUSB1IV = (ImageView) view.findViewById(com.hantek.ttia.R.id.dcrImageView);
        ttyUSB2IV = (ImageView) view.findViewById(com.hantek.ttia.R.id.etmImageView);
        ttyUSB3IV = (ImageView) view.findViewById(com.hantek.ttia.R.id.ledImageView);

        advertTV = (TextView) view.findViewById(R.id.advertTextView);
    }

    public void setGPS(int status) {
        if (status == lastGps && lastGps != 0)
            return;

        switch (status) {
            case -1:
                gpsIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_gps_off_black_48dp_red));
                break;
            case 0:
                if (gpsFlag)
                    gpsIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_gps_not_fixed_black_48dp_color_yellow));
                else
                    gpsIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_gps_fixed_black_48dp_color_yellow));
                gpsFlag = !gpsFlag;
                break;
            case 1:
                gpsIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_gps_fixed_black_48dp_color));
                break;
        }

        lastGps = status;
    }

    public void setAcc(int status) {
        switch (status) {
            case 0:
                accIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.key_down));
                break;
            case 1:
                accIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.key_up));
                break;
        }
    }

    public void setMode(int mode) {
        if (mode == lastMode)
            return;

        switch (mode) {
            case -1:
                modeIV.setVisibility(View.INVISIBLE);
                break;
            case 0:
                modeIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_network_wifi_black_48dp_blue));
                modeIV.setVisibility(View.VISIBLE);
                break;
            case 1:
                modeIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_network_cell_black_48dp_blue));
                modeIV.setVisibility(View.VISIBLE);
                break;
        }

        lastMode = mode;
    }

    public void setTime(String time) {
        timeTV.setText(time);
    }

    public void setSignalStrengths(int signalStrengths) {
        if (signalStrengths == lastSignal)
            return;

        switch (signalStrengths) {
            case 1:
                signalIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_signal_cellular_1_bar_white_48dp));
                break;
            case 2:
                signalIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_signal_cellular_2_bar_white_48dp));
                break;
            case 3:
                signalIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_signal_cellular_3_bar_white_48dp));
                break;
            case 4:
                signalIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_signal_cellular_4_bar_white_48dp));
                break;
            default:
                signalIV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_signal_cellular_0_bar_white_48dp));
                break;
        }

        lastSignal = signalStrengths;
    }

    public void setComm1(int status) {
        if (status == lastComm1)
            return;

        switch (status) {
            case 1:
                comm1IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_import_export_black_48dp_green));
                comm1IV.setVisibility(View.VISIBLE);
                break;
            case 0:
                comm1IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_import_export_black_48dp_red));
                comm1IV.setVisibility(View.VISIBLE);
                break;
            default:
                comm1IV.setVisibility(View.INVISIBLE);
                break;
        }

        lastComm1 = status;
    }

    public void setComm2(int status) {
        if (status == lastComm2)
            return;

        switch (status) {
            case 1:
                comm2IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_import_export_black_48dp_green));
                comm2IV.setVisibility(View.VISIBLE);
                break;
            case 0:
                comm2IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_import_export_black_48dp_red));
                comm2IV.setVisibility(View.VISIBLE);
                break;
            default:
                comm2IV.setVisibility(View.INVISIBLE);
                break;
        }

        lastComm2 = status;
    }

    public void setDi1(int status) {
        if (status == lastDi1)
            return;

        switch (status) {
            case 0:
                di1IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_looks_one_black_48dp_on));
                di1IV.setVisibility(View.VISIBLE);
                break;
            case 1:
                di1IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_looks_one_black_48dp_off));
                di1IV.setVisibility(View.VISIBLE);
                break;
            default:
                di1IV.setVisibility(View.INVISIBLE);
                break;
        }

        lastDi1 = status;
    }

    public void setDi2(int status) {
        if (status == lastDi2)
            return;

        switch (status) {
            case 0:
                di2IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_looks_two_black_48dp_on));
                di2IV.setVisibility(View.VISIBLE);
                break;
            case 1:
                di2IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.ic_looks_two_black_48dp_off));
                di2IV.setVisibility(View.VISIBLE);
                break;
            default:
                di2IV.setVisibility(View.INVISIBLE);
                break;
        }

        lastDi2 = status;
    }

    public void setTtyUSB1IV(int status) {
        if (status == lastttyUSB1)
            return;

        switch (status) {
            case 1:
                ttyUSB1IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.imput_device_blue));
                ttyUSB1IV.setVisibility(View.VISIBLE);
                break;
            case 0:
                ttyUSB1IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.imput_device_red));
                ttyUSB1IV.setVisibility(View.VISIBLE);
                break;
            default:
                ttyUSB1IV.setVisibility(View.INVISIBLE);
                break;
        }

        lastttyUSB1 = status;
    }

    public void setTtyUSB2IV(int status) {
        if (status == lastttyUSB2)
            return;

        switch (status) {
            case 1:
                ttyUSB2IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.imput_device_blue));
                ttyUSB2IV.setVisibility(View.VISIBLE);
                break;
            case 0:
                ttyUSB2IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.imput_device_red));
                ttyUSB2IV.setVisibility(View.VISIBLE);
                break;
            default:
                ttyUSB2IV.setVisibility(View.INVISIBLE);
                break;
        }

        lastttyUSB2 = status;
    }

    public void setTtyUSB3IV(int status) {
        if (status == lastttyUSB3)
            return;

        switch (status) {
            case 1:
                ttyUSB3IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.imput_device_blue));
                ttyUSB3IV.setVisibility(View.VISIBLE);
                break;
            case 0:
                ttyUSB3IV.setImageDrawable(getResources().getDrawable(com.hantek.ttia.R.drawable.imput_device_red));
                ttyUSB3IV.setVisibility(View.VISIBLE);
                break;
            default:
                ttyUSB3IV.setVisibility(View.INVISIBLE);
                break;
        }

        lastttyUSB3 = status;
    }

    public void setAdvert(String fileSize) {
        advertTV.setText(fileSize);
    }
}
