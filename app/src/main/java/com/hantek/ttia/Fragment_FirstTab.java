package com.hantek.ttia;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hantek.ttia.view.StatusLayout;

public class Fragment_FirstTab extends Fragment {
    private static final String TAG = Fragment_FirstTab.class.getName();

    private StatusLayout status;

    public Fragment_FirstTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Fragment newFragment = new FragmentMain();
            FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();
            ft.add(R.id.layoutplace, newFragment).commit();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_first_night, container, false);
        status = (StatusLayout) view.findViewById(R.id.statusView);
        return view;
    }

    public void setCarID(int id) {
        status.setCarID(" ID: " + Integer.valueOf(id).toString()+"-"+ SystemPara.getInstance().getVersion_code());
    }

    public void setAdvert(String advert) {
        if (status != null)
            status.setAdvert(advert);
    }

    public void setTime(String time) {
        if (status != null)
            status.setTime(time);
    }

    public void setGps(int gps) {
        if (status != null)
            status.setGPS(gps);
    }

    public void setAcc(int acc) {
        if (status != null)
            status.setAcc(acc);
    }

    public void setMode(int mode) {
        if (status != null)
            status.setMode(mode);
    }

    public void setSignal(int siganl) {
        if (status != null)
            status.setSignalStrengths(siganl);
    }

    public void setComm1(int type) {
        if (status != null)
            status.setComm1(type);
    }

    public void setComm2(int type) {
        if (status != null)
            status.setComm2(type);
    }

    public void setDi1(int type) {
        if (status != null)
            status.setDi1(type);
    }

    public void setDi2(int type) {
        if (status != null)
            status.setDi2(type);
    }

    public void setttyUSB1(int value) {
        if (status != null)
            status.setTtyUSB1IV(value);
    }

    public void setttyUSB2(int value) {
        if (status != null)
            status.setTtyUSB2IV(value);
    }

    public void setttyUSB3(int value) {
        if (status != null)
            status.setTtyUSB3IV(value);
    }

    public void switchMain() {
        try {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            Fragment f = new FragmentMain();
            ft.replace(R.id.layoutplace, f);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void switchSelectRoad() {
        try {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            Fragment f = new FragmentSelectRoad();
            ft.replace(R.id.layoutplace, f);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchOnRoad() {
        try {
            FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();
            FragmentOnRoad f = new FragmentOnRoad();
            ft.replace(R.id.layoutplace, f);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void switchModifyCarStatus() {
        try {
            FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();
            Fragment f = new FragmentModifyBusStatus();
            ft.replace(R.id.layoutplace, f);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void switchModifyDutyStatus() {
        try {
            FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();
            Fragment f = new FragmentModifyDutyStatus();
            ft.replace(R.id.layoutplace, f);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
