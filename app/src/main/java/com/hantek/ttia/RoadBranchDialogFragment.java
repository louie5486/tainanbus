package com.hantek.ttia;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.view.BranchCallback;
import com.hantek.ttia.view.BranchLayout;

import java.util.ArrayList;

public class RoadBranchDialogFragment extends DialogFragment implements View.OnClickListener, BranchCallback {
    public static final String TAG = RoadBranchDialogFragment.class.getName();

    LinearLayout mainBranch;
    BranchLayout branchLayout;
    Button branch0Button, branch1Button, branchBackButton;
    int roadID;
    int direct;
    String branch;

    public void setData(int roadID, int direct, String branch) {
        this.roadID = roadID;
        this.direct = direct;
        this.branch = branch;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.customize_road_branch, null);

        mainBranch = (LinearLayout) view.findViewById(R.id.mainBranch);
        branchLayout = (BranchLayout) view.findViewById(R.id.view);

        mainBranch.setVisibility(View.VISIBLE);
        branchLayout.setVisibility(View.GONE);

        branch0Button = (Button) view.findViewById(R.id.branch0Button);
        branch0Button.setOnClickListener(this);

        branch1Button = (Button) view.findViewById(R.id.branch1Button);
        branch1Button.setOnClickListener(this);

        branchBackButton = (Button) view.findViewById(R.id.branchBackButton);
        branchBackButton.setOnClickListener(this);

        ArrayList<Road> tmp = RoadManager.getInstance().hasBranch(roadID, false);
        if (tmp.size() == 0) {
            branch1Button.setEnabled(false);
            // 會直接進入主線選擇路線
        } else {
//            branchLayout.setBranch(getActivity(), tmp);
            branchLayout.setBranchFixed(getActivity(), tmp);
            branchLayout.setCallback(this);
        }

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
            case R.id.branch0Button:
                if (!((MainActivity) getActivity()).submitRoad(roadID, direct, "0")) {
                    Toast.makeText(getActivity(), "路線ID 回報失敗", Toast.LENGTH_SHORT).show();
                }
                dismiss();
                break;
            case R.id.branch1Button:
                mainBranch.setVisibility(View.GONE);
                branchLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.branchBackButton:
                dismiss();
                break;
        }
    }

    @Override
    public void branchSubmit(Road road) {
        Log.d(TAG, "submit branch=" + branch);
        if (!((MainActivity) getActivity()).submitRoad(roadID, direct, road.branch)) {
            Toast.makeText(getActivity(), "路線ID 回報失敗", Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    @Override
    public void branchBack() {
        dismiss();
    }
}