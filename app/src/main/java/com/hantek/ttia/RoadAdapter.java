package com.hantek.ttia;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.view.InsertCheckBox;

import java.util.ArrayList;

public class RoadAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private Context mContext;
    private ArrayList<RoadItem> mListItem;

    public RoadAdapter(Context context) {
        mContext = context;
        this.lInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(ArrayList<Road> list) {
        mListItem = new ArrayList<>();
        for (Road r : list) {
            mListItem.add(new RoadItem(r));
        }
    }

    public void select(int position) {
        if (!mListItem.get(position).isSelected()) {
            mListItem.get(position).setSelected(true);
            for (int i = 0; i < mListItem.size(); i++) {
                if (i != position) {
                    mListItem.get(i).setSelected(false);
                }
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mListItem == null ? 0 : mListItem.size();
    }

    @Override
    public RoadItem getItem(int position) {
        return mListItem == null ? null : mListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RoadItem item = null;
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.item_road, parent, false);
            item = new RoadItem(convertView);
            convertView.setTag(item);
        } else {
            item = (RoadItem) convertView.getTag();
        }

        RoadItem roadItem = mListItem.get(position);
        boolean checked = roadItem.isSelected;
        if (checked) {
            item.beginTextView.setBackgroundColor(Color.BLUE);
            item.endTextView.setBackgroundColor(Color.BLUE);
            item.roadLayout.setBackgroundColor(Color.BLUE);
            item.branchTextView.setBackgroundColor(Color.BLUE);
        } else {
            item.beginTextView.setBackgroundColor(Color.BLACK);
            item.endTextView.setBackgroundColor(Color.BLACK);
            item.roadLayout.setBackgroundColor(Color.BLACK);
            item.branchTextView.setBackgroundColor(Color.BLACK);
        }

        item.beginTextView.setText(roadItem.road.beginStation);
        item.endTextView.setText(roadItem.road.endStation);

        String text = roadItem.road.branch.equalsIgnoreCase("0") ? "主線" : ("支線" + roadItem.road.branch);
        // text += " v" + roadItem.road.version;//v19 +顯示版本
        String v1 = RoadManager.getInstance().checkVersion(roadItem.road.id, 1, roadItem.road.branch);
        if (v1.length() > 0)
            text += v1;
        String v2 = RoadManager.getInstance().checkVersion(roadItem.road.id, 2, roadItem.road.branch);
        if (v2.length() > 0)
            text += v2;

        item.branchTextView.setText(text);
        item.checkButton.setChecked(checked);

        return convertView;
    }

    class RoadItem {
        private RelativeLayout roadLayout;
        private InsertCheckBox checkButton;
        private TextView beginTextView;
        private TextView endTextView;
        private TextView branchTextView;

        private Road road;
        private boolean isSelected = false;

        public RoadItem(Road r) {
            this.road = r;
        }

        public RoadItem(View rootView) {
            roadLayout = (RelativeLayout) rootView.findViewById(R.id.roadLayout);
            checkButton = (InsertCheckBox) rootView.findViewById(R.id.checkRadioButton);
            beginTextView = (TextView) rootView.findViewById(R.id.beginTextView);
            endTextView = (TextView) rootView.findViewById(R.id.endTextView);
            branchTextView = (TextView) rootView.findViewById(R.id.branchTextView);
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public Road getRoad() {
            return road;
        }
    }
}
