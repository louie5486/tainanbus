package com.hantek.ttia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hantek.ttia.module.DateTime;
import com.hantek.ttia.module.DioController;
import com.hantek.ttia.module.SharedPreferencesHelper;
import com.hantek.ttia.protocol.a1a4.BackendMsgID;
import com.hantek.ttia.protocol.d1.Animation;
import com.hantek.ttia.protocol.d3.ETMMsgID;
import com.hantek.ttia.protocol.d3.Encryption;
import com.hantek.ttia.protocol.e2.DCRMsgID;
import com.hantek.ttia.view.ScrollViewExt;
import com.hantek.ttia.view.ScrollViewListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentTestMode extends Fragment implements Runnable, ScrollViewListener {
    private static final String TAG = FragmentTestMode.class.getName();

    public static final String TEST = "TEST";
    public static final String TEST_A1A4 = "TEST_A1A4";
    public static final String TEST_D1 = "TEST_D1";
    public static final String TEST_D3 = "TEST_D3";
    public static final String TEST_E2 = "TEST_E2";
    public static final String TEST_GPS = "TEST_GPS";

    public static final int HDL_DEBUG = 0;
    public static final int HDL_A1A4 = 1;
    public static final int HDL_D1 = 2;
    public static final int HDL_ETM_D3 = 3;
    public static final int HDL_DCR_E2 = 4;
    public static final int HDL_GPS = 5;
    public static final int HDL_Staion = 6;


    // ui
    public static Handler mHandler;
    private ScrollViewExt debugScrollView;
    private TextView debugTextView;
    private ListView cmdListView;
    private RadioButton radioA1;
    private RadioGroup group;

    private List<String> list = new ArrayList<String>();
    private Button clearButton;

    private Object lockObj = new Object();
    private LinkedList<String> alertStringList = new LinkedList<String>();
    private DateTime dd_util = new DateTime();
    private boolean onBottom = true;

    public FragmentTestMode() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        Log.d(TAG, "OnCreateView");

        debugScrollView = (ScrollViewExt) rootView.findViewById(R.id.debugScrollView);
        debugTextView = (TextView) rootView.findViewById(R.id.debugTextView);
        cmdListView = (ListView) rootView.findViewById(R.id.cmdListView);
        radioA1 = (RadioButton) rootView.findViewById(R.id.radioA1A4);
        group = (RadioGroup) rootView.findViewById(R.id.radioGroup1);
        clearButton = (Button) rootView.findViewById(R.id.clearButton);

        debugScrollView.setScrollViewListener(this);

        // testItem();
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioA1A4:
                        testA1();
                        break;
                    case R.id.radioD1:
                        testD1();
                        break;
                    case R.id.radioD3:
                        testD3();
                        break;
                    case R.id.radioE2:
                        testE2();
                        break;
                    case R.id.radioGPS:
                        testOther();
                        break;
                }

                refreshList();
            }
        });

        clearButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                synchronized (lockObj) {
                    alertStringList.clear();
                }
                onBottom = true;
                debugTextView.setText("");
            }

        });

        radioA1.setChecked(true);
        testA1();
        refreshList();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HDL_DEBUG:
                        addMessage(msg.obj.toString());
                        break;
                    case HDL_A1A4:
                        if (group.getCheckedRadioButtonId() == R.id.radioA1A4) {
                            addMessage(msg.obj.toString());
                        }
                        break;
                    case HDL_D1:
                        if (group.getCheckedRadioButtonId() == R.id.radioD1) {
                            addMessage(msg.obj.toString());
                        }
                        break;
                    case HDL_ETM_D3:
                        if (group.getCheckedRadioButtonId() == R.id.radioD3) {
                            addMessage(msg.obj.toString());
                        }
                        break;
                    case HDL_DCR_E2:
                        if (group.getCheckedRadioButtonId() == R.id.radioE2) {
                            addMessage(msg.obj.toString());
                        }
                        break;
                    case HDL_GPS:
                        if (group.getCheckedRadioButtonId() == R.id.radioGPS) {
                            addMessage(msg.obj.toString());
                        }
                        break;
                    case HDL_Staion:
                        addMessage(msg.obj.toString());
                        break;
                }
            }
        };

        return rootView;
    }

    private void refreshList() {
        String[] data = new String[list.size()];
        list.toArray(data);
        ListAdapter adapter = new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_list_item_1, data);
        cmdListView.setAdapter(adapter);
        cmdListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getId() == android.R.id.text1) {
                    TextView textView = (TextView) view;
                    splitCmd(textView.getText().toString());
                }
            }

        });
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        mHandler.removeCallbacks(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        mHandler.postDelayed(this, 100);
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void run() {
        try {
            updataDebugView();
            mHandler.postDelayed(this, 500);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    @Override
    public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
        // We take the last son in the scrollview
        View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
        int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

        // Log.d(TAG, "onScrollChanged view:" + view.getBottom() + " height:" + scrollView.getHeight() + " diff:" + diff);
        // if diff is zero, then the bottom has been reached
        if (diff == 0 || diff < 0) {
            // do stuff
            onBottom = true;
        } else {
            onBottom = false;
        }
    }

    private void updataDebugView() {
        try {
            // // append the new string
            // // mTextView.setText(mTextView.getText(),TextView.BufferType.NORMAL);
            // // mTextView.append(msg + "\n");
            //
            // // find the amount we need to scroll. This works by
            // // asking the TextView's internal layout for the position
            // // of the final line and then subtracting the TextView's height
            // final int scrollAmount = debugTextView.getLayout().getLineTop(debugTextView.getLineCount()) - debugTextView.getHeight();
            //
            // // if there is no need to scroll, scrollAmount will be <=0
            // if (scrollAmount > 0) {
            // debugScrollView.scrollTo(0, scrollAmount);
            // } else {
            // debugScrollView.scrollTo(0, 0);
            // }

            View view = (View) debugScrollView.getChildAt(debugScrollView.getChildCount() - 1);
            int diff = (view.getBottom() - (debugScrollView.getHeight()));// + debugScrollView.getScrollY()));
            if (this.onBottom) {
                // debugScrollView.fullScroll(android.view.View.FOCUS_DOWN); // if not on focus, no working.
                debugScrollView.scrollTo(0, diff);
                // View view1 = (View) debugScrollView.getChildAt(debugScrollView.getChildCount() - 1);
                // int diff1 = (view1.getBottom() - (debugScrollView.getHeight() + debugScrollView.getScrollY()));
                // Log.d(TAG, "V:" + view.getBottom() + " V1:" + view1.getBottom());
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private void testA1() {
        list.clear();
        list.add("====================");
        list.add("A1A4,0x00,註冊");
        list.add("A1A4,0x02,路線修改");
        list.add("A1A4,0x04,定時回報");
        list.add("A1A4,0x07,提示訊息確認");
        list.add("A1A4,0x08,事件回報");
        list.add("A1A4,0x0A,關機訊息");
        list.add("A1A4,0xF0,障礙回報");
        list.add("A1A4,0xF2,OD回報");
        list.add("====================");
    }

    private void testD3() {
        list.clear();
        list.add("====================");
        list.add("D3,0x01,身分認證要求");
        list.add("D3,0x04,開始請求GPS資訊收到");
        list.add("D3,0x05,送出GPS資訊");
        list.add("D3,0x08,終止請求GPS資訊收到");
        list.add("D3,0x09,系統資料上傳要求");
        list.add("D3,0x0B,上傳系統資料收到");
        list.add("D3,0x0C,系統資料下載要求");
        list.add("D3,0x0E,下載系統資料");
        list.add("D3,0xA0,開始OD資料請求");
        list.add("D3,0xA2,停止OD資料請求");
        list.add("D3,0xA5,OD資料收到");
        list.add("====================");
        list.add("D3,0x00,Result:0");
        list.add("D3,0x255,Result:255");
        list.add("====================");
    }

    private void testE2() {
        list.clear();
        list.add("====================");
        list.add("E2,A1,紀錄器基本資訊");
        list.add("E2,A2,最近2160h車輛行駛資料");
        list.add("E2,A3,瞬時速度資料");
        list.add("E2,A4,停車前5分鐘內每秒鐘最高速度");
        list.add("E2,A5,疲勞駕駛紀錄");
        list.add("E2,A6,讀取即時速度");
        list.add("E2,A7,駕駛員休息紀錄");
        list.add("E2,A8,GPS位置、時間、方位資訊");
        list.add("====================");
    }

    private void testD1() {
        list.clear();
        list.add("====================");
        list.add("D1,1,進入動畫控制");
        list.add("D1,2,效果控制播放");
        list.add("D1,3,文宣命令");

        list.add("D1,55P,顯示暫停");
        list.add("D1,55O,顯示繼續");
        list.add("D1,55Q,強制結束");
        list.add("====================");
    }

    private void testOther() {
        list.clear();
        list.add("====================");
        list.add("GPS,START,開始");
        list.add("GPS,MARK,標記");
        list.add("GPS,STOP,停止");
        list.add("====================");
        list.add("GPS,R_START,開始");
        list.add("GPS,R_STOP,停止");
        list.add("====================");
        list.add("DB,backup");
//        list.add("DB,100");
//        list.add("DB,200");
//        list.add("DB,300");
//        list.add("DB,400");
//        list.add("DB,500");
//        list.add("DB,1000");
//        list.add("DB,1500");
//        list.add("DB,2500");
        list.add("====================");
        list.add("DB,1,全部不傳(Ack=1)");
        list.add("DB,0,全部補傳(Ack=0)");
        list.add("====================");
        list.add("IMEI,?");
        list.add("IMEI,ON");
        list.add("IMEI,OFF");
        list.add("====================");
        list.add("Cradle,?");
        list.add("Tablet,?");
        list.add("SHUTDOWN,WARNING");
    }

    private void splitCmd(String data) {
        String[] tmp = data.split(",");
        if (tmp[0].equalsIgnoreCase("A1A4")) {
            int cmd = Integer.parseInt(tmp[1].replace("0x", ""), 16);
            Intent intent = new Intent();
            intent.setAction(TEST_A1A4);
            intent.putExtra("cmd", cmd);

            if (cmd == BackendMsgID.RegisterRequest.getValue()) {
                TestDialog.getInstance().showRegisterRequest(getActivity(), tmp[2]);
            } else if (cmd == BackendMsgID.RoadModification.getValue()) {
                TestDialog.getInstance().showRoadModification(getActivity(), tmp[2]);
            } else if (cmd == BackendMsgID.EventReport.getValue()) {
                TestDialog.getInstance().showEventReport(getActivity(), tmp[2]);
            } else if (cmd == BackendMsgID.Shutdown.getValue()) {
                TestDialog.getInstance().showShutdown(getActivity(), tmp[2]);
            } else if (cmd == BackendMsgID.DeviceAlarm.getValue()) {
                TestDialog.getInstance().showDeviceAlert(getActivity(), tmp[2]);
            } else if (cmd == BackendMsgID.ReportOD.getValue()) {
                TestDialog.getInstance().showReportOD(getActivity(), tmp[2]);
            } else {
                getActivity().sendBroadcast(intent);
            }
        } else if (tmp[0].equalsIgnoreCase("D1")) {
            if (tmp[1].trim().equalsIgnoreCase("1")) {
                TestDialog.getInstance().showD1_1(getActivity(), tmp[2]);
            } else if (tmp[1].trim().equalsIgnoreCase("2")) {
                TestDialog.getInstance().showD1_2(getActivity(), tmp[2]);
            } else if (tmp[1].trim().equalsIgnoreCase("3")) {
                Test.LEDText = "文宣測試Taipei花博會";
                Test.LEDanimation = Animation.Bottom;
                Test.cc1 = '1';
                Test.cc2 = '0';
                Test.cc3 = '1';
                Intent intent = new Intent();
                intent.setAction(TEST_D1);
                intent.putExtra("cmd", tmp[1].trim());
                getActivity().sendBroadcast(intent);
            } else {
                Intent intent = new Intent();
                intent.setAction(TEST_D1);
                intent.putExtra("cmd", tmp[1].trim());
                getActivity().sendBroadcast(intent);
            }
        } else if (tmp[0].equalsIgnoreCase("D3")) {
            int cmd = Integer.parseInt(tmp[1].replace("0x", ""), 16);
            if (cmd == ETMMsgID.AuthenRequest.getValue()) {
                TestDialog.getInstance().showAuthenRequest(getActivity(), tmp[2]);
            } else if (cmd == ETMMsgID.UploadDataReq.getValue()) {
                TestDialog.getInstance().showUploadDataReq(getActivity(), tmp[2]);
            } else if (cmd == ETMMsgID.StartODDataReq.getValue()) {
                TestDialog.getInstance().showStartODDataReq(getActivity(), tmp[2]);
            } else if (cmd == ETMMsgID.DownloadDataReq.getValue()) {
                TestDialog.getInstance().showDownloadDataReq(getActivity(), tmp[2]);
            } else if (cmd == ETMMsgID.DownloadData.getValue()) {
                TestDialog.getInstance().showDownloadData(getActivity(), tmp[2]);
            } else {
                Intent intent = new Intent();
                intent.setAction(TEST_D3);
                intent.putExtra("cmd", cmd);
                getActivity().sendBroadcast(intent);
            }
        } else if (tmp[0].equalsIgnoreCase("E2")) {
            int cmd = Integer.parseInt(tmp[1].replace("0x", ""), 16);
            if (cmd == DCRMsgID.ReadIntSpeed.getValue()) {
                TestDialog.getInstance().showReadIntSpeed(getActivity(), tmp[2]);
            } else {
                Intent intent = new Intent();
                intent.setAction(TEST_E2);
                intent.putExtra("cmd", cmd);
                getActivity().sendBroadcast(intent);
            }
        } else if (tmp[0].equalsIgnoreCase("GPS") || tmp[0].equalsIgnoreCase("DB")) {
            Intent intent = new Intent();
            intent.setAction(TEST_GPS);
            intent.putExtra("cmd", tmp[1]);
            getActivity().sendBroadcast(intent);
        } else if (tmp[0].equalsIgnoreCase("IMEI")) {
            if (tmp[1].equalsIgnoreCase("ON")) {
                SharedPreferencesHelper.getInstance(getActivity()).setSystemIMEI(true);
            } else if (tmp[1].equalsIgnoreCase("OFF")) {
                TestDialog.getInstance().showIMEI(getActivity(), tmp[1]);
            } else {
                String tmpIMEI = SystemPara.getInstance().getIMEI();
                boolean useInternal = SharedPreferencesHelper.getInstance(getActivity()).getSystemIMEI();
                if (!useInternal) {
                    tmpIMEI = SharedPreferencesHelper.getInstance(getActivity()).getIMEI();
                }
                Toast.makeText(getActivity(), "目前IMEI:" + tmpIMEI + (!useInternal ? ",自訂" : ",系統"), Toast.LENGTH_LONG).show();
            }
        } else if (tmp[0].equalsIgnoreCase("SHUTDOWN")) {
            DioController.getInstance().shutdown(getActivity());
        } else if (tmp[0].equalsIgnoreCase("Cradle")) {
            String ver = DioController.getInstance().getCradleMcuVersion();
            try {
                Toast.makeText(getActivity(), "Cradle Mcu Version:" + ver, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Cradle Mcu Version: error", Toast.LENGTH_LONG).show();
            }
        } else if (tmp[0].equalsIgnoreCase("Tablet")) {
            String ver = DioController.getInstance().getTabletMcuVersion();
            try {
                Toast.makeText(getActivity(), "Tablet Mcu Version:" + ver, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Tablet Mcu Version: error", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addMessage(String msg) {
        try {
            synchronized (lockObj) {
                if (alertStringList.size() >= 16) {
                    // alertStringList.removeLast();
                    alertStringList.removeFirst();
                }

                // alertStringList.addFirst("(" + dd_util.getFormatedSystemDateTime() + ")\n" + msg + "\n");
                alertStringList.addLast("(" + dd_util.getFormatedSystemDateTime() + ")\n" + msg + "\n");
            }

            // updataDebugView();
            debugTextView.setText(getAlertString());
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private String getAlertString() {
        StringBuffer buff = new StringBuffer();
        try {
            LinkedList<String> tmpList;

            synchronized (lockObj) {
                tmpList = new LinkedList<String>(alertStringList);
            }

            Iterator<String> ii = tmpList.iterator();
            while (ii.hasNext()) {
                buff.append(ii.next());
            }
        } catch (Exception x) {
        }
        return buff.toString();
    }

    static class TestDialog {
        static TestDialog dialog = new TestDialog();

        public static TestDialog getInstance() {
            return dialog;
        }

        public void showRegisterRequest(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_register_request, null);
            final RadioGroup regGroup = (RadioGroup) view.findViewById(R.id.radioGroupReg);
            final RadioGroup driverIDTypeGroup = (RadioGroup) view.findViewById(R.id.radioGroupDriverIDType);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_A1A4);
                    intent.putExtra("cmd", 0x00);

                    switch (regGroup.getCheckedRadioButtonId()) {
                        case R.id.radioReg1:
                            Test.regType = 1;
                            break;
                        default:
                            Test.regType = 0;
                            break;
                    }

                    switch (driverIDTypeGroup.getCheckedRadioButtonId()) {
                        case R.id.radioDriverIDType1:
                            Test.driverIDType = 1;
                            break;
                        case R.id.radioDriverIDType2:
                            Test.driverIDType = 2;
                            break;
                        default:
                            Test.driverIDType = 0;
                            break;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showRoadModification(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_road_modification, null);
            final RadioGroup directGroup = (RadioGroup) view.findViewById(R.id.radioGroupRoadDirect);
            final RadioGroup typeGroup = (RadioGroup) view.findViewById(R.id.radioGroupRoadType);
            final EditText routeID = (EditText) view.findViewById(R.id.editTextRoadID);
            routeID.setText(String.valueOf(Test.id));

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_A1A4);
                    intent.putExtra("cmd", 0x02);

                    switch (directGroup.getCheckedRadioButtonId()) {
                        case R.id.radioRoadDirect1:
                            Test.direct = 1;
                            break;
                        case R.id.radioRoadDirect2:
                            Test.direct = 2;
                            break;
                        default:
                            Test.direct = 0;
                            break;
                    }

                    switch (typeGroup.getCheckedRadioButtonId()) {
                        case R.id.radioRoadType1:
                            Test.branch = "A";
                            break;
                        default:
                            Test.branch = "0";
                            break;
                    }

                    if (routeID.getText().toString().length() == 0)
                        Test.id = 65535;
                    else {
                        int tmp = Integer.parseInt(routeID.getText().toString());
                        if (tmp > 65535) {
                            Test.id = 65535;
                        } else
                            Test.id = tmp;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showEventReport(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_event_report, null);
            final RadioGroup radioGroupEvent = (RadioGroup) view.findViewById(R.id.radioGroupEvent);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_A1A4);
                    intent.putExtra("cmd", 0x08);

                    switch (radioGroupEvent.getCheckedRadioButtonId()) {
                        case R.id.radio0x0001:
                            intent.putExtra("type", 0x0001);
                            break;
                        case R.id.radio0x0002:
                            intent.putExtra("type", 0x0002);
                            break;
                        case R.id.radio0x0004:
                            intent.putExtra("type", 0x0004);
                            break;
                        case R.id.radio0x0008:
                            intent.putExtra("type", 0x0008);
                            break;
                        case R.id.radio0x0010:
                            intent.putExtra("type", 0x0010);
                            break;
                        case R.id.radio0x0020:
                            intent.putExtra("type", 0x0020);
                            break;
                        case R.id.radio0x0040:
                            intent.putExtra("type", 0x0040);
                            break;
                        case R.id.radio0x0080:
                            intent.putExtra("type", 0x0080);
                            break;
                        case R.id.radio0x0100:
                            intent.putExtra("type", 0x0100);
                            break;
                        case R.id.radio0x8000:
                            intent.putExtra("type", 0x8000);
                            break;
                        default:
                            return;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showShutdown(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_shutdown, null);
            final EditText PSDReconnect = (EditText) view.findViewById(R.id.editTextPSDReconnect);
            final EditText PacketRatio = (EditText) view.findViewById(R.id.editTextPacketRatio);
            final EditText GPSRatio = (EditText) view.findViewById(R.id.editTextGPSRatio);
            PSDReconnect.setText(String.valueOf(Test.PSDReconnect));
            PacketRatio.setText(String.valueOf(Test.PacketRatio));
            GPSRatio.setText(String.valueOf(Test.GPSRatio));

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_A1A4);
                    intent.putExtra("cmd", 0x0A);

                    if (PSDReconnect.getText().toString().length() == 0)
                        Test.PSDReconnect = 65535;
                    else {
                        int tmp = Integer.parseInt(PSDReconnect.getText().toString());
                        if (tmp > 65535) {
                            Test.PSDReconnect = 65535;
                        } else
                            Test.PSDReconnect = tmp;
                    }

                    if (PacketRatio.getText().toString().length() == 0)
                        Test.PacketRatio = 65535;
                    else {
                        int tmp = Integer.parseInt(PacketRatio.getText().toString());
                        if (tmp > 100) {
                            Test.PacketRatio = 100;
                        } else
                            Test.PacketRatio = tmp;
                    }

                    if (GPSRatio.getText().toString().length() == 0)
                        Test.GPSRatio = 100;
                    else {
                        int tmp = Integer.parseInt(GPSRatio.getText().toString());
                        if (tmp > 100) {
                            Test.GPSRatio = 100;
                        } else
                            Test.GPSRatio = tmp;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showDeviceAlert(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_device_alert, null);

            final RadioGroup groupModule = (RadioGroup) view.findViewById(R.id.radioGroupModule);
            final RadioGroup groupCode = (RadioGroup) view.findViewById(R.id.radioGroupCode);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_A1A4);
                    intent.putExtra("cmd", 0xF0);

                    switch (groupModule.getCheckedRadioButtonId()) {
                        case R.id.radioM1:
                            Test.module = 0x01;
                            break;
                        case R.id.radioM2:
                            Test.module = 0x02;
                            break;
                        case R.id.radioM3:
                            Test.module = 0x03;
                            break;
                        case R.id.radioM4:
                            Test.module = 0x04;
                            break;
                    }

                    switch (groupCode.getCheckedRadioButtonId()) {
                        case R.id.radioCode0:
                            Test.code = 0x00;
                            break;
                        case R.id.radioCode1:
                            Test.code = 0x01;
                            break;
                        case R.id.radioCode2:
                            Test.code = 0x02;
                            break;
                    }
                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showReportOD(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_report_od, null);
            final RadioGroup directGroup = (RadioGroup) view.findViewById(R.id.radioGroupRoadDirect);
            final RadioGroup typeGroup = (RadioGroup) view.findViewById(R.id.radioGroupRoadType);
            final EditText routeID = (EditText) view.findViewById(R.id.editTextRoadID);
            routeID.setText(String.valueOf(Test.id));

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_A1A4);
                    intent.putExtra("cmd", 0xF2);

                    switch (directGroup.getCheckedRadioButtonId()) {
                        case R.id.radioRoadDirect1:
                            Test.direct = 1;
                            break;
                        case R.id.radioRoadDirect2:
                            Test.direct = 2;
                            break;
                        default:
                            Test.direct = 0;
                            break;
                    }

                    switch (typeGroup.getCheckedRadioButtonId()) {
                        case R.id.radioRoadType1:
                            Test.branch = "A";
                            break;
                        default:
                            Test.branch = "0";
                            break;
                    }

                    if (routeID.getText().toString().length() == 0)
                        Test.id = 65535;
                    else {
                        int tmp = Integer.parseInt(routeID.getText().toString());
                        if (tmp > 65535) {
                            Test.id = 65535;
                        } else
                            Test.id = tmp;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();

        }

        public void showD1_1(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_led_1, null);
            final RadioGroup ledGroup1 = (RadioGroup) view.findViewById(R.id.radioGroupLED1);
            final EditText text = (EditText) view.findViewById(R.id.editTextInfo);
            text.setText("GPS測試現在位置經度121.5468 緯度25.0796");

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_D1);
                    intent.putExtra("cmd", "1");

                    switch (ledGroup1.getCheckedRadioButtonId()) {
                        case R.id.radioLEDA:
                            Test.LEDanimation = Animation.Left;
                            break;
                        case R.id.radioLEDB:
                            Test.LEDanimation = Animation.Top;
                            break;
                        case R.id.radioLEDC:
                            Test.LEDanimation = Animation.Bottom;
                            break;
                        case R.id.radioLEDD:
                            Test.LEDanimation = Animation.Now;
                            break;
                    }

                    Test.LEDText = text.getText().toString();
                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showD1_2(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_led_2, null);
            final RadioGroup ledGroup2 = (RadioGroup) view.findViewById(R.id.radioGroupLED2);
            final EditText text = (EditText) view.findViewById(R.id.editTextInfo);
            text.setText("GPS測試現在位置經度121.5468 緯度25.0796");

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_D1);
                    intent.putExtra("cmd", "2");

                    switch (ledGroup2.getCheckedRadioButtonId()) {
                        case R.id.radioLED001:
                            Test.cc1 = '1';
                            Test.cc2 = '0';
                            Test.cc3 = '1';
                            break;
                        case R.id.radioLED002:
                            Test.cc1 = '1';
                            Test.cc2 = '0';
                            Test.cc3 = '0';
                            break;
                        case R.id.radioLED003:
                            Test.cc1 = '0';
                            Test.cc2 = 'A';
                            Test.cc3 = '0';
                            break;
                        case R.id.radioLED004:
                            Test.cc1 = '0';
                            Test.cc2 = 'A';
                            Test.cc3 = '1';
                            break;
                    }

                    Test.LEDText = text.getText().toString();
                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showAuthenRequest(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_etm_authen_request, null);
            final RadioGroup radioGroupOAT = (RadioGroup) view.findViewById(R.id.radioGroupOAT);
            final RadioGroup radioGroupOT = (RadioGroup) view.findViewById(R.id.radioGroupOT);

            final EditText editTextOAID = (EditText) view.findViewById(R.id.editTextOAID);
            final EditText editTextPW = (EditText) view.findViewById(R.id.editTextPW);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_D3);
                    intent.putExtra("cmd", 0x01);

                    switch (radioGroupOAT.getCheckedRadioButtonId()) {
                        case R.id.radioOAT0:
                            Test.oat = 0x00;
                            break;
                        case R.id.radioOAT1:
                            Test.oat = 0x01;
                            break;
                        case R.id.radioOAT2:
                            Test.oat = 0x02;
                            break;
                        case R.id.radioOAT3:
                            Test.oat = 0x03;
                            break;
                    }

                    switch (radioGroupOT.getCheckedRadioButtonId()) {
                        case R.id.radioOT0:
                            Test.ot = 0x00;
                            break;
                        case R.id.radioOT1:
                            Test.ot = 0x01;
                            break;
                        case R.id.radioOT2:
                            Test.ot = 0x02;
                            break;
                    }

                    try {
                        long id = Long.parseLong(editTextOAID.getText().toString());
                        if (id > 429496729l || id < 0) {
                            Test.oID = 99999;
                        } else {
                            Test.oID = id;
                        }
                    } catch (Exception e) {
                        Test.oID = 99999;
                    }

                    Test.pwd = editTextPW.getText().toString();

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showUploadDataReq(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_etm_upload_data_req, null);
            final RadioGroup radioGroupUDR_ID = (RadioGroup) view.findViewById(R.id.radioGroupUDR_ID);
            final RadioGroup radioGroupEnFLag = (RadioGroup) view.findViewById(R.id.radioGroupEnFLag);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_D3);
                    intent.putExtra("cmd", 0x09);

                    switch (radioGroupUDR_ID.getCheckedRadioButtonId()) {
                        case R.id.radioUDR_ID1:
                            Test.UDRid = 0x0001;
                            break;
                        case R.id.radioUDR_ID2:
                            Test.UDRid = 0x0002;
                            break;
                    }

                    switch (radioGroupEnFLag.getCheckedRadioButtonId()) {
                        case R.id.radioEN0:
                            Test.UDRencryption = Encryption.Plaintext;
                            break;
                        case R.id.radioEN1:
                            Test.UDRencryption = Encryption.Ciphertext;
                            break;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showStartODDataReq(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_etm_start_od_req, null);
            final RadioGroup radioGroupODReq = (RadioGroup) view.findViewById(R.id.radioGroupODReq);
            final RadioGroup radioGroupEnFLag = (RadioGroup) view.findViewById(R.id.radioGroupEnFLag);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_D3);
                    intent.putExtra("cmd", 0xA0);

                    switch (radioGroupODReq.getCheckedRadioButtonId()) {
                        case R.id.radioODReq_0:
                            Test.reportFlag = 0x00;
                            break;
                        case R.id.radioODReq_1:
                            Test.reportFlag = 0x01;
                            break;
                    }

                    switch (radioGroupEnFLag.getCheckedRadioButtonId()) {
                        case R.id.radioEN0:
                            Test.UDRencryption = Encryption.Plaintext;
                            break;
                        case R.id.radioEN1:
                            Test.UDRencryption = Encryption.Ciphertext;
                            break;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showDownloadDataReq(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_etm_download_data_req, null);
            final RadioGroup radioGroupDDR = (RadioGroup) view.findViewById(R.id.radioGroupDDR);
            final RadioGroup radioGroupEnFLag = (RadioGroup) view.findViewById(R.id.radioGroupEnFLag);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_D3);
                    intent.putExtra("cmd", 0x0C);

                    switch (radioGroupDDR.getCheckedRadioButtonId()) {
                        case R.id.radioDDR0101:
                            Test.downloadDataReq = 0x0101;

                            Test.dlDID = 0x0101;
                            Test.dlDLast = 0;
                            Test.dlDTotal = 2;
                            break;
                        case R.id.radioDDR0102:
                            Test.downloadDataReq = 0x0102;

                            Test.dlDID = 0x0102;
                            Test.dlDLast = 1;
                            Test.dlDTotal = 1;
                            break;
                        case R.id.radioDDR0103:
                            Test.downloadDataReq = 0x0103;

                            Test.dlDID = 0x0103;
                            Test.dlDLast = 1;
                            Test.dlDTotal = 1;
                            break;
                        case R.id.radioDDR0104:
                            Test.downloadDataReq = 0x0104;

                            Test.dlDID = 0x0104;
                            Test.dlDLast = 1;
                            Test.dlDTotal = 1;
                            break;
                    }

                    switch (radioGroupEnFLag.getCheckedRadioButtonId()) {
                        case R.id.radioEN0:
                            Test.UDRencryption = Encryption.Plaintext;
                            break;
                        case R.id.radioEN1:
                            Test.UDRencryption = Encryption.Ciphertext;
                            break;
                    }

                    Test.dlDSN = 0;
                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showDownloadData(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_etm_download_data, null);
            final RadioGroup radioGroupDLD = (RadioGroup) view.findViewById(R.id.radioGroupDLD);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_D3);
                    intent.putExtra("cmd", 0x0E);

                    switch (radioGroupDLD.getCheckedRadioButtonId()) {
                        case R.id.radioDLD0101:
                            Test.dlDID = 0x0101;
                            Test.dlDLast = 0;
                            Test.dlDTotal = 2;
                            break;
                        case R.id.radioDLD0102:
                            Test.dlDID = 0x0102;
                            Test.dlDLast = 1;
                            Test.dlDTotal = 1;
                            break;
                        case R.id.radioDLD0103:
                            Test.dlDID = 0x0103;
                            Test.dlDLast = 1;
                            Test.dlDTotal = 1;
                            break;
                        case R.id.radioDLD0104:
                            Test.dlDID = 0x0104;
                            Test.dlDLast = 1;
                            Test.dlDTotal = 1;
                            break;
                    }

                    Test.dlDSN = 0;
                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        public void showReadIntSpeed(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_dcr_a6, null);
            final EditText seconds = (EditText) view.findViewById(R.id.editTextSeconds);
            seconds.setText(String.valueOf(Test.seconds));

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle(tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(TEST_E2);
                    intent.putExtra("cmd", 0xA6);

                    if (seconds.getText().toString().length() == 0)
                        Test.seconds = 0;
                    else {
                        int tmp = Integer.parseInt(seconds.getText().toString());
                        if (tmp > 60 * 60 * 10) {
                            Test.seconds = 60 * 60 * 10;
                        } else
                            Test.seconds = tmp;
                    }

                    context.sendBroadcast(intent);
                }

            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }

        // [TEMPLETE]
        // public void showRoadModification(final Context context) {
        // LayoutInflater inflater = LayoutInflater.from(context);
        // View view = inflater.inflate(R.layout.test_register_request, null);
        // // TODO view
        //
        // Builder dialog = new AlertDialog.Builder(context);
        // dialog.setView(view);
        // dialog.setCancelable(false);
        //
        // DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
        //
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // }
        //
        // };
        //
        // DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
        //
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // Intent intent = new Intent();
        // // TODO data ACTION
        // context.sendBroadcast(intent);
        // }
        //
        // };
        //
        // dialog.setNegativeButton(R.string.cancel, negative);
        // dialog.setPositiveButton(R.string.confirm, positive);
        // dialog.create().show();
        // }

        public void showIMEI(final Context context, String tmp) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.test_imei, null);
            final EditText imeiEdTxt = (EditText) view.findViewById(R.id.editTextIMEI);
            String imei = SharedPreferencesHelper.getInstance(context).getIMEI();
            imeiEdTxt.setText(imei);

            Builder dialog = new AlertDialog.Builder(context);
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setTitle("Mode:" + tmp);

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (imeiEdTxt.length() > 15) {
                        Toast.makeText(context, "IMEI超過15位數", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferencesHelper.getInstance(context).setIMEI(imeiEdTxt.getText().toString());
                    SharedPreferencesHelper.getInstance(context).setSystemIMEI(false);
                }
            };

            dialog.setNegativeButton(R.string.cancel, negative);
            dialog.setPositiveButton(R.string.confirm, positive);
            dialog.create().show();
        }
    }
}