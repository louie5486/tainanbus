package com.hantek.ttia;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;

import java.util.ArrayList;

import component.LogManager;

public class SelectDirectFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = SelectDirectFragment.class.getName();

    private RoadAdapter adapter;
    private Button directGoButton, directBackButton, directOtherButton, cancelButton;
    private ListView mListView;

    private int roadID;
    private Road road;

    public SelectDirectFragment() {
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
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_select_direct_night, null);
        directGoButton = (Button) view.findViewById(R.id.direct1Button);
        directGoButton.setOnClickListener(this);

        directBackButton = (Button) view.findViewById(R.id.direct2Button);
        directBackButton.setOnClickListener(this);

        directOtherButton = (Button) view.findViewById(R.id.otherButton);
        directOtherButton.setOnClickListener(this);

        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.roadListView);
        mListView.setDivider(new ColorDrawable(0xffd4d5d6));
        mListView.setDividerHeight(2);

        adapter = new RoadAdapter(getActivity());
        //重新搜尋
        //adapter.setData(((MainActivity) getActivity()).getBranch(this.roadID)); // v18, down
        adapter.setData(RoadManager.getInstance().getBranch(this.roadID, this.road.branch)); // v19

        mListView.setAdapter(adapter);

        adapter.select(0);
        road = adapter.getItem(0).getRoad();
        directGoButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 1, road.branch));
        directBackButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 2, road.branch));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.select(position);
                road = adapter.getItem(position).getRoad();

                directGoButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 1, road.branch));
                directBackButton.setEnabled(RoadManager.getInstance().checkDirect(road.id, 2, road.branch));
            }
        });

//        builder.setView(view);
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
}
