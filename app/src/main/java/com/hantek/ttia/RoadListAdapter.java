package com.hantek.ttia;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.view.CheckableRelativeLayout;
import com.hantek.ttia.view.InsertCheckBox;

public class RoadListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater lInflater;
    private List<SingleChoiceItem> roadArrayList;
    private int currentPosition = -1;

    public RoadListAdapter(Context context) {
        this.mContext = context;
        this.lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<Road> arraylist) {
        roadArrayList = new ArrayList<>();
        for (Road r : arraylist) {
            roadArrayList.add(new SingleChoiceItem(r));
        }
    }

    @Override
    public int getCount() {
        return this.roadArrayList.size();
    }

    @Override
    public SingleChoiceItem getItem(int position) {
        return this.roadArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SingleChoiceItem item = null;
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.single_choice_items, parent, false);
            item = new SingleChoiceItem(convertView);
            convertView.setTag(item);
        } else {
            item = (SingleChoiceItem) convertView.getTag();
        }

        SingleChoiceItem tmpRoad = this.roadArrayList.get(position);

        boolean checked = tmpRoad.isSelected;
        if (checked) {
            item.layout.setBackgroundColor(Color.BLUE);
        } else {
            item.layout.setBackgroundColor(Color.BLACK);
        }

        StringBuilder details = new StringBuilder();
        details.append(tmpRoad.getRoad().beginStation);
        if (tmpRoad.getRoad().endStation.trim().length() > 0) {
            details.append(" - " + tmpRoad.getRoad().endStation);
        }

        details.append(String.format("\n%s", filterDirect(tmpRoad.getRoad().direct)));

        if (!tmpRoad.getRoad().branch.equalsIgnoreCase("0")) {
            details.append(String.format("-支線:%s", tmpRoad.getRoad().branch));
        }

        item.singleItemIdTextView.setText(details);

        return convertView;
    }

    public int getCurrentPosition() {
        return this.currentPosition;
    }

    public void setPosition(int position) {
        this.currentPosition = position;
        if (!roadArrayList.get(position).isSelected()) {
            roadArrayList.get(position).setSelected(true);
            for (int i = 0; i < roadArrayList.size(); i++) {
                if (i != position) {
                    roadArrayList.get(i).setSelected(false);
                }
            }
        }

        notifyDataSetChanged();
    }

    private String filterDirect(int direct) {
        switch (direct) {
            case 1:
                return mContext.getString(R.string.direct_1);
            case 2:
                return mContext.getString(R.string.direct_2);
            default:
                return mContext.getString(R.string.direct_0);
        }
    }

    class SingleChoiceItem {
        private TextView singleItemIdTextView;
        private CheckableRelativeLayout layout;
        private InsertCheckBox checkBox;

        private Road road;
        private boolean isSelected = false;

        public SingleChoiceItem(Road r) {
            this.road = r;
        }

        public SingleChoiceItem(View convertView) {
            layout = (CheckableRelativeLayout) convertView.findViewById(R.id.cbRL);
            singleItemIdTextView = (TextView) convertView.findViewById(R.id.singleitemId);
            checkBox = (InsertCheckBox) convertView.findViewById(R.id.singleItemCheckBox);
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
