package com.hantek.ttia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.protocol.a1a4.BusStatus;
import com.hantek.ttia.protocol.a1a4.RegisterResponse;

/**
 * main
 */
public class FragmentMain extends GlobalFragment implements OnClickListener {
    private static final String TAG = FragmentMain.class.getName();

    private TextView carStatusTextView, roadTextView, driverTextView;
    private Button goButton, secondGoButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int resID = R.layout.fragment_main_night;
//        if(((MainActivity) getActivity()).getUITheme())

        View rootView = inflater.inflate(resID, container, false);
        Log.d(TAG, "OnCreateView");

        initView(rootView);

        refreshCarStatus();
        refreshRoad();
        refreshDriver();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.carStatusTextView:
                ((MainActivity) getActivity()).gotoChooseCarStatus();
                break;
            case R.id.roadTextView:
                ((MainActivity) getActivity()).gotoSelectRoad();
                break;
            case R.id.driverTextView:
                ((MainActivity) getActivity()).onLogoff();
                break;
            case R.id.goButton:
                ((MainActivity) getActivity()).startWork(true);
                break;
            case R.id.secondGoButton:
                ((MainActivity) getActivity()).secondGo();
                int a=0;
                break;
        }
    }

    @Override
    public void onResume() {
        refreshCarStatus();
        super.onResume();
    }

    private void initView(View rootView) {
        this.carStatusTextView = (TextView) rootView.findViewById(R.id.carStatusTextView);
        this.carStatusTextView.setOnClickListener(this);
        this.carStatusTextView.setText("");

        this.roadTextView = (TextView) rootView.findViewById(R.id.roadTextView);
        this.roadTextView.setOnClickListener(this);
        this.roadTextView.setText("");

        this.driverTextView = (TextView) rootView.findViewById(R.id.driverTextView);
        this.driverTextView.setOnClickListener(this);
        this.driverTextView.setText("");

        this.goButton = (Button) rootView.findViewById(R.id.goButton);
        this.goButton.setOnClickListener(this);

        this.secondGoButton = (Button) rootView.findViewById(R.id.secondGoButton);
        this.secondGoButton.setOnClickListener(this);
    }

    private void refreshCarStatus() {
        String content = String.format("%s", filterStatus(SystemPara.getInstance().getCurrentBusStatus()));
        this.carStatusTextView.setText(content);
    }

    private void refreshRoad() {
        Road road = RoadManager.getInstance().getCurrentRoad();
        if (road != null) {
            String content = textModeDefault(road);
            int cusID = SystemPara.getInstance().getCustomerID();
            if (cusID == 7600 || cusID == 999)
                content = textMode7600(road);

            this.roadTextView.setText(content);
        } else {
            this.roadTextView.setText("請輸入路線代碼");
        }
    }

    private String textModeDefault(Road road) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("路線:%s ", road.id));
        if (road.id == 65535)
            sb.append("\n" + road.beginStation);
        else {
            sb.append(road.branch.equalsIgnoreCase("0") ? "主線" : ("支線" + road.branch));
            sb.append("\n" + road.beginStation + "-" + road.endStation);
        }
        sb.append("\n" + filterDirect(road.direct));
        return sb.toString();
    }

    private String textMode7600(Road road) {
        StringBuilder sb = new StringBuilder();
        int newID = getRouteID(road.id, road.branch);
        sb.append(String.format("路線:%s ", newID));
        if (road.id == 65535)
            sb.append("\n" + road.beginStation);
        else {
            if (newID < 10000) {
                sb.append("支線" + road.branch);
            }
            sb.append("\n" + road.beginStation + "-" + road.endStation);
        }
        sb.append("\n" + filterDirect(road.direct));
        return sb.toString();
    }

    private int getRouteID(int routeID, String branch) {
        // 其他路線固定為65535
        if (routeID == 65535)
            return 65535;

        // 新營客運(999), 興南客運(7600), 定義5碼的路線ID.
        if (SystemPara.getInstance().getCustomerID() == 999 || SystemPara.getInstance().getCustomerID() == 7600) {
            try {
                routeID = Integer.parseInt(branch) + routeID * 10;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return routeID;
    }

    private void refreshDriver() {
        this.driverTextView.setText("登出");
        RegisterResponse response = SystemPara.getInstance().getRegisterResponse();

        if (response == null) {
            return;
        }

        String content = String.format("%d %s", response.driverID, response.driverName);
        // 2016-03-23 顯示登出
        this.driverTextView.setText("登出");
    }

    private String filterStatus(BusStatus busStatus) {
        switch (busStatus.getValue()) {
            case 0x1:
                return getString(R.string.car_status_online);
            case 0x2:
                return getString(R.string.car_status_accident);
            case 0x4:
                return getString(R.string.car_status_breakdown);
            case 0x8:
                return getString(R.string.car_status_jam);
            case 0x10:
                return getString(R.string.car_status_emergency);
            case 0x20:
                return getString(R.string.car_status_service);
            default:
                return getString(R.string.car_status_offline);
        }
    }

    private String filterDirect(int direct) {
        switch (direct) {
            case 1:
                return getActivity().getString(R.string.direct_1);
            case 2:
                return getActivity().getString(R.string.direct_2);
            default:
                return getActivity().getString(R.string.direct_0);
        }
    }
}