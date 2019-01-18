package com.hantek.ttia;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.view.BranchCallback;
import com.hantek.ttia.view.BranchLayout2;

import java.util.ArrayList;

import component.LogManager;

public class SelectDirectFragment2 extends DialogFragment implements View.OnClickListener, BranchCallback {
    public static final String TAG = SelectDirectFragment2.class.getName();

    private Button directGoButton, directBackButton, directOtherButton, cancelButton;
    private TextView detailsTextView, roadDetail2TextView, infoTextView;
    BranchLayout2 branchLayout2;
    private int roadID;
    private Road road;

    public SelectDirectFragment2() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");
        // the content
//        final LinearLayout root = new LinearLayout(getActivity());
//        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(getActivity().getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        getActivity().getWindow().setAttributes(lp);

        // Set customize view.
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_select_direct_night2, null);
        directGoButton = (Button) view.findViewById(R.id.direct1Button);
        directGoButton.setOnClickListener(this);

        directBackButton = (Button) view.findViewById(R.id.direct2Button);
        directBackButton.setOnClickListener(this);

        directOtherButton = (Button) view.findViewById(R.id.otherButton);
        directOtherButton.setOnClickListener(this);

        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        detailsTextView = (TextView) view.findViewById(R.id.roadDetailTextView);
//        detailsTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
//        detailsTextView.setSelected(true);

        roadDetail2TextView = (TextView) view.findViewById(R.id.roadDetail2TextView);
        roadDetail2TextView.setSelected(true);

        infoTextView = (TextView) view.findViewById(R.id.infoTextView);

        branchLayout2 = (BranchLayout2) view.findViewById(R.id.view2);
        branchLayout2.setCallback(this);
        branchLayout2.setBranch(getActivity(), ((MainActivity) getActivity()).getBranch(this.roadID));

//        directGoButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 1, road.branch));
//        directBackButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 2, road.branch));

        Dialog dialog = new Dialog(getActivity()); // builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        getDialog().setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.direct1Button:
                ((MainActivity) getActivity()).submitRoad(this.roadID, 1, road.branch);
                break;
            case R.id.direct2Button:
                ((MainActivity) getActivity()).submitRoad(this.roadID, 2, road.branch);
                break;
            case R.id.otherButton:
                ((MainActivity) getActivity()).submitRoad(this.roadID, 0, road.branch);
                break;
            case R.id.cancelButton:

                break;
        }

        dismiss();
    }

    public void setRoad(ArrayList<Road> tmpList) {
        for (Road r : tmpList) {
            LogManager.write("debug", "debug print: " + r.toString(), null);
        }
        this.roadID = tmpList.get(0).id;
        this.road = tmpList.get(0);
    }

    public void updateText(Road road) {
        if (road == null)
            return;

        // 5路 起站
        int newID = getRouteID(roadID, road.branch);
        StringBuilder sb = new StringBuilder();
//        if (SystemPara.getInstance().getCustomerID() == 9800)
            sb.append(newID + " 路");
//        sb.append(road.beginStation + "\r\n");
        detailsTextView.setText(sb.toString());

        // 迄站
        if (road.endStation.equalsIgnoreCase("")){
            roadDetail2TextView.setText(road.beginStation + "\r\n");
        }else{
            roadDetail2TextView.setText(road.beginStation  + "-" + road.endStation + "\r\n");
        }
        roadDetail2TextView.setSelected(true);

        // v1v1
        infoTextView.setText("");
        String v1 = RoadManager.getInstance().checkVersion(road.id, 1, road.branch);
        String v2 = RoadManager.getInstance().checkVersion(road.id, 2, road.branch);
        if (v1.length() > 0)
            infoTextView.append(v1);
        if (v2.length() > 0)
            infoTextView.append(v2);
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

    @Override
    public void branchSubmit(Road road) {
        this.road = road;
        directGoButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 1, road.branch));
        directBackButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 2, road.branch));
        updateText(road);
    }

    @Override
    public void branchBack() {

    }
}
