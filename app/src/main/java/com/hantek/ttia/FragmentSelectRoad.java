package com.hantek.ttia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hantek.ttia.module.roadutils.Road;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSelectRoad extends GlobalFragment implements OnClickListener {
    private static final String TAG = FragmentSelectRoad.class.getName();

    private TextView roadIDTextView, roadDetailTextView;
    private Button digiButton1, digiButton2, digiButton3, digiButton4, digiButton5, digiButton6, digiButton7, digiButton8, digiButton9, digiButton0;
    private StringBuilder sb = new StringBuilder();
    private ArrayList<Road> tmp;

    public FragmentSelectRoad() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_road_night, container, false);

        this.roadIDTextView = (TextView) rootView.findViewById(R.id.roadIDTextView);
        this.roadDetailTextView = (TextView) rootView.findViewById(R.id.roadDetailTextView);
        this.roadDetailTextView.setSelected(true);

        Button showListButton = (Button) rootView.findViewById(R.id.showListButton);
        showListButton.setOnClickListener(this);

        Button quitButton = (Button) rootView.findViewById(R.id.quitButton);
        quitButton.setOnClickListener(this);

        Button clearButton = (Button) rootView.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);

        Button roadSubmitButton = (Button) rootView.findViewById(R.id.roadSubmitButton);
        roadSubmitButton.setOnClickListener(this);

        this.digiButton0 = (Button) rootView.findViewById(R.id.roadIDButton0);
        this.digiButton1 = (Button) rootView.findViewById(R.id.roadIDButton1);
        this.digiButton2 = (Button) rootView.findViewById(R.id.roadIDButton2);
        this.digiButton3 = (Button) rootView.findViewById(R.id.roadIDButton3);
        this.digiButton4 = (Button) rootView.findViewById(R.id.roadIDButton4);
        this.digiButton5 = (Button) rootView.findViewById(R.id.roadIDButton5);
        this.digiButton6 = (Button) rootView.findViewById(R.id.roadIDButton6);
        this.digiButton7 = (Button) rootView.findViewById(R.id.roadIDButton7);
        this.digiButton8 = (Button) rootView.findViewById(R.id.roadIDButton8);
        this.digiButton9 = (Button) rootView.findViewById(R.id.roadIDButton9);

        this.digiButton0.setOnClickListener(this);
        this.digiButton1.setOnClickListener(this);
        this.digiButton2.setOnClickListener(this);
        this.digiButton3.setOnClickListener(this);
        this.digiButton4.setOnClickListener(this);
        this.digiButton5.setOnClickListener(this);
        this.digiButton6.setOnClickListener(this);
        this.digiButton7.setOnClickListener(this);
        this.digiButton8.setOnClickListener(this);
        this.digiButton9.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showListButton:
                RoadModificationDialogFragment fragment = new RoadModificationDialogFragment();
                fragment.show(getFragmentManager(), getTag());
                break;
            case R.id.quitButton:
                ((MainActivity) getActivity()).quitSelectRoad();
                break;
            case R.id.clearButton:
                this.sb = new StringBuilder();
                updateRoadID();
                roadDetailTextView.setSelected(false);
                roadDetailTextView.setText("");
                break;
            case R.id.roadSubmitButton:
                if (this.sb.length() > 0) {
                    int roadID = Integer.parseInt(this.sb.toString());
                    if (roadID > 65535) {
                        return;
                    } else if (roadID == 65535) {
                        if (!((MainActivity) getActivity()).submitRoad(65535, 0, "0")) {
                            Toast.makeText(getActivity(), "路線ID 回報失敗", Toast.LENGTH_SHORT).show();
                        }
                    } else if (sb.length() >= 5) {
                        if (!((MainActivity) getActivity()).submitRoad(roadID / 10, -1, String.valueOf(roadID % 10))) {
                            Toast.makeText(getActivity(), "路線ID 回報失敗", Toast.LENGTH_SHORT).show();
                        }
                    } else {// if (sb.length() >= 4) {
                        if (!((MainActivity) getActivity()).submitRoad(roadID, -1, "-1")) {
                            Toast.makeText(getActivity(), "路線ID 回報失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case R.id.roadIDButton0:
            case R.id.roadIDButton1:
            case R.id.roadIDButton2:
            case R.id.roadIDButton3:
            case R.id.roadIDButton4:
            case R.id.roadIDButton5:
            case R.id.roadIDButton6:
            case R.id.roadIDButton7:
            case R.id.roadIDButton8:
            case R.id.roadIDButton9:
                Button clickButton = (Button) v;

                int roadID = Integer.parseInt(sb.toString() + clickButton.getText().toString());
                int len = sb.toString().length() + clickButton.getText().toString().length();
                if (roadID > 65535 || len > 5)
                    return;

                this.sb.append(clickButton.getText().toString());
                updateRoadID();
                if (sb.length() >= 5) {
                    tmp = ((MainActivity) getActivity()).queryRoad(roadID / 10, -1, clickButton.getText().toString());
                } else// if (sb.length() >= 4)
                    tmp = ((MainActivity) getActivity()).queryRoad(roadID, -1);
//                else
//                    tmp = new ArrayList<>();

                if (tmp.size() == 0) {
                    updateRoadDetails(null);
                } else {
                    updateRoadDetails(tmp);
                }
                break;
        }
    }

    private void updateRoadID() {
        String content;
        if (this.sb.length() == 0) {
            content = getString(R.string.msg_input_roadid);
        } else
            content = sb.toString();

        this.roadIDTextView.setText(content);
    }

    private void updateRoadDetails(ArrayList<Road> tmp) {
        Road road;
        if (tmp == null) {
            road = null;
        } else if (tmp.size() == 1) {
            road = tmp.get(0);
        } else {
            road = tmp.get(0);// 通常會顯示主線名稱
        }

        String content = "";
        if (road == null) {
            content = getString(R.string.msg_input_road_details);
        } else {
            Log.d(TAG, "Update Road, size=" + tmp.size());
            if (road.id == 65535) {
                content = road.beginStation;
            } else {
                if (tmp.size() == 1 || tmp.size() == 2) {
                    content = road.branch.equalsIgnoreCase("0") ? "(主)" : ("(支" + road.branch + ")");
                }
                if (road.endStation.equalsIgnoreCase("")) {
                    content += road.beginStation;
                } else
                    content += String.format("%s - %s", road.beginStation, road.endStation);
            }
        }

        this.roadDetailTextView.setSelected(true);
        this.roadDetailTextView.setText(content);
    }
}