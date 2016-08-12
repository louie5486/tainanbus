package com.hantek.ttia;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hantek.ttia.module.roadutils.Station;

import java.util.ArrayList;

public class StationListAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private Context mContext;
    private ArrayList<Station> mList;
    private int position = -1;

    public StationListAdapter(Context context) {
        mContext = context;
        this.lInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(ArrayList<Station> list) {
        ArrayList<Station> al = new ArrayList<>();
        for (Station s : list) {
            if (s.type == 1)
                al.add(s);
        }
        mList = al;
    }

    public int setPosition(int id) {
        if (id != -1) {
            for (int i = 0; i < this.mList.size(); i++) {
                if (this.mList.get(i).id == id) {
                    this.position = i;
                    break;
                }
            }
        } else {
            this.position = id;
        }
        notifyDataSetChanged();
        return this.position;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Station getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StationItem item = null;
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.item_station, parent, false);
            item = new StationItem(convertView);
            convertView.setTag(item);
        } else {
            item = (StationItem) convertView.getTag();
        }

        Station station = mList.get(position);
        item.station = station;
        // 顯示 index
        item.idTxv.setText(String.valueOf(position + 1)); // station.id));
        item.nameTxv.setText(station.zhName);

        if (this.position != -1 && this.position == position) {
            item.linearLayout.setBackgroundColor(Color.GREEN);
        } else {
            item.linearLayout.setBackgroundColor(Color.BLACK);
        }

        return convertView;
    }

    class StationItem {
        private Station station;
        private LinearLayout linearLayout;
        private TextView idTxv;
        private TextView nameTxv;

        public StationItem(View rootView) {
            station = new Station();
            linearLayout = (LinearLayout) rootView.findViewById(R.id.stationLinearLayout);
            idTxv = (TextView) rootView.findViewById(R.id.sIDView);
            nameTxv = (TextView) rootView.findViewById(R.id.sNameView);
        }
    }
}
