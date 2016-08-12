package com.hantek.ttia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.module.roadutils.Station;

public class QuickChangeDialogFragment extends DialogFragment implements
        Runnable, View.OnClickListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    public static final String TAG = QuickChangeDialogFragment.class.getName();

    private StationListAdapter adapter;
    private DialogClickListener callback;
    private Button directButton, backButton;
    private ListView stationListView;
    private Handler handler;
    private int lastStation = -1;

    public QuickChangeDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");

        try {
            callback = (DialogClickListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getActivity().getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getActivity().getWindow().setAttributes(lp);

        // Set customize view.
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_change_night, null);
        directButton = (Button) view.findViewById(R.id.directButton);

        backButton = (Button) view.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        stationListView = (ListView) view.findViewById(R.id.stationListView);
        handler = new Handler();

        Road road = RoadManager.getInstance().getCurrentRoad();
        boolean b1 = RoadManager.getInstance().checkDirect(road.id, 1, road.branch);
        boolean b2 = RoadManager.getInstance().checkDirect(road.id, 2, road.branch);

        if (b1 && b2) {
            directButton.setEnabled(true);
            directButton.setOnClickListener(this);
        } else
            directButton.setEnabled(false);

        switch (road.direct) {
            case 1:
                directButton.setText(getActivity().getString(R.string.direct_2));
                break;
            case 2:
                directButton.setText(getActivity().getString(R.string.direct_1));
                break;
        }

        adapter = new StationListAdapter(getActivity());
        adapter.setData(road.stationArrayList);
        stationListView.setAdapter(adapter);
        stationListView.setOnItemClickListener(this);
        stationListView.setOnScrollListener(this);
        Station station = RoadManager.getInstance().getCurrentStationForSearch();
        if (station != null) {
            int id = station.id;
            stationListView.setSelection(adapter.setPosition(id));
        }

        builder.setView(view);
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        getDialog().setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(this, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.directButton:
                ((MainActivity) getActivity()).changeDirect();
                callback.onDirectChanged();
                break;
        }
        dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((MainActivity) getActivity()).changeStation(adapter.getItem(position).id);
        dismiss();
    }

    @Override
    public void run() {
        Station station = RoadManager.getInstance().getCurrentStationForSearch();
        // 避免畫面拖移後又返回
        if (station != null) {
            int id = station.id;
            if (lastStation != id) {
                lastStation = id;
                stationListView.setSelection(adapter.setPosition(id));
            }
        } else {
            if (lastStation != -1) {
                lastStation = -1;
                adapter.setPosition(-1);
                stationListView.setSelection(-1);
            }
        }
        handler.postDelayed(this, 1000);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(TAG, "onScrollStateChanged=" + scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(TAG, "onScroll=" + totalItemCount);
    }
}
