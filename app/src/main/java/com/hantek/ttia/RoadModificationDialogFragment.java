package com.hantek.ttia;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class RoadModificationDialogFragment extends DialogFragment implements View.OnClickListener {

    private RoadListAdapter adapter = null;
    private Button confirmButton, cancelButton;
    private ListView mListView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        adapter = new RoadListAdapter(getActivity());
        adapter.setData(RoadManager.getInstance().getLocalRoadData());

//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(getActivity().getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        getActivity().getWindow().setAttributes(lp);

        View view = getActivity().getLayoutInflater().inflate(R.layout.customize_road_list, null);
        confirmButton = (Button) view.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(this);

        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.roadListView);
        mListView.setDivider(new ColorDrawable(0xffd4d5d6));
        mListView.setDividerHeight(2);

        mListView.setAdapter(adapter);
        adapter.setPosition(0);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setPosition(position);
            }
        });

        Dialog dialog = new Dialog(getActivity()); // builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirmButton:
                int position = adapter.getCurrentPosition();
                if (position != -1) {
                    Road road = adapter.getItem(position).getRoad();
                    if (!((MainActivity) getActivity()).submitRoad(road.id, road.direct, road.branch)) {
                        Toast.makeText(getActivity(), "路線ID 回報失敗", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.cancelButton:
                // nothing...
                break;
        }

        dismiss();
    }
}