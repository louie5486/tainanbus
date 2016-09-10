package com.hantek.ttia;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.module.roadutils.Station;
import com.hantek.ttia.protocol.a1a4.BusStatus;
import com.hantek.ttia.protocol.a1a4.DutyStatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FragmentOnRoad extends Fragment implements OnClickListener, Runnable, DialogClickListener, View.OnLongClickListener {
    private static final String TAG = FragmentOnRoad.class.getName();

    private RelativeLayout rl;
    private TextView timeTextView,dutyStatusTextView,carStatusTextView, roadTitleTextView, roadDirectTextView, roadCurrTextView,roadCurrentTextView, roadNextTextView, roadNext;
    private Button onRoadGoBackButton;
    private Handler mHandler = null;
    private boolean flag = false;
    private boolean flag2 = false;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private String lastCStationStr = "";
    private String lastNStationStr = "";

    public FragmentOnRoad() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_onroad, container, false);
        Log.d(TAG, "onCreateView");
        mHandler = new Handler();
        lastCStationStr = "";
        lastNStationStr = "";

        this.rl = (RelativeLayout) rootView.findViewById(R.id.onRoadLayout);
        this.rl.setOnClickListener(this);

        this.onRoadGoBackButton = (Button) rootView.findViewById(R.id.onRoadGoBackButton);
        this.onRoadGoBackButton.setOnClickListener(this);
        onRoadGoBackButton.bringToFront();

        this.dutyStatusTextView = (TextView) rootView.findViewById(R.id.dutyStatusTextView);
        this.dutyStatusTextView.setOnClickListener(this);

        this.carStatusTextView = (TextView) rootView.findViewById(R.id.carStatusTextView);
        this.carStatusTextView.setOnClickListener(this);

        this.roadTitleTextView = (TextView) rootView.findViewById(R.id.roadTitleTextView);
        this.roadTitleTextView.setOnClickListener(this);
        this.roadTitleTextView.setOnLongClickListener(this);
        this.roadTitleTextView.setSelected(true);// 跑馬燈

        this.roadDirectTextView = (TextView) rootView.findViewById(R.id.roadDirectTextView);
        this.roadDirectTextView.setOnClickListener(this);
        this.roadDirectTextView.setOnLongClickListener(this);

        this.roadCurrTextView = (TextView) rootView.findViewById(R.id.roadCurrTextView);
        this.roadCurrTextView.setOnLongClickListener(this);
        this.roadCurrTextView.setText("前站:");

        this.roadCurrentTextView = (TextView) rootView.findViewById(R.id.roadCurrentTextView);
        this.roadCurrentTextView.setOnClickListener(this);
        this.roadCurrentTextView.setOnLongClickListener(this);
        this.roadCurrentTextView.setSelected(true);

        this.roadNextTextView = (TextView) rootView.findViewById(R.id.roadNextTextView);
        this.roadNextTextView.setOnLongClickListener(this);
        this.roadNextTextView.setText("下一站:");

        this.roadNext = (TextView) rootView.findViewById(R.id.roadNext);
        this.roadNext.setOnLongClickListener(this);
        this.roadNext.setSelected(true);

        this.timeTextView = (TextView) rootView.findViewById(R.id.timeTextView);
        return rootView;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        refreshDutyStatus();
        refreshCarStatus();
        refreshRoadTitle();
//        refreshRoadDirect();
        refreshCurrentRoad();
        refreshNextRoad();
        mHandler.postDelayed(this, 1000);
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        mHandler.removeCallbacks(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach: " + activity.toString());
        super.onAttach(activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onRoadGoBackButton:
                ((MainActivity) getActivity()).stopWork();
                break;
            case R.id.dutyStatusTextView:
                ((MainActivity) getActivity()).gotoChooseDutyStatus();
                break;
            case R.id.carStatusTextView:
                ((MainActivity) getActivity()).gotoChooseCarStatus();
                break;
            default:
//                ((MainActivity) getActivity()).select(this);
                break;
        }
    }

    private void refreshCarStatus() {
        String content = String.format("車況:%s", filterCarStatus(SystemPara.getInstance().getCurrentBusStatus()));
        this.carStatusTextView.setText(content);
    }

    private void refreshDutyStatus() {
        String content = String.format("勤務:%s", filterDutyStatus(SystemPara.getInstance().getCurrentDutyStatus()));
        this.dutyStatusTextView.setText(content);
    }

    private void refreshRoadTitle() {
        Road road = RoadManager.getInstance().getCurrentRoad();
        if (road != null) {
            String content = textModeDefault(road);
            int cusID = SystemPara.getInstance().getCustomerID();
            if (cusID == 7600 || cusID == 999)
                content = textMode7600(road);

            this.roadTitleTextView.setText(genTitle(road));
            this.roadDirectTextView.setText(String.format("%s  / %s", content, filterDirect(road.direct)));
        } else {
            this.roadTitleTextView.setText("請輸入路線代碼");
            this.roadDirectTextView.setText("");
        }
    }

    /**
     * 第一行
     *
     * @param road
     * @return
     */
    private String genTitle(Road road) {
        StringBuilder sb = new StringBuilder();
        if (road.id == 65535)
            sb.append(road.beginStation);
        else if (road.endStation.equalsIgnoreCase("")) {
            sb.append(road.beginStation);
        } else {
            sb.append(road.beginStation + "-" + road.endStation);
        }
        return sb.toString();
    }

    /**
     * 預設
     *
     * @param road
     * @return
     */
    private String textModeDefault(Road road) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s 路", road.id));
        sb.append("(" + (road.branch.equalsIgnoreCase("0") ? "主線" : (" 支線 " + road.branch)) +")");
        return sb.toString();
    }

    /**
     * 特殊
     *
     * @param road
     * @return
     */
    private String textMode7600(Road road) {
        StringBuilder sb = new StringBuilder();
        int newID = getRouteID(road.id, road.branch);
        sb.append(newID);
        if (newID < 10000)
            sb.append("(" + (road.branch.equalsIgnoreCase("0") ? "主線" : road.branch) +")");
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

    private void refreshCurrentRoad() {
        Station station = RoadManager.getInstance().getCurrentStationForUI();
        String tmp = "";
        if (station != null) {
            roadCurrTextView.setText("本站:");
            tmp =  station.zhName;
        } else {
            Station prev = RoadManager.getInstance().getPreviousStation();
            if (prev != null) {
                roadCurrTextView.setText("前站:");
                tmp = prev.zhName;
            } else {
//                if (flag)
//                    tmp = "前站:";
//                else
//                    tmp = "前站:";

                flag = !flag;
            }
        }

        if (!lastCStationStr.equalsIgnoreCase(tmp)) {
            lastCStationStr = tmp;
            this.roadCurrentTextView.setText(tmp);
        }
    }

    private void refreshNextRoad() {
        Station station = RoadManager.getInstance().getNextStationUI(); //.getNextStation();
        String tmp = "";
        if (station != null)
            tmp = station.zhName;
        else {
            if (flag2)
                tmp = "搜尋中..";
            else
                tmp = "搜尋中.";

            flag2 = !flag2;
        }

        if (!lastNStationStr.equalsIgnoreCase(tmp)) {
            lastNStationStr = tmp;
            roadNext.setText(tmp);
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

    private String filterDutyStatus(DutyStatus dutyStatus) {
        switch (dutyStatus.getValue()) {
            case 0x1:
                return getString(R.string.duty_status_ready);
            case 0x2:
                return getString(R.string.duty_status_start);
            case 0x4:
                return getString(R.string.duty_status_stop);
            case 0x8:
                return getString(R.string.duty_status_full);
            case 0x10:
                return getString(R.string.duty_status_OnRoad);
            default:
                return getString(R.string.duty_status_ready);
        }
    }

    private String filterCarStatus(BusStatus busStatus) {
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

    @Override
    public void run() {
        try {
            refreshCurrentRoad();
            refreshNextRoad();
            this.timeTextView.setText(dateFormat.format(Calendar.getInstance().getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.postDelayed(this, 1000);
    }

    @Override
    public void onDirectChanged() {
        refreshRoadTitle();
//        refreshRoadDirect();
    }

    @Override
    public boolean onLongClick(View v) {
        ((MainActivity) getActivity()).select(this);
        return false;
    }
}