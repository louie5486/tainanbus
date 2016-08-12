package com.hantek.ttia.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hantek.ttia.R;
import com.hantek.ttia.module.roadutils.Road;

import java.util.ArrayList;

public class BranchLayout extends LinearLayout implements View.OnClickListener {

    LinearLayout mainLayout;
    Button backButton;

    /**
     * 一行可顯示的數量
     */
    static final int row_size = 6;
    Button[] buttons;
    BranchCallback branchCallback;

    public BranchLayout(Context context) {
        super(context);
    }

    public BranchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_branch, this);
        mainLayout = (LinearLayout) view.findViewById(R.id.mainLayout);
        mainLayout.removeAllViews();

        backButton = (Button) view.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
    }

    public void setCallback(BranchCallback callback) {
        branchCallback = callback;
    }

    /**
     * 只顯示系統存在的支線按鈕
     *
     * @param context
     * @param data
     */
    public void setBranch(Context context, ArrayList data) {
        buttons = new Button[data.size()];

        LinearLayout linearLayout = null;
        for (int i = 0; i < data.size(); i++) {
            if (i % row_size == 0) {
                linearLayout = generateLayout(context);
                mainLayout.addView(linearLayout);
            }
            Button b = new Button(context);
            b.setId(i + 999);
            b.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            b.setTextAppearance(context, R.style.btn_road_branch_night);
            b.setBackgroundResource(R.drawable.btn_white_night);
            b.setText("支線" + ((Road) data.get(i)).branch);
            b.setOnClickListener(this);
            b.setTag(data.get(i));
            buttons[i] = b;
            linearLayout.addView(b);
        }
    }

    /**
     * 固定顯示A~Z, 但只能按下系統存在的支線按鈕
     *
     * @param context
     * @param data
     */
    public void setBranchFixed(Context context, ArrayList data) {
        buttons = new Button[26]; //A~Z

        LinearLayout linearLayout = null;
        for (int i = 0; i < 26; i++) {
            if (i % row_size == 0) {
                linearLayout = generateLayout(context);
                mainLayout.addView(linearLayout);
            }
            String text = Character.toString((char) (i + 65));
            Button b = new Button(context);
            b.setId(i + 999);
            b.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            b.setText("支線" + text);
            Road tmpRoad = findBranch(data, text);
            if (tmpRoad != null) {
                b.setTag(tmpRoad);
                b.setEnabled(true);
                b.setTextAppearance(context, R.style.btn_road_branch_night);
                b.setBackgroundResource(R.drawable.btn_white_night);
                b.setOnClickListener(this);
            } else {
                b.setEnabled(false);
            }
            buttons[i] = b;
            linearLayout.addView(b);
        }
    }

    private Road findBranch(ArrayList data, String branch) {
        for (Object road : data) {
            if (((Road) road).branch.equalsIgnoreCase(branch)) {
                return (Road) road;
            }
        }

        return null;
    }

    private LinearLayout generateLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        linearLayout.setWeightSum(row_size);
        return linearLayout;
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.backButton:
                    branchCallback.branchBack();
                    break;
                default:
                    int id = v.getId() - 999;
                    Button btn = buttons[id];
                    Road road = (Road) btn.getTag();
                    branchCallback.branchSubmit(road);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

