package com.hantek.ttia.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hantek.ttia.FragmentTestMode;
import com.hantek.ttia.SystemPara;
import com.hantek.ttia.Test;
import com.hantek.ttia.comms.PhysicalInterface;
import com.hantek.ttia.comms.SerialPortHandler;
import com.hantek.ttia.comms.UdpSocketClient;
import com.hantek.ttia.debug.SP;
import com.hantek.ttia.debug.SPListener;
import com.hantek.ttia.dl.DownloadReceiver;
import com.hantek.ttia.dl.DownloadService;
import com.hantek.ttia.module.AccTimer;
import com.hantek.ttia.module.AudioPlayer;
import com.hantek.ttia.module.DioController;
import com.hantek.ttia.module.IAccListener;
import com.hantek.ttia.module.NetworkUtils;
import com.hantek.ttia.module.Phone;
import com.hantek.ttia.module.SharedPreferencesHelper;
import com.hantek.ttia.module.Utility;
import com.hantek.ttia.module.accelerationutils.Acceleration;
import com.hantek.ttia.module.accelerationutils.AccelerationInterface;
import com.hantek.ttia.module.forwardutils.ForwardInterface;
import com.hantek.ttia.module.forwardutils.ForwardManager;
import com.hantek.ttia.module.forwardutils.ForwardMessage;
import com.hantek.ttia.module.gpsutils.GpsReceiver;
import com.hantek.ttia.module.handshake.ResponseManager;
import com.hantek.ttia.module.ledutils.LEDPlayer;
import com.hantek.ttia.module.polygonutils.Polygon;
import com.hantek.ttia.module.polygonutils.PolygonInterface;
import com.hantek.ttia.module.polygonutils.Region;
import com.hantek.ttia.module.polygonutils.RegionDataFactory;
import com.hantek.ttia.module.reportutils.IReport;
import com.hantek.ttia.module.reportutils.RegularTransfer;
import com.hantek.ttia.module.roadutils.GpsContent;
import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadDataFactory;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.module.roadutils.Station;
import com.hantek.ttia.module.roadutils.StationInterface;
import com.hantek.ttia.module.sqliteutils.DatabaseHelper;
import com.hantek.ttia.module.sqliteutils.PacketEntity;
import com.hantek.ttia.protocol.a1a4.BackendController;
import com.hantek.ttia.protocol.a1a4.BackendListener;
import com.hantek.ttia.protocol.a1a4.BackendMsgID;
import com.hantek.ttia.protocol.a1a4.BusStatus;
import com.hantek.ttia.protocol.a1a4.DeviceAlarm;
import com.hantek.ttia.protocol.a1a4.DeviceModule;
import com.hantek.ttia.protocol.a1a4.DutyStatus;
import com.hantek.ttia.protocol.a1a4.EventCode;
import com.hantek.ttia.protocol.a1a4.EventReport0x0001;
import com.hantek.ttia.protocol.a1a4.EventReport0x0002;
import com.hantek.ttia.protocol.a1a4.EventReport0x0004;
import com.hantek.ttia.protocol.a1a4.EventReport0x0008;
import com.hantek.ttia.protocol.a1a4.EventReport0x0010;
import com.hantek.ttia.protocol.a1a4.EventReport0x0020;
import com.hantek.ttia.protocol.a1a4.EventReport0x0040;
import com.hantek.ttia.protocol.a1a4.EventReport0x0080;
import com.hantek.ttia.protocol.a1a4.EventReport0x0100;
import com.hantek.ttia.protocol.a1a4.EventReport0x4000;
import com.hantek.ttia.protocol.a1a4.EventReport0x8000;
import com.hantek.ttia.protocol.a1a4.EventReportBase;
import com.hantek.ttia.protocol.a1a4.FileStruct;
import com.hantek.ttia.protocol.a1a4.GpsStruct;
import com.hantek.ttia.protocol.a1a4.Header;
import com.hantek.ttia.protocol.a1a4.MonitorStructType1;
import com.hantek.ttia.protocol.a1a4.MonitorStructType2;
import com.hantek.ttia.protocol.a1a4.NotifyMessage;
import com.hantek.ttia.protocol.a1a4.ODReport;
import com.hantek.ttia.protocol.a1a4.RegisterRequest;
import com.hantek.ttia.protocol.a1a4.RegisterResponse;
import com.hantek.ttia.protocol.a1a4.RegularReport;
import com.hantek.ttia.protocol.a1a4.RoadModification;
import com.hantek.ttia.protocol.a1a4.Shutdown;
import com.hantek.ttia.protocol.a1a4.WarningCode;
import com.hantek.ttia.protocol.d1.Animation;
import com.hantek.ttia.protocol.d1.LED;
import com.hantek.ttia.protocol.d1.LEDListener;
import com.hantek.ttia.protocol.d3.AuthenResult;
import com.hantek.ttia.protocol.d3.DataDownloadReady;
import com.hantek.ttia.protocol.d3.DownloadDataAck;
import com.hantek.ttia.protocol.d3.ETM;
import com.hantek.ttia.protocol.d3.ETMListener;
import com.hantek.ttia.protocol.d3.ETMMsgID;
import com.hantek.ttia.protocol.d3.GpsInfo;
import com.hantek.ttia.protocol.d3.ODData;
import com.hantek.ttia.protocol.d3.SendGpsInfoAck;
import com.hantek.ttia.protocol.d3.StartODDataAck;
import com.hantek.ttia.protocol.d3.StartRequestGpsInfo;
import com.hantek.ttia.protocol.d3.StopODDataAck;
import com.hantek.ttia.protocol.d3.StopRequestGpsInfo;
import com.hantek.ttia.protocol.d3.UploadData;
import com.hantek.ttia.protocol.e2.BasicData;
import com.hantek.ttia.protocol.e2.DCR;
import com.hantek.ttia.protocol.e2.DCRListener;
import com.hantek.ttia.protocol.e2.DCRMsgID;
import com.hantek.ttia.protocol.e2.DriverRest;
import com.hantek.ttia.protocol.e2.DrivingData;
import com.hantek.ttia.protocol.e2.HighSpeed;
import com.hantek.ttia.protocol.e2.InstantSpeed;
import com.hantek.ttia.protocol.e2.ReadIntSpeed;
import com.hantek.ttia.protocol.e2.ReportGps;
import com.hantek.ttia.protocol.e2.SpeedData;
import com.hantek.ttia.protocol.e2.TiredDriving;
import com.hantek.ttia.protocol.hantek.HantekController;
import com.hantek.ttia.protocol.hantek.HantekListener;
import com.hantek.ttia.protocol.hantek.UploadConfirm;
import com.hantek.ttia.protocol.hantek.UploadLog;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import component.LogInterface;
import component.LogManager;
import component.LogUploader;

import static com.hantek.ttia.Test.branch;

public class SystemService extends Service implements
        IService,
        Runnable,
        HantekListener,
        BackendListener,
        LEDListener,
        DCRListener,
        ETMListener,
        StationInterface,
        PolygonInterface,
        ForwardInterface,
        AccelerationInterface,
        SPListener,
        IReport,
        IAccListener,
        LogInterface {
    private static final String TAG = SystemService.class.getName();

    // 第一通訊
    // 華夏正式平台
//    private static final String MainServerAddress = "210.241.103.178,4215";
    private static final String MainServerAddress = "210.241.103.178,54215";//2016/08/08 v23變更位置(測試平台)
//    private static final String MainServerAddress = "210.241.103.178,4215";//2016/08/26 v27變更位置(正式平台)

    // 開發測試
    // private static final String MainServerAddress = "61.222.88.242,10129";
    // 華夏測試
    // private static final String MainServerAddress = "60.251.61.189,3215";

    // 第二通訊
    // 漢名測試
    private static final String SubServerAddress = "60.199.131.66,10130";   //2016/09/09 v29變更位置(漢名新VM)
    // 開發測試
//    private static final String SubServerAddress = "61.222.88.242,10130";

    private static final String DCR_Options = "/dev/ttyUSB1,115200";
    private static final String ETM_Options = "/dev/ttyUSB2,115200";
    private static final String LED_Options = "/dev/ttyUSB3,9600";

    private static boolean TTIA_TEST_MODE = false;
    private List<Integer> processList = new ArrayList<Integer>();

    // 通訊介面
    private PhysicalInterface ttiaComm = null;
    private SerialPortHandler dcrSp = null;
    private SerialPortHandler etmSp = null;
    private SerialPortHandler ledSp = null;
    private PhysicalInterface hantekComm = null;

    private boolean sp1 = false;
    private boolean sp2 = false;
    private boolean sp3 = false;

    // check event time
    private ServiceReceiver mReceiver = null;
    private Calendar lastCheckEvent0x0001 = Calendar.getInstance();
    private Calendar lastCheckEvent0x0002 = Calendar.getInstance();

    private Calendar lastCheckEvent0x0008 = Calendar.getInstance();
    private Calendar lastCheckEvent0x0040 = Calendar.getInstance();

    private Thread thread = null;
    private boolean isStart = false;

    private boolean isLogon = false;

    //冷開機
    private boolean coldReset = true;

    private boolean report0x0001;
    private boolean report0x0002;
    private boolean report0x0004;
    private boolean report0x0008;
    private boolean report0x0010;
    private boolean report0x0020; // 司機更改車輛狀態
    private boolean report0x0040;
    private boolean report0x0080; // 司機回覆
    private boolean report0x0100;
    @SuppressWarnings("unused")
    private boolean report0x0200; // 待增
    @SuppressWarnings("unused")
    private boolean report0x0400; // 待增
    @SuppressWarnings("unused")
    private boolean report0x0800; // 待增
    @SuppressWarnings("unused")
    private boolean report0x1000; // 待增
    @SuppressWarnings("unused")
    private boolean report0x2000; // 待增
    @SuppressWarnings("unused")
    private boolean report0x4000; // 待增
    private boolean report0x8000;

    // 前門
    private int frontDoorCounter;
    // 後門
    private int backDoorCounter;

    private Calendar lastCheckTimeCarAlarm = Calendar.getInstance();
    private long idleCounter = 0;
    private boolean isIdle = false; // 閒置, 怠速, 停車不熄火

    private boolean dcrAlarmSent = false;
    private Calendar lastCheckDCRTime = Calendar.getInstance();

    private boolean gpsAlarmSent = false;
    private Calendar lastCheckGpsTime = Calendar.getInstance();

    private boolean ledAlarmSent = false;
    private Calendar lastCheckLEDTime = Calendar.getInstance();

    private boolean etmAlarmSent = false;
    private Calendar lastCheckETMTime = Calendar.getInstance();

    private boolean sendETMGps = false;
    private int sendEtmGpsInterval = 0;
    private int sendEtmGpsNumOfReport = 0;

    private double lastLat;
    private double lastLon;
    private boolean lastFixed = false;

    private Calendar lastCheckEvent0x0100Time = Calendar.getInstance();
    private Calendar lastCheckEvent0x8000Time = Calendar.getInstance();
    private int outOfRegion = 0;
    private boolean notOnSchedule = false;

    private boolean logGPS = false;
    private Calendar lastLogGPSTime = Calendar.getInstance();

    private int sequence;
    private boolean playStation = false;

    private boolean isDoorOpen = false;
    private Calendar lastRecordGPS = Calendar.getInstance();

    private final IBinder mBinder = new LocalBinder();

    //20160812 固定班表暫存參數
    private int dummy_roadID;
    private int dummy_direct;
    private String dummy_branch;


    @Override
    public void onSPDataReceived(String data) {
//        obs("R: " + data + "\n", FragmentTestMode.HDL_GPS);
        try {
            //2016-03-24 GPS開啟不允許使用DEBUG避免資料錯誤
            if (GpsReceiver.getInstance().isEnable())
                return;
            GpsContent gps = GpsContent.parse(data);
            LogManager.write("test", gps.toString(), null);
            GpsReceiver.getInstance().setStatus(gps.isFixed ? "A" : "V");
            GpsReceiver.getInstance().setLon(gps.lon);
            GpsReceiver.getInstance().setLat(gps.lat);
            GpsReceiver.getInstance().setTime(gps.time);
            GpsReceiver.getInstance().setAngle(gps.getAngle());
            GpsReceiver.getInstance().setSpeed(gps.getSpeed());
            GpsReceiver.getInstance().setSatelliteNumber(gps.satelliteNumber);

//            obs("R: " + gps.toString() + "\n", FragmentTestMode.HDL_GPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLCDDataReceived(byte[] bytes) {

    }

    @Override
    public void updateReport() {
//        Log.d(TAG, "update regular report.");
        RegularTransfer.getInstance().addMonitorStructType1Array(getMonitorDataType1());
    }

    @Override
    public void report(RegularReport report) {
//        Log.d(TAG, "send regular report.");
        sendMessage(BackendMsgID.RegularReport, report, null);
    }

    @Override
    public void sendShutdown() {
        Shutdown shutdown = new Shutdown();
        shutdown.monitorData = this.getMonitorDataType2();
        shutdown.psdReconnect = SystemPara.getInstance().getPSDReconnect();
        shutdown.packetRatio = SystemPara.getInstance().getPacketRatio();
        shutdown.gpsRatio = SystemPara.getInstance().getGPSRatio();
        sendMessage(BackendMsgID.Shutdown, shutdown, null);

        LogManager.write("debug", "acc,send,.", null);
        Log.d(TAG, "acc off, shutdown.");
    }

    @Override
    public void shutdown() {
        try {
            // SharedPreferencesHelper.getInstance(this).setStatus(0);
            DioController.getInstance().shutdown(getApplication());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendUploadCompleted(String fileName, int result, int year, int month, int day) {
        try {
            Header header = getHeader(BackendMsgID.Reserved2, 3);
            UploadConfirm confirm = new UploadConfirm();
            confirm.year = year;
            confirm.month = month;
            confirm.day = day;
            confirm.type = 2;
            confirm.result = (byte) result;
            confirm.fileName = fileName;
            byte[] data = HantekController.getInstance().sendUploadConfirm(header, confirm);

            boolean online = false;
            if (NetworkUtils.isOnline(getApplication())) {
                online = true;
                this.hantekComm.send(data);
            }
            LogManager.write("comm2", "S:" + header.toString() + ",online:" + online, null);
        } catch (Exception e) {
            LogManager.write("error", "sendUploadComplete," + fileName + "," + e.toString(), null);
        }
    }

    public class LocalBinder extends Binder {
        public IService getService() {
            return SystemService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.sequence = SharedPreferencesHelper.getInstance(getApplication()).getSequence();
        SystemPara.getInstance().setInfoID(SharedPreferencesHelper.getInstance(this).getInfoID());

        int dutyStatus = SharedPreferencesHelper.getInstance(this).getDutyStatus();
        SystemPara.getInstance().setCurrentDutyStatus(DutyStatus.forValue(dutyStatus));

        int busStatus = SharedPreferencesHelper.getInstance(this).getBusStatus();
        SharedPreferencesHelper.getInstance(this).setBusStatus(busStatus);

        isConfiguration();
        Log.d(TAG, "onCreate: get sequence:" + this.sequence);

        // 未登入, 可處理的訊息清單
        processList.add(BackendMsgID.RegisterResponse.getValue());
        processList.add(BackendMsgID.ShutdownConfirm.getValue());
        processList.add(BackendMsgID.DeviceAlarmConfirm.getValue());
        processList.add(BackendMsgID.RegularReportConfirm.getValue());
        processList.add(BackendMsgID.EventReportConfirm.getValue());
        processList.add(BackendMsgID.ReportODConfirm.getValue());
        processList.add(BackendMsgID.RoadModificationConfirm.getValue());

        SystemPara.getInstance().setIMEI(Utility.getIMEI(getBaseContext()));
        SystemPara.getInstance().setIMSI(Utility.getIMSI(getBaseContext()));

        // 載入路線資料
        RoadManager.getInstance().setRoadData(RoadDataFactory.loadRoadData());
        RoadManager.getInstance().setInterface(this);

        // 特定區域多邊形
        Polygon.getInstance().setRegionData(RegionDataFactory.loadRegionData(this));
        Polygon.getInstance().setInterface(this);

        // 週邊設備開啟
        DioController.getInstance().start(getBaseContext());
        GpsReceiver.getInstance().startListener(getBaseContext());

        Phone.getInstance().startListener(getBaseContext());

        // A1A4介面
        this.ttiaComm = new UdpSocketClient();
        BackendController.getInstance().setInterface(this.ttiaComm);
        BackendController.getInstance().setListener(this);
        BackendController.getInstance().open(MainServerAddress);

        this.hantekComm = new UdpSocketClient();
        HantekController.getInstance().setInterface(this.hantekComm);
        HantekController.getInstance().setListener(this);
        HantekController.getInstance().open(SubServerAddress);

        // E2介面
        this.dcrSp = new SerialPortHandler();
        DCR.getInstance().setInterface(dcrSp);
        DCR.getInstance().setListener(this);
        sp1 = DCR.getInstance().open(DCR_Options);
        LogManager.write("debug", "DCR:" + DCR.getInstance().isOpen(), null);

        // D3介面
        this.etmSp = new SerialPortHandler();
//        ETM.getInstance().setInterface(etmSp);
//        ETM.getInstance().setListener(this);
//        sp2 = ETM.getInstance().open(ETM_Options);
        LogManager.write("debug", "ETM:" + ETM.getInstance().isOpen(), null);

        // D1介面
        SP.getInstance().setInterface(etmSp);
        SP.getInstance().setListener(this);
        sp2 = SP.getInstance().open(ETM_Options);
        LogManager.write("debug", "SP:" + SP.getInstance().isOpen(), null);

        this.ledSp = new SerialPortHandler();
        LED.getInstance().setCommInterface(ledSp);
        LED.getInstance().setListener(this); // 單向
        sp3 = LED.getInstance().open(LED_Options);
        LogManager.write("debug", "LED:" + LED.getInstance().isOpen(), null);

        LEDPlayer.getInstance().open();

        // 如果3個port都無法開啟, 表示未裝置夾, 但有可能系統未Ready.
        if (!sp1 && !sp2 && !sp3) {
            DioController.getInstance().setDevice(false);
            LogManager.write("info", "env is not 5668.", null);
        }

        ForwardManager.getInstance().setInterface(this);
        ForwardManager.getInstance().open(this);
        initReceiveBroadcast();

        Acceleration.getInstance().setInterface(this);
        AudioPlayer.getInstance().open(this);

//        DBUpdater.getInstance().open(this);

        AccTimer.getInstance().setInterfaces(this);
        AccTimer.getInstance().open(this);

        RegularTransfer.getInstance().setInterface(this);
        RegularTransfer.getInstance().open();

        LogUploader.getInstance().setInterface(this);
        LogUploader.getInstance().open(getBaseContext());

        this.isStart = true;
        thread = new Thread(this);
        thread.setName("Service");
        thread.start();
    }

    private void initReceiveBroadcast() {
        // 測試 event
        mReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FragmentTestMode.TEST_A1A4);
        filter.addAction(FragmentTestMode.TEST_D1);
        filter.addAction(FragmentTestMode.TEST_D3);
        filter.addAction(FragmentTestMode.TEST_E2);
        filter.addAction(FragmentTestMode.TEST);
        filter.addAction(FragmentTestMode.TEST_GPS);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: flags:" + flags + " startID:" + startId);
        if (intent != null) {
            Log.d(TAG, "onStartCommand: intent:" + intent.getComponent().getClassName());
        }
        return START_STICKY; // super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "OnDestroy");

        this.isStart = false;

        AudioPlayer.getInstance().close();
        ForwardManager.getInstance().close();
//        DBUpdater.getInstance().close();
        LEDPlayer.getInstance().close();
        RegularTransfer.getInstance().close();
        AccTimer.getInstance().close();

        try {
            this.thread.join(5000);
        } catch (InterruptedException e) {
            this.thread.interrupt();
        }

        BackendController.getInstance().close();
        HantekController.getInstance().close();
        DCR.getInstance().close();
        ETM.getInstance().close();
        LED.getInstance().close();
        GpsReceiver.getInstance().stopListener();
        Phone.getInstance().stopListener();

        super.onDestroy();
    }

    @Override
    public void run() {
        LogManager.write("debug", "background start.", null);
        LogManager.clear(getApplication());

        Log.i(TAG, "usedMemory: " + Debug.getNativeHeapSize() / 1048576L);

        while (this.isStart) {
            try {
                Thread.sleep(50);
                reportGpsToETM();

//                logGPSFunction();
                do0x0001Event();

                if (Utility.dateDiffNow(this.lastRecordGPS) >= 1000) {
                    this.lastRecordGPS = Calendar.getInstance();
                    try {
                        // TODO 轉速來源
                        SystemPara.getInstance().setRpmArray(0);
                        SystemPara.getInstance().setIntSpeedArray((int) (GpsReceiver.getInstance().getSpeed() * 1.852d));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (TTIA_TEST_MODE)
                    continue;

                do0x0002Event();
                do0x0004Event();
                do0x0008Event();
                do0x0010Event();
                do0x0040Event();
                do0x0100Event();
                do0x8000Event();

                checkGps();
                // checkLED();
                // checkETM();
                // checkDCR();
                // reportGpsToDCR();
                checkConnectionForShutdown();
            } catch (Exception e) {
                e.printStackTrace();
                LogManager.write("debug", "background process: " + e.toString(), null);
            }
        }

        LogManager.write("debug", "background stop.", null);
    }

    private Calendar lastCheckConnect = Calendar.getInstance();
    private boolean lastConnectStatus = true;

    /**
     * 每秒檢查, 如果連線狀態變更為上線，增加重建次數
     */
    private void checkConnectionForShutdown() {
        if (Utility.dateDiffNow(this.lastCheckConnect) >= 1000) {
            this.lastCheckConnect = Calendar.getInstance();

            // use WIFI or Mobile
            boolean tmpConnectionFlag = NetworkUtils.isOnline(this);
            if (this.lastConnectStatus != tmpConnectionFlag) {
                if (tmpConnectionFlag) {
                    SystemPara.getInstance().addReconnectCounter();
                    LogManager.write("debug", "Reconnect", null);
                } else {
                    LogManager.write("debug", "Disconnect", null);
                }
                this.lastConnectStatus = tmpConnectionFlag;
            }
        }
    }

    private void logGPSFunction() {
        if (this.logGPS) {
            try {
                if (Utility.dateDiffNow(this.lastLogGPSTime) >= 1000) {
                    this.lastLogGPSTime = Calendar.getInstance();
                    String content = String.format("%s,%s,%s,%s,%s,%s,%s", GpsReceiver.getInstance().getRawStatus(), GpsReceiver.getInstance().getSatelliteNumber(), GpsReceiver
                            .getInstance().getLongitude(), GpsReceiver.getInstance().getLatitude(), GpsReceiver.getInstance().getAngle(), GpsReceiver.getInstance().getSpeed(), GpsReceiver.getInstance().getTime());
                    obs(content + "\n", FragmentTestMode.HDL_GPS);
                    LogManager.write("GPS", content, null);
                }
            } catch (Exception e) {
                LogManager.write("debug", "logGPS," + e.toString(), null);
            }
        }
    }

    private Calendar lastSendGpsInfoToETM = Calendar.getInstance();

    private void reportGpsToETM() {
        if (this.sendETMGps) {
            if (Utility.dateDiffNow(this.lastSendGpsInfoToETM) >= this.sendEtmGpsInterval * 1000) {
                this.lastSendGpsInfoToETM = Calendar.getInstance();

                // 代表要求回報次數
                if (this.sendEtmGpsNumOfReport > 0) {
                    this.sendEtmGpsNumOfReport--;

                    if (this.sendEtmGpsNumOfReport == 0) {
                        this.sendETMGps = false;
                    }
                }

                ETM.getInstance().sendGpsInfo(etmGetGpsInfo());
            }
        }
    }

    private Calendar lastSendGpsInfoToDcr = Calendar.getInstance();

    private void reportGpsToDCR() {
        if (Utility.dateDiffNow(this.lastSendGpsInfoToDcr) >= 1000) {
            this.lastSendGpsInfoToDcr = Calendar.getInstance();

            // 定時發送
            ReportGps data = new ReportGps(GpsReceiver.getInstance().getUTCTime(), GpsReceiver.getInstance().getLongitude(), GpsReceiver.getInstance().getLatitude(), (float) GpsReceiver.getInstance()
                    .getAngle(), (int) GpsReceiver.getInstance().getSpeed(), GpsReceiver.getInstance().isFixed());
            DCR.getInstance().reportGPS(data);

            try {
                DCR.getInstance().readIntSpeedReq(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReceivedHantek(com.hantek.ttia.protocol.a1a4.Message message) {
        String debugString = null;
        int msgID = message.header.messageID;
        debugString = String.format("Receive: Hantek [ id=0x%02X seq:%s ]\n", msgID & 0xff, message.header.sequence);
        Log.d(TAG, debugString);
        LogManager.write("comm2", "R:" + message.header.toString(), null);

        int ackID = -1;
        if (msgID == BackendMsgID.RegisterResponse.getValue()) {
            ackID = BackendMsgID.RegisterRequest.getValue();
        } else if (msgID == BackendMsgID.NotifyMessage.getValue()) {
//            NotifyMessage notifyMessage = (NotifyMessage) message.payload;
            sendNotifyConfirm(message.header.sequence);
//            LogManager.write("msg", notifyMessage.toString(), null);
//            SystemPara.getInstance().addNotifyMessageQueue(message);
        } else if (msgID == BackendMsgID.EventReportConfirm.getValue()) {
            ackID = BackendMsgID.EventReport.getValue();
        } else if (msgID == BackendMsgID.DeviceAlarmConfirm.getValue()) {
            ackID = BackendMsgID.DeviceAlarm.getValue();
        } else if (msgID == BackendMsgID.RegularReportConfirm.getValue()) {
            ackID = BackendMsgID.RegularReport.getValue();
        } else if (msgID == BackendMsgID.ReportODConfirm.getValue()) {
            ackID = BackendMsgID.ReportOD.getValue();
        } else if (msgID == BackendMsgID.RoadModificationConfirm.getValue()) {
            ackID = BackendMsgID.RoadModification.getValue();
        } else if (msgID == BackendMsgID.ShutdownConfirm.getValue()) {
            ackID = BackendMsgID.Shutdown.getValue();
        } else if (msgID == BackendMsgID.Reserved1.getValue()) {
            try {
                UploadLog uploadLog = (UploadLog) message.payload;

//                LogUploader.getInstance().setInterface(this);
//                LogUploader.getInstance().open(getBaseContext());

                boolean uploadRun = false;
                if (uploadLog.type == 1) {
                    String[] requestFile = uploadLog.fileName.split("\\^");// "msgo^comm1o^test" or "";
                    for (String tmpFName : requestFile) {
                        boolean result = LogUploader.getInstance().checkFileIsExists(uploadLog.year, uploadLog.month, uploadLog.day, tmpFName);
                        sendCheckUploadLog(tmpFName, result, uploadLog.year, uploadLog.month, uploadLog.day);
                        if (result)
                            uploadRun = true;
                    }
                } else {
                    // 指定上傳日期
                    uploadRun = true;
                }

                if (uploadRun) {
                    String tmpIMEI = SystemPara.getInstance().getIMEI();
                    if (!SharedPreferencesHelper.getInstance(this).getSystemIMEI()) {
                        tmpIMEI = SharedPreferencesHelper.getInstance(this).getIMEI();
                    }
                    String processing = LogUploader.getInstance().setUploadFile(tmpIMEI, uploadLog.year, uploadLog.month, uploadLog.day, uploadLog.fileName);
                    sendUploadConfirm(tmpIMEI + "^" + processing, processing.equalsIgnoreCase("") ? 0 : 1, uploadLog.year, uploadLog.month, uploadLog.day);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUploader.getInstance().reset();
            }
        } else {
            Log.w(TAG, "receive unknown msgID: " + msgID);
            LogManager.write("debug", "receive unknown msgID: " + msgID, null);
        }

        if (ackID != -1) {
            ForwardMessage forwardMessage = new ForwardMessage(ackID, message.header.sequence, null, 0, 1);
            ForwardManager.getInstance().remove(forwardMessage);
            // DatabaseHelper.getInstance(getApplication()).updateHantekAck(ackID, message.header.sequence);
        }
    }

    private void checkScheduldata(RegisterResponse response) {
        try {
            //有班表，進行改路線動作
            if (response.schedule == 1) {
                dummy_roadID = response.routeID;
                dummy_direct = response.routeDirect;
                dummy_branch = Character.toString((char) response.routeBranch);
                LogManager.write("msg", "取得班表資料變更~", null);
            } else {
                dummy_roadID = 0;
                dummy_direct = 0;
                dummy_branch = null;
            }
//            else{
//                int roadID = 5;
//                int direct = 1;
//                String branch = "0";
//                this.changeRoad(roadID,direct,branch);
//                LogManager.write("msg", "無班表資料~改成５路", null);
//            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }


    @Override
    public void onReceivedBackend(com.hantek.ttia.protocol.a1a4.Message message) {
//		// 註冊成功才處理
//		if (!this.isLogon && !processList.contains(message.header.messageID))
//			return;

        int ackID = -1;
        int msgID = message.header.messageID;
        String debugString = String.format("Receive: A1A4 [ id=0x%02X seq:%s ]\n", msgID & 0xff, message.header.sequence);
        LogManager.write("comm1", "R:" + message.header.toString(), null);

        if (msgID == BackendMsgID.RegisterResponse.getValue()) {
            ackID = BackendMsgID.RegisterRequest.getValue();
            RegisterResponse response = (RegisterResponse) message.payload;
            SystemPara.getInstance().setHeader(message.header);
            SystemPara.getInstance().setCarID(message.header.carID);
            SharedPreferencesHelper.getInstance(this).setCarID(String.valueOf(message.header.carID));
            receiveRegisterResponse(response);
            Acceleration.getInstance().setLimited(response.accelerate, response.decelerate);
            checkScheduldata(response);
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += response.toString() + "\n";
            }
        } else if (msgID == BackendMsgID.NotifyMessage.getValue()) {
            NotifyMessage notifyMessage = (NotifyMessage) message.payload;
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += notifyMessage.toString() + "\n";
            }
            // sendMessage(BackendMsgID.NotifyMessageConfirm, null);
            sendNotifyConfirm(message.header.sequence);
            LogManager.write("msg", notifyMessage.toString(), null);
            SystemPara.getInstance().addNotifyMessageQueue(message);
        } else if (msgID == BackendMsgID.EventReportConfirm.getValue()) {
            ackID = BackendMsgID.EventReport.getValue();
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += "EventReportConfirm[]\n";
            }
        } else if (msgID == BackendMsgID.DeviceAlarmConfirm.getValue()) {
            ackID = BackendMsgID.DeviceAlarm.getValue();
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += "DeviceAlarmConfirm[]\n";
            }
        } else if (msgID == BackendMsgID.RegularReportConfirm.getValue()) {
            ackID = BackendMsgID.RegularReport.getValue();
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += "RegularReportConfirm[]\n";
            }
        } else if (msgID == BackendMsgID.ReportODConfirm.getValue()) {
            ackID = BackendMsgID.ReportOD.getValue();
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += "ReportODConfirm[]\n";
            }
        } else if (msgID == BackendMsgID.RoadModificationConfirm.getValue()) {
            ackID = BackendMsgID.RoadModification.getValue();
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += "RoadModificationConfirm[]\n";
            }
        } else if (msgID == BackendMsgID.ShutdownConfirm.getValue()) {
            ackID = BackendMsgID.Shutdown.getValue();
            if (TTIA_TEST_MODE) {
                debugString += message.header.toString() + "\n";
                debugString += "ShutdownConfirm[]\n";
            }
        } else {
            Log.w(TAG, "receive unknown msgID: " + msgID);
            if (TTIA_TEST_MODE) {
                debugString = "receive unknown msgID.\n";
            }
            return;
        }

        // TODO 待測
        if (SystemPara.getInstance().getRegisterResponse() == null)
            doLogon((byte) (coldReset ? 0 : 1), (byte) 1);

        if (TTIA_TEST_MODE) {
            obs(debugString, FragmentTestMode.HDL_A1A4);
        }
        Log.d(TAG, debugString);

        SystemPara.getInstance().addReceivePacketCounter();
        if (ackID != -1) {
            ForwardMessage forwardMessage = new ForwardMessage(ackID, message.header.sequence, null, 1, 0);
            ForwardManager.getInstance().remove(forwardMessage);
        }
    }

    @Override
    public void onETMDataReceived(com.hantek.ttia.protocol.d3.Message message) {
        String ds = null;

        ds = "Receive ETM [ id=0x" + String.format("%02X", message.getMessageID() & 0xff) + " ]\n";
        if (message.getMessageID() == ETMMsgID.AuthenResult.getValue()) {

            AuthenResult data = (AuthenResult) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == ETMMsgID.StartRequestGpsInfo.getValue()) {

            StartRequestGpsInfo data = (StartRequestGpsInfo) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
            this.receiveEtmStartRequestGpsInfo(data);
        } else if (message.getMessageID() == ETMMsgID.SendGpsInfoAck.getValue()) {

            SendGpsInfoAck data = (SendGpsInfoAck) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == ETMMsgID.StopRequestGpsInfo.getValue()) {

            StopRequestGpsInfo data = (StopRequestGpsInfo) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
            this.receiveEtmStopRequestGpsInfo(data);
        } else if (message.getMessageID() == ETMMsgID.UploadData.getValue()) {

            UploadData data = (UploadData) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
            this.receiveUploadData(data);
        } else if (message.getMessageID() == ETMMsgID.DataDownloadReady.getValue()) {

            DataDownloadReady data = (DataDownloadReady) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
            this.receiveDataDownloadReady(data);
        } else if (message.getMessageID() == ETMMsgID.DownloadDataAck.getValue()) {

            DownloadDataAck data = (DownloadDataAck) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
            this.receiveDownloadDataAck(data);
        } else if (message.getMessageID() == ETMMsgID.StartODDataAck.getValue()) {

            StartODDataAck data = (StartODDataAck) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }

            if (data.getResult() != 0x00) {

            } else {
                this.receiveStartODDataAck(data);
            }
        } else if (message.getMessageID() == ETMMsgID.StopODDataAck.getValue()) {

            StopODDataAck data = (StopODDataAck) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
            if (data.getResult() != 0x00) {

            }
        } else if (message.getMessageID() == ETMMsgID.ODData.getValue()) {

            ODData data = (ODData) message;
            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }

            // TODO upload A1A4 server

            if (TTIA_TEST_MODE) {
                ETM.getInstance().ODDataAck(Test.Result);
                if (Test.reportFlag == 1) {
                    ETM.getInstance().stopODDataReq();
                }
            }
        } else {
            Log.w(TAG, "receive unknown ETM msgID: " + message.getMessageID());
            if (TTIA_TEST_MODE) {
                ds += " unknown msgID.\n";
            }
        }

        if (TTIA_TEST_MODE) {
            obs(ds + "\n", FragmentTestMode.HDL_ETM_D3);
        }
    }

    private void receiveStartODDataAck(StartODDataAck data) {

    }

    private void receiveDownloadDataAck(DownloadDataAck data) {
        if (TTIA_TEST_MODE) {
            if (data.getResult() == 0x00) {
                // 已符合次數
                if (Test.dlDSN == Test.dlDTotal - 1)
                    return;

                Test.dlDSN += 1;
                // 最後一筆
                if (Test.dlDSN == Test.dlDTotal - 1)
                    Test.dlDLast = 1;

                ETM.getInstance().downloadData(Test.dlDID, Test.dlDSN, Test.dlDLast, Test.getdlD_2());
            }
        }
    }

    private void receiveDataDownloadReady(DataDownloadReady data) {
        if (data.getResult() != 0) {
            // means fail.
            // TODO shutdown connection.
            Log.w(TAG, "DataDownloadReady result: " + data.getResult());
            return;
        }

        // TODO get data from ETM server ..., then sendto ETM.

        if (TTIA_TEST_MODE) {
            // 最後一筆
            if (Test.dlDSN == Test.dlDTotal - 1)
                Test.dlDLast = 1;
            ETM.getInstance().downloadData(Test.dlDID, Test.dlDSN, Test.dlDLast, Test.getdlD());
        }
    }

    // 收到ETM的上傳資料
    private void receiveUploadData(UploadData data) {
        int serialNumber = data.getSerialNumber();
        // TODO upload server, then send result to ETM.

        if (TTIA_TEST_MODE) {
            if (Test.Result == 0x00) {
                ETM.getInstance().uploadDataAck(0, serialNumber);
            } else {
                ETM.getInstance().uploadDataAck(255, serialNumber);
            }
        }
    }

    // 收到ETM的GPS Start
    private void receiveEtmStartRequestGpsInfo(StartRequestGpsInfo data) {
        try {
            this.sendEtmGpsInterval = data.getGpsInfoReportInternal();
            this.sendEtmGpsNumOfReport = data.getNumOfReport();

            // min = 1 sec
            if (this.sendEtmGpsInterval == 0) {
                this.sendEtmGpsInterval = 1;
            }

            ETM.getInstance().startRequestGpsInfoAck(0);
            this.lastSendGpsInfoToETM = Calendar.getInstance();
            this.sendETMGps = true;
        } catch (Exception ex) {
            ETM.getInstance().startRequestGpsInfoAck(255);
        }
    }

    // 收到 ETM的GPS Stop
    private void receiveEtmStopRequestGpsInfo(StopRequestGpsInfo data) {
        this.sendETMGps = false;
        ETM.getInstance().stopRequestGpsInfoAck(0);
    }

    @Override
    public void onDCRDataReceived(com.hantek.ttia.protocol.e2.Message message) {
        String ds = null;

        ds = "Receive DCR [ id=" + String.format("%02X", message.getMessageID() & 0xff) + "h ]\n";
        if (message.getMessageID() == DCRMsgID.BasicData.getValue()) {
            BasicData data = (BasicData) message;

            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == DCRMsgID.DrivingData.getValue()) {
            DrivingData data = (DrivingData) message;

            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == DCRMsgID.InstantSpeed.getValue()) {
            InstantSpeed data = (InstantSpeed) message;

            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == DCRMsgID.HighSpeed.getValue()) {
            HighSpeed data = (HighSpeed) message;

            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == DCRMsgID.TiredDriving.getValue()) {
            TiredDriving data = (TiredDriving) message;

            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == DCRMsgID.ReadIntSpeed.getValue()) {
            ReadIntSpeed data = (ReadIntSpeed) message;

            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else if (message.getMessageID() == DCRMsgID.DriverRest.getValue()) {
            DriverRest data = (DriverRest) message;

            if (TTIA_TEST_MODE) {
                ds += data.toString();
            }
        } else {
            Log.w(TAG, "receive unknown DCR msgID: " + message.getMessageID());

            if (TTIA_TEST_MODE) {
                ds += " unknown msgID.\n";
            }
        }

        if (TTIA_TEST_MODE) {
            obs(ds + "\n", FragmentTestMode.HDL_DCR_E2);
        }
    }

    private void obs(String ds, int type) {
        if (ds != null && ds.length() > 0) {
            if (FragmentTestMode.mHandler != null) {
                Message msg = new Message();
                msg.what = type;
                msg.obj = ds;
                FragmentTestMode.mHandler.sendMessage(msg);
            }
        }
    }

    class ServiceReceiver extends BroadcastReceiver {
        final List<String> testList = new LinkedList<>();

        public ServiceReceiver() {
            testList.add(FragmentTestMode.TEST_A1A4);
            testList.add(FragmentTestMode.TEST_D1);
            testList.add(FragmentTestMode.TEST_D3);
            testList.add(FragmentTestMode.TEST_E2);
            testList.add(FragmentTestMode.TEST);
            testList.add(FragmentTestMode.TEST_GPS);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "service broadcast receive:" + action);
            try {
                if (testList.contains(action)) {
                    testMode(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void testMode(Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(FragmentTestMode.TEST)) {
                TTIA_TEST_MODE = intent.getBooleanExtra("test", false);
                Toast.makeText(SystemService.this, "Test Mode: " + TTIA_TEST_MODE, Toast.LENGTH_SHORT).show();
                return;
            }

            if (action.equalsIgnoreCase(FragmentTestMode.TEST_A1A4)) {
                int cmd = intent.getIntExtra("cmd", 0);
                int eventType = intent.getIntExtra("type", 0);

                if (BackendMsgID.getMappings().containsKey(cmd)) {
                    if (cmd != BackendMsgID.EventReport.getValue()) {
                        obs(String.format("Send:A1A4, MessageID:0x%02X\n", cmd), FragmentTestMode.HDL_A1A4);
                    } else {
                        obs(String.format("Send:A1A4, MessageID:0x%02X, Event:0x%04X\n", cmd, eventType), FragmentTestMode.HDL_A1A4);
                    }
                }

                if (cmd == BackendMsgID.RegisterRequest.getValue()) {
                    /* 註冊 */
                    doLogon((byte) Test.regType, Test.driverIDType);
                } else if (cmd == BackendMsgID.RoadModification.getValue()) {
                    /* 修改路線 */
                    doRoadModify(Test.id, Test.direct, branch, true);
                } else if (cmd == BackendMsgID.RegularReport.getValue()) {
                    /* 定時回報 */
                    RegularReport report = new RegularReport();
                    report.monitorDataList = RegularTransfer.getInstance().getMonitorStructType1Array();
                    if (report.monitorDataList.size() == 0) {
                        RegularTransfer.getInstance().addMonitorStructType1Array(getMonitorDataType1());
                        report.monitorDataList = RegularTransfer.getInstance().getMonitorStructType1Array();
                    }
                    report.monitorData = (byte) report.monitorDataList.size();
                    sendMessage(BackendMsgID.RegularReport, report.clone(), null);
                } else if (cmd == BackendMsgID.NotifyMessageConfirm.getValue()) {
                    /* 提示訊息確認 */
                    sendMessage(BackendMsgID.NotifyMessageConfirm, null, null);
                } else if (cmd == BackendMsgID.EventReport.getValue()) {
                    /* 事件回報 */
                    if (eventType == EventCode.InOutStation.getValue()) {
                        EventReport0x0001 event = new EventReport0x0001();
                        event.monitorData = getMonitorDataType2();
                        event.doorOpen = 1;
                        event.stationID = 5;
                        event.type = 0;
                        sendEventMessage(EventCode.InOutStation, event);
                    } else if (eventType == EventCode.OverSpeedAndRPM.getValue()) {
                        EventReport0x0002 event = new EventReport0x0002();
                        event.monitorData = getMonitorDataType2();
                        event.reserved = 0;
                        event.stationID = 5;
                        event.type = 1;
                        sendEventMessage(EventCode.OverSpeedAndRPM, event);
                    } else if (eventType == EventCode.Acceleration.getValue()) {
                        EventReport0x0004 event = new EventReport0x0004();
                        event.monitorData = getMonitorDataType2();
                        event.reserved = 0;
                        event.speed = SystemPara.getInstance().getRegisterResponse().accelerate;
                        event.type = 1;
                        sendEventMessage(EventCode.Acceleration, event);
                    } else if (eventType == EventCode.DoorOpen.getValue()) {
                        EventReport0x0008 event = new EventReport0x0008();
                        event.monitorData = getMonitorDataType2();
                        event.reserved = 0;
                        event.type = 1;
                        sendEventMessage(EventCode.DoorOpen, event);
                    } else if (eventType == EventCode.CarAlarm.getValue()) {
                        EventReport0x0010 event = new EventReport0x0010();
                        event.monitorData = getMonitorDataType2();
                        event.flag = 1;
                        event.type = 2;
                        sendEventMessage(EventCode.CarAlarm, event);
                    } else if (eventType == EventCode.UpdateCarStatus.getValue()) {
                        EventReport0x0020 event = new EventReport0x0020();
                        event.monitorData = getMonitorDataType2();
//                         event.preType
//                         event.type
                        sendEventMessage(EventCode.UpdateCarStatus, event);
                    } else if (eventType == EventCode.NoScheduleOnMove.getValue()) {
                        EventReport0x0040 event = new EventReport0x0040();
                        event.monitorData = getMonitorDataType2();
                        event.movement = SystemPara.getInstance().getRegisterResponse().movement;
                        sendEventMessage(EventCode.NoScheduleOnMove, event);
                    } else if (eventType == EventCode.DriverReport.getValue()) {
                        EventReport0x0080 event = new EventReport0x0080();
                        event.monitorData = getMonitorDataType2();
//                         event.infoID
//                         event.reserved
//                         event.type
                        sendEventMessage(EventCode.DriverReport, event);
                    } else if (eventType == EventCode.InOutSpecialArea.getValue()) {
                        EventReport0x0100 event = new EventReport0x0100();
                        event.monitorData = getMonitorDataType2();
                        event.regionID = 100;
                        sendEventMessage(EventCode.InOutSpecialArea, event);
                    } else if (eventType == EventCode.NotOnSchedule.getValue()) {
                        EventReport0x8000 event = new EventReport0x8000();
                        event.monitorData = getMonitorDataType2();
                        sendEventMessage(EventCode.NotOnSchedule, event);
                    }
                } else if (cmd == BackendMsgID.Shutdown.getValue()) {
                    /* 關機 */
                    Shutdown shutdown = new Shutdown();
                    shutdown.monitorData = getMonitorDataType2();
                    shutdown.psdReconnect = Test.PSDReconnect;
                    shutdown.packetRatio = Test.PacketRatio;
                    shutdown.gpsRatio = Test.GPSRatio;
                    sendMessage(BackendMsgID.Shutdown, shutdown, null);
                } else if (cmd == BackendMsgID.DeviceAlarm.getValue()) {
                    /* 障礙回報 */
                    sendDeviceAlarm(Test.module, Test.code);
                } else if (cmd == BackendMsgID.ReportOD.getValue()) {
                    /* OD回報 */
                    ODReport report = new ODReport();
                    report.routeID = Test.id;
                    report.routeDirect = (byte) Test.direct;
                    report.odReportList = Test.getReportList(1);
                    report.ODRecord = (byte) report.odReportList.size();
                    report.Reserved = 0;
                    try {
                        report.routeBranch = branch.getBytes("big5")[0];
                        sendMessage(BackendMsgID.ReportOD, report, null);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.equalsIgnoreCase(FragmentTestMode.TEST_D1)) {
                String cmd = intent.getStringExtra("cmd");
                if (cmd.equalsIgnoreCase("55P")) {
                    LED.getInstance().pause();
                } else if (cmd.equalsIgnoreCase("55O")) {
                    LED.getInstance().resume();
                } else if (cmd.equalsIgnoreCase("55Q")) {
                    LED.getInstance().forceStopPlay();
                } else if (cmd.equalsIgnoreCase("1")) {
                    LED.getInstance().sendMessage("00", Test.LEDanimation, '1', '0', '0', Test.LEDText);
                } else if (cmd.equalsIgnoreCase("2")) {
                    LED.getInstance().sendMessage("00", Animation.Now, Test.cc1, Test.cc2, Test.cc3, Test.LEDText);
                } else {
                    LED.getInstance().sendMessage("00", Test.LEDanimation, Test.cc1, Test.cc2, Test.cc3, Test.LEDText);
                }
            } else if (action.equalsIgnoreCase(FragmentTestMode.TEST_D3)) {
                int cmd = intent.getIntExtra("cmd", 0);
                if (ETMMsgID.getMappings().containsKey(cmd)) {
                    obs(String.format("Send:D3, MessageID:0x%02X\n", cmd), FragmentTestMode.HDL_ETM_D3);
                }

                if (cmd == ETMMsgID.AuthenRequest.getValue()) {
                    ETM.getInstance().authenRequest(Test.oat, Test.ot, Test.oID, Test.pwd);
                } else if (cmd == ETMMsgID.StartRequestGpsInfoAck.getValue()) {
                    ETM.getInstance().startRequestGpsInfoAck(Test.Result); /* ACK */
                } else if (cmd == ETMMsgID.SendGpsInfo.getValue()) {
                    ETM.getInstance().sendGpsInfo(etmGetGpsInfo());
                } else if (cmd == ETMMsgID.StopRequestGpsInfoAck.getValue()) {
                    ETM.getInstance().stopRequestGpsInfoAck(Test.Result); /* ACK */
                } else if (cmd == ETMMsgID.UploadDataReq.getValue()) {
                    ETM.getInstance().uploadDataReq(Test.UDRid, Test.UDRencryption);
                } else if (cmd == ETMMsgID.UploadDataAck.getValue()) {
                    ETM.getInstance().uploadDataAck(Test.Result, 0); /* ACK */
                } else if (cmd == ETMMsgID.DownloadDataReq.getValue()) {
                    ETM.getInstance().downloadDataReq(Test.downloadDataReq, Test.UDRencryption);
                } else if (cmd == ETMMsgID.DownloadData.getValue()) {
                    ETM.getInstance().downloadData(Test.dlDID, Test.dlDSN, Test.dlDLast, Test.getdlD());
                } else if (cmd == ETMMsgID.StartODDataReq.getValue()) {
                    ETM.getInstance().startODDataReq(Test.UDRencryption, Test.reportFlag);
                } else if (cmd == ETMMsgID.StopODDataReq.getValue()) {
                    ETM.getInstance().stopODDataReq();
                } else if (cmd == ETMMsgID.ODDataAck.getValue()) {
                    ETM.getInstance().ODDataAck(Test.Result); /* ACK */
                } else {
                    if (cmd == 0x00) {
                        Test.Result = 0x00;
                    } else {
                        Test.Result = cmd;
                    }
                }
            } else if (action.equalsIgnoreCase(FragmentTestMode.TEST_E2)) {
                String ds = null;
                int cmd = intent.getIntExtra("cmd", 0);
                if (DCRMsgID.getMappings().containsKey(cmd)) {
                    ds = String.format("Send:E2, MessageID:%02Xh\n", cmd);
                }

                if (cmd == DCRMsgID.BasicData.getValue()) {
                    DCR.getInstance().basicDataReq();
                } else if (cmd == DCRMsgID.DrivingData.getValue()) {
                    DCR.getInstance().drivingDataReq();
                } else if (cmd == DCRMsgID.InstantSpeed.getValue()) {
                    DCR.getInstance().instantSpeedReq();
                } else if (cmd == DCRMsgID.HighSpeed.getValue()) {
                    DCR.getInstance().highSpeedReq();
                } else if (cmd == DCRMsgID.TiredDriving.getValue()) {
                    DCR.getInstance().tiredDrivingReq();
                } else if (cmd == DCRMsgID.ReadIntSpeed.getValue()) {
                    try {
                        DCR.getInstance().readIntSpeedReq(Test.seconds);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (cmd == DCRMsgID.DriverRest.getValue()) {
                    DCR.getInstance().driverRestReq();
                } else if (cmd == DCRMsgID.ReportGPS.getValue()) {
                    ReportGps data = new ReportGps(GpsReceiver.getInstance().getUTCTime(), GpsReceiver.getInstance().getLongitude(), GpsReceiver.getInstance().getLatitude(), (float) GpsReceiver
                            .getInstance().getAngle(), (int) GpsReceiver.getInstance().getSpeed(), GpsReceiver.getInstance().isFixed());
                    DCR.getInstance().reportGPS(data);
                    ds += data.toString();
                }

                obs(ds, FragmentTestMode.HDL_DCR_E2);
            } else if (action.equalsIgnoreCase(FragmentTestMode.TEST_GPS)) {
                String cmd = intent.getStringExtra("cmd");
                if (cmd.equalsIgnoreCase("START")) {
                    lastLogGPSTime = Calendar.getInstance();
                    logGPS = true;
                    GpsReceiver.getInstance().setLogGps(true);
                } else if (cmd.equalsIgnoreCase("STOP")) {
                    lastLogGPSTime = Calendar.getInstance();
                    logGPS = false;
                    GpsReceiver.getInstance().setLogGps(false);
                } else if (cmd.equalsIgnoreCase("R_START")) {
                    GpsReceiver.getInstance().startListener(getApplication());
                    LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    SystemPara.getInstance().getBastLocation().unregister(locationmanager);
                    SystemPara.getInstance().getBastLocation().register(locationmanager, true);
                } else if (cmd.equalsIgnoreCase("R_STOP")) {
                    GpsReceiver.getInstance().stopListener();
                    LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    SystemPara.getInstance().getBastLocation().unregister(locationmanager);
                    SystemPara.getInstance().getBastLocation().getGps().setNow_location(null);
                } else if (cmd.equalsIgnoreCase("MARK")) {
                    try {
                        String content = String.format("Mark,%s,%s,%s,%s,%s,%s,%s", GpsReceiver.getInstance().getRawStatus(), GpsReceiver.getInstance().getSatelliteNumber(), GpsReceiver
                                .getInstance().getLongitude(), GpsReceiver.getInstance().getLatitude(), GpsReceiver.getInstance().getAngle(), GpsReceiver.getInstance().getSpeed(), GpsReceiver.getInstance().getTime());

                        obs(content + "\n", FragmentTestMode.HDL_GPS);
                        LogManager.write("GPS", content, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (cmd.equalsIgnoreCase("1")) {
                    int row = DatabaseHelper.getInstance(SystemService.this).ack1();
                    Toast.makeText(getApplication(), "affect " + row + " row..(Ack set 1)", Toast.LENGTH_LONG).show();
                } else if (cmd.equalsIgnoreCase("0")) {
                    int row = DatabaseHelper.getInstance(SystemService.this).ack0();
                    Toast.makeText(getApplication(), "affect " + row + " row..(Ack set 0)", Toast.LENGTH_LONG).show();
                } else if (cmd.equalsIgnoreCase("backup")) {
                    SharedPreferencesHelper.getInstance(SystemService.this).exportXML();
                    DatabaseHelper.getInstance(SystemService.this).exportDB();
                } else {
                    Calendar sTime = Calendar.getInstance();
                    int count = Integer.parseInt(cmd);
                    Cursor cursor = DatabaseHelper.getInstance(getApplication()).getPacket(0, count);
                    int row = 0;
                    if (cursor != null && cursor.getCount() >= 1) {
                        row = cursor.getCount();
                        cursor.moveToFirst();
                        do {
                            String hexString = cursor.getString(cursor.getColumnIndex(PacketEntity.MESSAGE)).trim();
                            int sequence = cursor.getInt(cursor.getColumnIndex(PacketEntity.SEQ));
                            int msgID = cursor.getInt(cursor.getColumnIndex(PacketEntity.MSGID));
                            int ack = cursor.getInt(cursor.getColumnIndex(PacketEntity.ACK));
                            int ack2 = cursor.getInt(cursor.getColumnIndex(PacketEntity.ACK2));

                            byte[] data = Utility.hexStringToByteArray(hexString);
                            int[] arr = convertToIntArray(data, data.length);

                            // check customer id and car id.
                            Header h = Header.Parse(arr);
                        } while (cursor.moveToNext());
                    }

                    long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
                    Toast.makeText(getApplication(), String.format("Load Packet %s, Row:%s, EOT:%s.", count, row, EOT), Toast.LENGTH_LONG).show();
                    LogManager.write("debug", String.format("Load Packet %s, Row:%s, EOT:%s.", count, row, EOT), null);
                }
            }
        }
    }

    /**
     * 註冊
     */
    private void doLogon(byte regType, byte driverIDType) {
        RegisterRequest request = new RegisterRequest();
        request.monitorData = this.getMonitorDataType2();

        // 系統開機, 可能未Ready, 在抓一次IMSI
        if (SystemPara.getInstance().getIMSI().equalsIgnoreCase("")) {
            SystemPara.getInstance().setIMSI(Utility.getIMSI(getBaseContext()));
        }
        request.IMSI = SystemPara.getInstance().getIMSI();

        // 系統開機, 可能未Ready, 在抓一次IMEI
        if (SystemPara.getInstance().getIMEI().equalsIgnoreCase("")) {
            SystemPara.getInstance().setIMEI(Utility.getIMEI(getBaseContext()));
        }
        String tmpIMEI = SystemPara.getInstance().getIMEI();
        if (!SharedPreferencesHelper.getInstance(this).getSystemIMEI()) {
            tmpIMEI = SharedPreferencesHelper.getInstance(this).getIMEI();
        }
        request.IMEI = tmpIMEI;

        request.manufacturer = SystemPara.getInstance().getManufacturer();
        request.OBUVersion = new byte[8];
        String tmp = "tBus2.0 ";//固定值
//        try {
//            tmp = Utility.getVersionName(getApplication());
//            tmp = tmp.substring(7);
//            Log.d(TAG, "OBUVersion:" + tmp);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        try {
            byte[] tmpBytes = tmp.getBytes();
            for (int i = 0; i < 8; i++) {
                request.OBUVersion[i] = tmpBytes[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.regType = regType;
        request.driverIDType = driverIDType;

        ArrayList<FileStruct> fileList = getFileList();
        if (fileList != null && fileList.size() > 0) {
            ArrayList<FileStruct> tmpFileList = new ArrayList<>();
            for (int i = 0; i < fileList.size(); i++) {
                tmpFileList.add(fileList.get(i));
                if (tmpFileList.size() >= 42)
                    break;
            }
            request.fileNumber = (byte) tmpFileList.size();
            request.fileList = tmpFileList;
        }

        this.sendMessage(BackendMsgID.RegisterRequest, request, null);
    }

    /**
     * 修改路線
     */
    private boolean doRoadModify(int id, int direct, String branch, boolean sendMsg) {
        RoadModification roadModify = new RoadModification();
        roadModify.routeID = getRouteID(id, branch);
        if (roadModify.routeID > 65535)
            return false;

        roadModify.routeDirect = (byte) direct;
        try {
            roadModify.routeBranch = branch.getBytes("big5")[0];
            if (sendMsg) {
                this.sendMessage(BackendMsgID.RoadModification, roadModify, null);
            }

            if (!TTIA_TEST_MODE) {
                if (!RoadManager.getInstance().setCurrentRoadID(id, direct, branch))
                    Toast.makeText(this, "找不到路線", Toast.LENGTH_LONG).show();
                else
                    SharedPreferencesHelper.getInstance(this).setRoadData(id, direct, branch);
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 註冊回應
     */
    private void receiveRegisterResponse(RegisterResponse response) {
        if (this.isLogon) {
            LogManager.write("debug", "logon already.", null);
        }
        LogManager.write("msg", response.toString(), null);
        // logon success
        if (response.result == 0) {
            SystemPara.getInstance().setRegisterResponse(response);
            this.report0x0001 = bitCheck(response.eventReport, 0x0001);
            this.report0x0002 = bitCheck(response.eventReport, 0x0002);
            this.report0x0004 = bitCheck(response.eventReport, 0x0004);
            this.report0x0008 = bitCheck(response.eventReport, 0x0008);
            this.report0x0010 = bitCheck(response.eventReport, 0x0010);
            this.report0x0020 = bitCheck(response.eventReport, 0x0020);
            this.report0x0040 = bitCheck(response.eventReport, 0x0040);
            this.report0x0080 = bitCheck(response.eventReport, 0x0080);
            this.report0x0100 = bitCheck(response.eventReport, 0x0100);
            this.report0x0200 = bitCheck(response.eventReport, 0x0200);
            this.report0x0400 = bitCheck(response.eventReport, 0x0400);
            this.report0x0800 = bitCheck(response.eventReport, 0x0800);
            this.report0x1000 = bitCheck(response.eventReport, 0x1000);
            this.report0x2000 = bitCheck(response.eventReport, 0x2000);
            this.report0x4000 = bitCheck(response.eventReport, 0x4000);
            this.report0x8000 = bitCheck(response.eventReport, 0x8000);

            RegularTransfer.getInstance().reset();

            //2016-02-21 取消使用華夏
            //RoadManager.getInstance().setInRadius(response.inRadius * 10);
            //RoadManager.getInstance().setOutRadius(response.outRadius * 10);

            this.isLogon = true;
        }

        ResponseManager.getInstance().set(BackendMsgID.RegisterRequest.getValue(), String.valueOf(response.result));
    }

    /**
     * 檢查進(出)站 (圓形範圍)
     */
    private void do0x0001Event() {
        // 每秒檢查
        if (Utility.dateDiffNow(this.lastCheckEvent0x0001) >= 1000) {
            this.lastCheckEvent0x0001 = Calendar.getInstance();
            try {
                GpsContent data = new GpsContent();
                data.lat = GpsReceiver.getInstance().getLatitude();
                data.lon = GpsReceiver.getInstance().getLongitude();
                data.isFixed = GpsReceiver.getInstance().isFixed();
                data.time = GpsReceiver.getInstance().getUTCTime();
                data.setSpeed(GpsReceiver.getInstance().getSpeed());
                data.setAngle(GpsReceiver.getInstance().getAngle());
                data.satelliteNumber = GpsReceiver.getInstance().getSatelliteNumber();
                data.nmeaLat = GpsReceiver.getInstance().getNmeaLatitude();
                data.nmeaLon = GpsReceiver.getInstance().getNmeaLongitude();
                data.gpsStruct = getGpsStruct();

                if (playStation) {
                    // 偵測進站至出站期間是否發生車門開啟
                    if (DioController.getInstance().getDiHi(1)) {
                        isDoorOpen = true;
                    }
                    if (DioController.getInstance().getDiHi(2)) {
                        isDoorOpen = true;
                    }

                    //20160920 Louie 改邏輯，改用物件 PNDPlay2
//                    RoadManager.getInstance().checkStation(data);
                }
            } catch (Exception e) {
                LogManager.write(e, null);
            }
        }
    }

    /**
     * 超轉超速
     */
    private void do0x0002Event() {
        if (!this.report0x0002)
            return;

        try {
            if (Utility.dateDiffNow(this.lastCheckEvent0x0002) >= 5000) {
                this.lastCheckEvent0x0002 = Calendar.getInstance();

                Station station = RoadManager.getInstance().getNextStation();
                if (station == null) {
                    Log.w(TAG, "no station");
                    return;
                }

                //2016/08/16 更改回傳stationid 為實際站牌id，不是站序
                if (SystemPara.getInstance().getCurrentRPM() > SystemPara.getInstance().getRegisterResponse().RPM) {
                    EventReport0x0002 event = new EventReport0x0002();
                    event.monitorData = this.getMonitorDataType2();
                    event.stationID = station.stop_id;
                    event.type = 0x00;
                    event.value = SystemPara.getInstance().getRegisterResponse().RPM;
                    sendEventMessage(EventCode.OverSpeedAndRPM, event);
                }

                if (SystemPara.getInstance().getCurrentSpeed() > station.speedLimit) {
                    EventReport0x0002 event = new EventReport0x0002();
                    event.monitorData = this.getMonitorDataType2();
                    event.stationID = station.stop_id;
                    event.type = 0x01;
                    event.value = station.speedLimit;
                    sendEventMessage(EventCode.OverSpeedAndRPM, event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 急加/減速 判斷
     */
    private void do0x0004Event() {
        if (!this.report0x0004)
            return;

        if (GpsReceiver.getInstance().isFixed()) {
            Acceleration.getInstance().check(GpsReceiver.getInstance().getSpeed());
        }
    }

    /**
     * 行駛中前門/後門開啟 判斷
     */
    private void do0x0008Event() {
        if (!this.report0x0008)
            return;

        try {
            // 持續5秒回報狀態。DI1回報前門開啟 DI2回報後門開啟。
            if (Utility.dateDiffNow(this.lastCheckEvent0x0008) >= 1000) {
                this.lastCheckEvent0x0008 = Calendar.getInstance();

                GpsStruct gpsStruct = getGpsStruct();
                if (gpsStruct.gpsStatus == 1 && gpsStruct.intSpeed > 0) {
                    if (DioController.getInstance().getDiHi(1)) {
                        this.frontDoorCounter += 1;
                    } else {
                        this.frontDoorCounter = 0;
                    }

                    if (DioController.getInstance().getDiHi(2)) {
                        this.backDoorCounter += 1;
                    } else {
                        this.backDoorCounter = 0;
                    }

                    // 持續開啟 每n秒回報
                    if (this.frontDoorCounter >= 10) {
                        this.frontDoorCounter = 0;
                        send0x0008Event((byte) 1, gpsStruct);
                    }

                    if (this.backDoorCounter >= 10) {
                        this.backDoorCounter = 0;
                        send0x0008Event((byte) 2, gpsStruct);
                    }
                } else {
                    this.frontDoorCounter = 0;
                    this.backDoorCounter = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send0x0008Event(byte type, GpsStruct gpsStruct) {
        EventReport0x0008 event = new EventReport0x0008();
        event.type = type;
        event.monitorData = this.getMonitorDataType2(gpsStruct);
        sendEventMessage(EventCode.DoorOpen, event);
    }

    /**
     * 車輛異常回報 判斷
     */
    private void do0x0010Event() {
        if (!this.report0x0010)
            return;

        try {
            if (Utility.dateDiffNow(this.lastCheckTimeCarAlarm) >= 1000) {
                this.lastCheckTimeCarAlarm = Calendar.getInstance();
                if (SystemPara.getInstance().getCurrentRPM() > 0 && SystemPara.getInstance().getCurrentSpeed() == 0) {
                    this.idleCounter += 1;
                } else {
                    this.idleCounter = 0;
                }

                if (this.isIdle && this.idleCounter == 0) {
                    this.isIdle = false;
                    send0x0010Event(1, 2);
                }

                if (!this.isIdle && this.idleCounter > SystemPara.getInstance().getRegisterResponse().halt * 60 * 1000) {
                    this.isIdle = true;
                    send0x0010Event(1, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send0x0010Event(int type, int flag) {
        EventReport0x0010 event = new EventReport0x0010();
        event.monitorData = this.getMonitorDataType2();
        event.type = (byte) type;
        event.flag = (byte) flag;
        sendEventMessage(EventCode.CarAlarm, event);
    }

    /**
     * 異常發車
     */
    private void do0x0040Event() {
        if (!this.report0x0040)
            return;

        try {
            if (Utility.dateDiffNow(this.lastCheckEvent0x0040) >= 1000) {
                this.lastCheckEvent0x0040 = Calendar.getInstance();
                if (SystemPara.getInstance().getRegisterResponse().schedule == 0 && SystemPara.getInstance().getCurrentDutyStatus() == DutyStatus.Ready) {

                    double lat = GpsReceiver.getInstance().getLatitude();
                    double lon = GpsReceiver.getInstance().getLongitude();
                    boolean fixed = GpsReceiver.getInstance().isFixed();

                    // 無效不計算
                    if (!fixed) {
                        return;
                    }

                    double meter = Utility.calcDistance(lastLon, lastLat, lon, lat);

                    SystemPara.getInstance().setAlarmMileage(meter);
                    this.lastLon = lon;
                    this.lastLat = lat;
                    this.lastFixed = fixed;

                    if (SystemPara.getInstance().getAlarmTotalMileage() >= SystemPara.getInstance().getRegisterResponse().movement * 10) {
                        SystemPara.getInstance().resetAlarmMileage();
                        EventReport0x0040 event = new EventReport0x0040();
                        event.monitorData = this.getMonitorDataType2();
                        event.movement = SystemPara.getInstance().getRegisterResponse().movement;
                        sendEventMessage(EventCode.NoScheduleOnMove, event);
                    }
                } else {
                    SystemPara.getInstance().resetAlarmMileage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 進出特定區域
     */
    private void do0x0100Event() {
        if (!this.report0x0100)
            return;

        if (Utility.dateDiffNow(this.lastCheckEvent0x0100Time) >= 1000) {
            this.lastCheckEvent0x0100Time = Calendar.getInstance();
            try {
                Polygon.getInstance().check(GpsReceiver.getInstance().getLongitude(), GpsReceiver.getInstance().getLatitude(), GpsReceiver.getInstance().isFixed());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 路線外營運回報
     */
    private void do0x8000Event() {
        if (!this.report0x8000)
            return;

        if (Utility.dateDiffNow(this.lastCheckEvent0x8000Time) >= 1000) {
            this.lastCheckEvent0x8000Time = Calendar.getInstance();
            try {
                // TODO something, reset flag
//                outOfRegion++;
//                if (outOfRegion > 60) {
//                    if (!this.notOnSchedule) {
//                        this.notOnSchedule = true;
//                        EventReport0x8000 event = new EventReport0x8000();
//                        event.monitorData = this.getMonitorDataType2();
//                        sendEventMessage(EventCode.NotOnSchedule, event);
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 障礙回報 DCR
     */
    private void checkDCR() {
        try {
            if (DCR.getInstance().isOpen() && Utility.dateDiffNow(this.lastCheckDCRTime) > 1000) {
                this.lastCheckDCRTime = Calendar.getInstance();
                if (Utility.dateDiffNow(DCR.getInstance().getLastReceiveTime()) > 10000) {
                    if (!this.dcrAlarmSent) {
                        this.dcrAlarmSent = true;
                        this.sendDeviceAlarm(DeviceModule.DCR.getValue(), WarningCode.ModuleNoResponse.getValue());
                    }
                } else {
                    if (this.dcrAlarmSent) {
                        this.dcrAlarmSent = false;
                        this.sendDeviceAlarm(DeviceModule.DCR.getValue(), WarningCode.Recovery.getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 障礙回報 GPS
     */
    private void checkGps() {
        try {
            if (Utility.dateDiffNow(this.lastCheckGpsTime) > 1000) {
                this.lastCheckGpsTime = Calendar.getInstance();
                if (Utility.dateDiffNow(GpsReceiver.getInstance().getLastReceiveTime()) > 10000) {
                    if (!this.gpsAlarmSent) {
                        this.gpsAlarmSent = true;
                        this.sendDeviceAlarm(DeviceModule.GPS.getValue(), WarningCode.ModuleNoResponse.getValue());
                    }
                } else {
                    if (this.gpsAlarmSent) {
                        this.sendDeviceAlarm(DeviceModule.GPS.getValue(), WarningCode.Recovery.getValue());
                        this.gpsAlarmSent = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 障礙回報 ETM
     */
    private void checkETM() {
        try {
            if (ETM.getInstance().isOpen() && Utility.dateDiffNow(this.lastCheckETMTime) > 1000) {
                this.lastCheckETMTime = Calendar.getInstance();
                if (Utility.dateDiffNow(ETM.getInstance().getLastReceiveTime()) > 10000) {
                    if (!this.etmAlarmSent) {
                        this.etmAlarmSent = true;
                        this.sendDeviceAlarm(DeviceModule.ETM.getValue(), WarningCode.ModuleNoResponse.getValue());
                    }
                } else {
                    if (this.etmAlarmSent) {
                        this.etmAlarmSent = false;
                        this.sendDeviceAlarm(DeviceModule.ETM.getValue(), WarningCode.Recovery.getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 障礙回報 LED
     */
    private void checkLED() {
        try {
            if (LED.getInstance().isOpen() && Utility.dateDiffNow(this.lastCheckLEDTime) > 1000) {
                this.lastCheckLEDTime = Calendar.getInstance();
                if (Utility.dateDiffNow(LED.getInstance().getLastReceiveTime()) > 10000) {
                    if (!this.ledAlarmSent) {
                        this.ledAlarmSent = true;
                        this.sendDeviceAlarm(DeviceModule.LED.getValue(), WarningCode.ModuleNoResponse.getValue());
                    }
                } else {
                    if (this.ledAlarmSent) {
                        this.ledAlarmSent = false;
                        this.sendDeviceAlarm(DeviceModule.LED.getValue(), WarningCode.Recovery.getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendDeviceAlarm(int module, int code) {
        DeviceAlarm deviceAlarm = new DeviceAlarm();
        deviceAlarm.module = (byte) module;
        deviceAlarm.code = (byte) code;
        sendMessage(BackendMsgID.DeviceAlarm, deviceAlarm, null);
    }

    private boolean bitCheck(int value, int condition) {
        boolean result = (value & condition) == condition;
        if (!result)
            Log.d(TAG, "BitCheck false: " + value + " & " + condition);
        return result;
    }

    private MonitorStructType1 getMonitorDataType1() {
        MonitorStructType1 monitorData = new MonitorStructType1();
        monitorData.gpsStruct = getGpsStruct();
        monitorData.avgSpeed = SystemPara.getInstance().getAvgSpeed();
        monitorData.intSpeed = SystemPara.getInstance().getIntSpeedArray();
        monitorData.RPM = SystemPara.getInstance().getRpmArray();
        monitorData.dutyStatus = (byte) SystemPara.getInstance().getCurrentDutyStatus().getValue();
        monitorData.busStatus = (byte) SystemPara.getInstance().getCurrentBusStatus().getValue();
        monitorData.mileage = SystemPara.getInstance().getMileage();
        return monitorData;
    }

    private MonitorStructType2 getMonitorDataType2() {
        MonitorStructType2 monitorData = new MonitorStructType2();
        monitorData.avgSpeed = SystemPara.getInstance().getAvgSpeed();
        monitorData.gpsStruct = getGpsStruct();
        monitorData.dutyStatus = (byte) SystemPara.getInstance().getCurrentDutyStatus().getValue();
        monitorData.busStatus = (byte) SystemPara.getInstance().getCurrentBusStatus().getValue();
        monitorData.mileage = SystemPara.getInstance().getMileage();
        return monitorData;
    }

    private MonitorStructType2 getMonitorDataType2(GpsStruct content) {
        MonitorStructType2 monitorData = new MonitorStructType2();
        monitorData.avgSpeed = SystemPara.getInstance().getAvgSpeed();
        monitorData.gpsStruct = content; // getGpsStruct(content);
        monitorData.dutyStatus = (byte) SystemPara.getInstance().getCurrentDutyStatus().getValue();
        monitorData.busStatus = (byte) SystemPara.getInstance().getCurrentBusStatus().getValue();
        monitorData.mileage = SystemPara.getInstance().getMileage();
        return monitorData;
    }

    private ArrayList<FileStruct> getFileList() {
        ArrayList<FileStruct> fileList = new ArrayList<>();
        // fileList.add(new FileStruct("TTIA", "110103"));
        return fileList;
    }

    private GpsStruct getGpsStruct() {
        GpsStruct struct = new GpsStruct();
        struct.satelliteNo = (byte) GpsReceiver.getInstance().getSatelliteNumber();
        struct.gpsStatus = (byte) (GpsReceiver.getInstance().getRawStatus().equalsIgnoreCase("A") ? 1 : 0);

        String tmpLongitude = GpsReceiver.getInstance().getNmeaLongitude();
        String tmpLatitude = GpsReceiver.getInstance().getNmeaLatitude();

        byte lonDu = Utility.nmeaToDu(tmpLongitude);
        byte lonFen = Utility.nmeaToFen(tmpLongitude);
        int lonMiao = Utility.nmeaToMiao(tmpLongitude);
        struct.longitudeDu = lonDu;
        struct.longitudeFen = lonFen;
        struct.longitudeMiao = lonMiao;
        struct.longitudeQuadrant = GpsReceiver.getInstance().getLongitudeQuadrant().getBytes()[0];
        if (lonDu != 0 && lonMiao == 0) {
            LogManager.write("error", String.format("Longitude:%s,%s,%s. %s,%s ", struct.longitudeDu, struct.longitudeFen, struct.longitudeMiao, tmpLatitude, tmpLongitude), null);
        }//2016-03-22 add log, why equals zero.

        byte latDu = Utility.nmeaToDu(tmpLatitude);
        byte latFen = Utility.nmeaToFen(tmpLatitude);
        int latMiao = Utility.nmeaToMiao(tmpLatitude);
        struct.latitudeDu = latDu;
        struct.latitudeFen = latFen;
        struct.latitudeMiao = latMiao;
        struct.latitudeQuadrant = GpsReceiver.getInstance().getLatitudeQuadrant().getBytes()[0];
        if (latDu != 0 && latMiao == 0) {
            LogManager.write("error", String.format("Latitude:%s,%s,%s. %s,%s ", struct.latitudeDu, struct.latitudeFen, struct.latitudeMiao, tmpLatitude, tmpLongitude), null);
        }//2016-03-22 add log, why equals zero.

        struct.direction = (int) GpsReceiver.getInstance().getAngle();

        // get dcr speed or gps speed
        boolean useGps = true;
        if (DCR.getInstance().isOpen() && Utility.dateDiffNow(DCR.getInstance().getLastReceiveTime()) < 5000) {
            useGps = false;
        }

        if (useGps) {
            struct.intSpeed = (int) (GpsReceiver.getInstance().getSpeed() * 1.852d); // gps speed
        } else {
            ReadIntSpeed readIntSpeed = DCR.getInstance().getLastReadIntSpeed();
            if (readIntSpeed != null) {
                List<SpeedData> dataList = readIntSpeed.getSpeedDataList();
                if (dataList != null && dataList.size() > 0) {
                    SpeedData data = dataList.get(0);
                    if (data != null) {
                        struct.intSpeed = data.getSpeed();
                    }
                }
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (Utility.dateDiffNow(GpsReceiver.getInstance().getLastReceiveTime()) < 5000) {
            calendar.setTime(GpsReceiver.getInstance().getUTCTime());
        }

        int tmpYear = calendar.get(Calendar.YEAR);
        struct.year = (byte) (tmpYear >= 2000 ? tmpYear - 2000 : 0);
        struct.month = (byte) (calendar.get(Calendar.MONTH) + 1);
        struct.day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        struct.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        struct.minute = (byte) calendar.get(Calendar.MINUTE);
        struct.second = (byte) calendar.get(Calendar.SECOND);

//         Log.d(TAG, "GetGpsStruct:" + tmpLongitude + " " + tmpLatitude);
        return struct;
    }

    private GpsStruct getGpsStruct(GpsContent content) {
        GpsStruct struct = new GpsStruct();
        struct.satelliteNo = (byte) content.satelliteNumber;
        struct.gpsStatus = (byte) (content.isFixed ? 1 : 0);

        String tmpLongitude = GpsReceiver.getInstance().getNmeaLongitude();
        struct.longitudeDu = Utility.nmeaToDu(String.valueOf(Utility.du2Nmea(content.lon)));
        struct.longitudeFen = Utility.nmeaToFen(String.valueOf(Utility.du2Nmea(content.lon)));
        struct.longitudeMiao = Utility.nmeaToMiao(String.valueOf(Utility.du2Nmea(content.lon)));
        struct.longitudeQuadrant = GpsReceiver.getInstance().getLongitudeQuadrant().getBytes()[0];

        String tmpLatitude = GpsReceiver.getInstance().getNmeaLatitude();
        struct.latitudeDu = Utility.nmeaToDu(String.valueOf(Utility.du2Nmea(content.lat)));
        struct.latitudeFen = Utility.nmeaToFen(String.valueOf(Utility.du2Nmea(content.lat)));
        struct.latitudeMiao = Utility.nmeaToMiao(String.valueOf(Utility.du2Nmea(content.lat)));
        struct.latitudeQuadrant = GpsReceiver.getInstance().getLatitudeQuadrant().getBytes()[0];

        struct.direction = (int) GpsReceiver.getInstance().getAngle();

        // get dcr speed or gps speed
        boolean useGps = false;
        if (!DCR.getInstance().isOpen() || Utility.dateDiffNow(DCR.getInstance().getLastReceiveTime()) > 5000) {
            useGps = true;
        }

        ReadIntSpeed readIntSpeed = DCR.getInstance().getLastReadIntSpeed();
        if (readIntSpeed != null) {
            List<SpeedData> dataList = readIntSpeed.getSpeedDataList();
            if (dataList != null && dataList.size() > 0) {
                SpeedData data = dataList.get(0);
                if (data != null) {
                    struct.intSpeed = data.getSpeed();
                }
            }
        }

        if (useGps) {
            struct.intSpeed = (int) (GpsReceiver.getInstance().getSpeed() * 1.852d); // gps speed
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(GpsReceiver.getInstance().getUTCTime());
        int tmpYear = calendar.get(Calendar.YEAR);
        struct.year = (byte) (tmpYear >= 2000 ? tmpYear - 2000 : 0);
        struct.month = (byte) (calendar.get(Calendar.MONTH) + 1);
        struct.day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        struct.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        struct.minute = (byte) calendar.get(Calendar.MINUTE);
        struct.second = (byte) calendar.get(Calendar.SECOND);

//         Log.d(TAG, "GetGpsStruct:" + tmpLongitude + " " + tmpLatitude);
        return struct;
    }

    private GpsInfo etmGetGpsInfo() {
        GpsInfo struct = new GpsInfo();
        struct.satelliteNo = (byte) GpsReceiver.getInstance().getSatelliteNumber();
        struct.gpsStatus = (byte) (GpsReceiver.getInstance().getRawStatus().equalsIgnoreCase("A") ? 1 : 0);

        String tmpLongitude = GpsReceiver.getInstance().getNmeaLongitude();
        struct.longitudeDu = Utility.nmeaToDu(tmpLongitude);
        struct.longitudeFen = Utility.nmeaToFen(tmpLongitude);
        struct.longitudeMiao = Utility.nmeaToMiao(tmpLongitude);

        String tmpLatitude = GpsReceiver.getInstance().getNmeaLatitude();
        struct.latitudeDu = Utility.nmeaToDu(tmpLatitude);
        struct.latitudeFen = Utility.nmeaToFen(tmpLatitude);
        struct.latitudeMiao = Utility.nmeaToMiao(tmpLatitude);

        struct.direction = (int) GpsReceiver.getInstance().getAngle();

        // get dcr speed or gps speed
        boolean useGps = false;
        if (!DCR.getInstance().isOpen() || Utility.dateDiffNow(DCR.getInstance().getLastReceiveTime()) > 5000) {
            useGps = true;
        }

        ReadIntSpeed readIntSpeed = DCR.getInstance().getLastReadIntSpeed();
        if (readIntSpeed != null) {
            List<SpeedData> dataList = readIntSpeed.getSpeedDataList();
            if (dataList != null && dataList.size() > 0) {
                SpeedData data = dataList.get(0);
                if (data != null) {
                    struct.intSpeed = data.getSpeed();
                }
            }
        }

        if (useGps) {
            struct.intSpeed = (int) (GpsReceiver.getInstance().getSpeed() * 1.852d); // gps speed
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(GpsReceiver.getInstance().getUTCTime());
        int tmpYear = calendar.get(Calendar.YEAR);
        struct.year = (byte) (tmpYear >= 2000 ? tmpYear - 2000 : 0);
        struct.month = (byte) (calendar.get(Calendar.MONTH) + 1);
        struct.day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        struct.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        struct.minute = (byte) calendar.get(Calendar.MINUTE);
        struct.second = (byte) calendar.get(Calendar.SECOND);

        return struct;
    }

    @Override
    public void enterStation(Station station, GpsContent gps, boolean endStation, Road road) {
        try {
            LogManager.write("STA", "S," + station.id + "(" +station.stop_id + "),"+ station.zhName + "," + station.type + "," + gps.toString(), null);

            if (station.type != 1) // 虛擬站點
                return;

            if (!endStation) {
                LEDPlayer.getInstance().play(station, road.audioGender, road.audioType, "in");
                AudioPlayer.getInstance().play(station, road.audioGender, road.audioType, "in");
            } else {
                LEDPlayer.getInstance().play(station, road.audioGender, road.audioType, "in");
                AudioPlayer.getInstance().play(station, road.audioGender, road.audioType, "in");

                AudioPlayer.getInstance().playStop();// 播放終點站
            }

            EventReport0x0001 event = new EventReport0x0001();
            event.doorOpen = 0x00;
            if (gps.gpsStruct == null)
                event.monitorData = this.getMonitorDataType2();
            else
                event.monitorData = this.getMonitorDataType2(gps.gpsStruct);
            //2016/08/16 改為實際站牌id
            event.stationID = station.stop_id;
            event.type = 0x01;
            event.oroad = road;
            event.istation = station;
            sendEventMessage(EventCode.InOutStation, event);
            isDoorOpen = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leaveStation(Station station, GpsContent gps, boolean endStation, Road road) {
        try {
            LogManager.write("STA", "E," + station.id + "(" +station.stop_id + ")," + station.zhName + "," + station.type + "," + gps.toString(), null);

            if (station.type != 1) // 虛擬站點
                return;

            int stationID = station.id <= 0 ? station.id : (station.id - 1);
            if (!endStation) {
                LEDPlayer.getInstance().play(station, road.audioGender, road.audioType, "out");
                AudioPlayer.getInstance().play(station, road.audioGender, road.audioType, "out");
            } else {
                // 最後一站
                stationID = station.id;
            }

            EventReport0x0001 event = new EventReport0x0001();
            event.doorOpen = (byte) (this.isDoorOpen ? 0x01 : 0x00);
            this.isDoorOpen = false;
            if (gps.gpsStruct == null)
                event.monitorData = this.getMonitorDataType2();
            else
                event.monitorData = this.getMonitorDataType2(gps.gpsStruct);
            event.stationID = road.stationArrayList.get(stationID).stop_id;
            event.type = 0x00;
            event.oroad = road;
            event.istation = road.stationArrayList.get(stationID);
            sendEventMessage(EventCode.InOutStation, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void debug(String content) {
        // LogManager.write("play_station", String.format("[%1$tH:%1$tM:%1$tS] ", new Date()) + content, null);
        LogManager.write("station", content, null);
    }

    @Override
    public void enterRegion(Region region) {
        EventReport0x0100 event = new EventReport0x0100();
        event.monitorData = this.getMonitorDataType2();
        event.regionID = region.ID;
        sendEventMessage(EventCode.InOutSpecialArea, event);
    }

    @Override
    public void leaveRegion(Region region) {
        // no define
    }

    private Header getHeader(BackendMsgID msgID, int sequence) {
        Header header = new Header();
        header.protocolID = SystemPara.getInstance().getProtocolID();
        header.protocolVer = SystemPara.getInstance().getProtocolVer();
        header.messageID = msgID.getValue();
        header.customerID = SystemPara.getInstance().getCustomerID();

        Header h = SystemPara.getInstance().getHeader();
        if (h == null)
            header.carID = SystemPara.getInstance().getCarID();
        else
            header.carID = h.carID;
        header.idStorage = SystemPara.getInstance().getIdStorage();

        RegisterResponse response = SystemPara.getInstance().getRegisterResponse();
        if (response == null)
            header.driverID = SystemPara.getInstance().getDriverID();
        else
            header.driverID = SystemPara.getInstance().getRegisterResponse().driverID; // SystemPara.getInstance().getDriverID();
        header.sequence = sequence;

        // 2016-02-15 暫時放入版本資訊.
        try {
            header.reserved = Utility.getVersionCode(this);
        } catch (Exception e) {
            e.printStackTrace();
            header.reserved = 0;
        }
        // header.payLoadLength = payloadLength;
        return header;
    }

    /**
     * 0 ~ 65535
     */
    private synchronized int getSequence() {
        if (this.sequence > 65535)
            this.sequence = 0;

        return this.sequence++;
    }

    private EventReportBase getEventReport(int eventType) {
        EventReportBase eventHeader = new EventReportBase();
        eventHeader.eventType = eventType;

        Road road = RoadManager.getInstance().getCurrentRoad();
        if (road == null) {
            eventHeader.routeID = 65535;
            eventHeader.routeDirect = 0;
            eventHeader.routeBranch = 0x30;
        } else {
            eventHeader.routeID = getRouteID(road.id, road.branch); // road.id;
            if (eventHeader.routeID > 65535)
                return null;
            eventHeader.routeDirect = (byte) road.direct;
            try {
                eventHeader.routeBranch = road.branch.getBytes("big5")[0];
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return eventHeader;
    }

    private boolean sendMessage(BackendMsgID msgID, Object payload, Header tmpHeader) {
        if (SystemPara.getInstance().getCustomerID() == 0) {
            LogManager.write("debug", "wrong header:" + SystemPara.getInstance().getCarID() + " " + SystemPara.getInstance().getCustomerID(), null);
            return false;
        }

        // Get Message Header.
        int tmpSequence = -1;
        Header header = tmpHeader;
        if (header == null) {
            tmpSequence = getSequence();
            header = getHeader(msgID, tmpSequence);
        }

        boolean inOutEvent = false;
        String payloadStr = "";
        byte[] data = null;
        switch (msgID) {
            case RegisterRequest:
                data = BackendController.getInstance().sendRegisterRequest(header, (RegisterRequest) payload);
                break;
            case RoadModification:
                data = BackendController.getInstance().sendRoadModification(header, (RoadModification) payload);
                break;
            case RegularReport:
                data = BackendController.getInstance().sendRegularReport(header, (RegularReport) payload);

                RegularReport tmp = (RegularReport) payload;
                SystemPara.getInstance().addGpsCounter();
                if (tmp.monitorDataList.get(0).gpsStruct.gpsStatus == 1) {
                    SystemPara.getInstance().addGpsFixedCounter();
                }
                break;
            case NotifyMessageConfirm:
                data = BackendController.getInstance().sendNotifyMessageConfirm(header);
                break;
            case EventReport:
                EventReportBase eventHeader = (EventReportBase) payload;
                data = BackendController.getInstance().sendEventReport(header, eventHeader, payload);
                if (eventHeader.eventType == EventCode.InOutStation.getValue()) {
                    inOutEvent = true;
                    EventReport0x0001 evnr = (EventReport0x0001) payload;
                    payloadStr = evnr.toFmtString();
                }
                break;
            case Shutdown:
                data = BackendController.getInstance().sendShutdown(header, (Shutdown) payload);
                break;
            case DeviceAlarm:
                data = BackendController.getInstance().sendDeviceAlarm(header, (DeviceAlarm) payload);
                break;
            case ReportOD:
                data = BackendController.getInstance().sendODReport(header, (ODReport) payload);
                break;
            default:
                break;
        }

        if (data != null) {
            SystemPara.getInstance().addSendPacketCounter();

            try {
                // Store Message
                if (ForwardManager.getInstance().forwardContains(msgID)) {
                    if (inOutEvent) {
                        ForwardMessage message2 = new ForwardMessage(msgID.getValue(), tmpSequence, data, 0, 0);
                        ForwardManager.getInstance().add(message2);
                    }

                    // 存到SQLite DB
//                    DatabaseHelper.getInstance(getApplicationContext()).insertPacket(String.valueOf(msgID.getValue()), String.valueOf(tmpSequence), Utility.bytesToHex(data),
//                            GpsClock.getInstance().getTime(), 0, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogManager.write("error", "send," + msgID.toString() + "," + e.toString() + ",.", null);
            }

            if (NetworkUtils.isOnline(getApplication())) {
                this.ttiaComm.send(data);
                this.hantekComm.send(data);
                LogManager.write("comm1", "S:" + header.toString() + payloadStr, null);
                LogManager.write("comm2", "S:" + header.toString() + payloadStr, null);
            } else {
                // 2016-03-22 無網路的紀錄
                LogManager.write("comm1", "SF:" + header.toString() + payloadStr, null);
                LogManager.write("comm2", "SF:" + header.toString() + payloadStr, null);
            }
        }

        if (tmpSequence != -1)
            SharedPreferencesHelper.getInstance(getApplication()).setSequence(tmpSequence + 1);

        return true;
    }

    private void sendEventMessage(EventCode code, Object eventContent) {
        EventReportBase eventReport = getEventReport(code.getValue());
        if (eventReport == null)
            return;

        EventReportBase tmpEventReport = null;
        switch (code) {
            case InOutStation:
                tmpEventReport = (EventReport0x0001) eventContent;
                break;
            case OverSpeedAndRPM:
                tmpEventReport = (EventReport0x0002) eventContent;
                break;
            case Acceleration:
                tmpEventReport = (EventReport0x0004) eventContent;
                break;
            case DoorOpen:
                tmpEventReport = (EventReport0x0008) eventContent;
                break;
            case CarAlarm:
                tmpEventReport = (EventReport0x0010) eventContent;
                break;
            case UpdateCarStatus:
                tmpEventReport = (EventReport0x0020) eventContent;
                break;
            case NoScheduleOnMove:
                tmpEventReport = (EventReport0x0040) eventContent;
                break;
            case DriverReport:
                tmpEventReport = (EventReport0x0080) eventContent;
                break;
            case InOutSpecialArea:
                tmpEventReport = (EventReport0x0100) eventContent;
                break;
            case EventCode200:
                break;
            case EventCode400:
                break;
            case EventCode800:
                break;
            case EventCode1000:
                break;
            case EventCode2000:
                break;
            case EventCode4000:
                tmpEventReport = (EventReport0x4000) eventContent;
                break;
            case NotOnSchedule:
                tmpEventReport = (EventReport0x8000) eventContent;
                break;
            default:
                return;
        }
        if (tmpEventReport == null)
            return;

        tmpEventReport.eventType = eventReport.eventType;
        tmpEventReport.routeBranch = eventReport.routeBranch;
        tmpEventReport.routeDirect = eventReport.routeDirect;
        tmpEventReport.routeID = eventReport.routeID;
        sendMessage(BackendMsgID.EventReport, tmpEventReport, null);
    }

    private void sendNotifyConfirm(int sequence) {
        Header header = getHeader(BackendMsgID.NotifyMessageConfirm, sequence);
        sendMessage(BackendMsgID.NotifyMessageConfirm, null, header.clone());
    }

    private void sendCheckUploadLog(String fileName, boolean result, int year, int month, int day) {
        try {
            Header header = getHeader(BackendMsgID.Reserved2, 1);
            UploadConfirm confirm = new UploadConfirm();
            confirm.year = year;
            confirm.month = month;
            confirm.day = day;
            confirm.type = 0;
            confirm.result = (byte) (result ? 1 : 0);
            confirm.fileName = fileName;
            byte[] data = HantekController.getInstance().sendUploadConfirm(header, confirm);

            boolean online = false;
            if (NetworkUtils.isOnline(getApplication())) {
                online = true;
                this.hantekComm.send(data);
            }
            LogManager.write("comm2", "S:" + header.toString() + ",online:" + online, null);
        } catch (Exception e) {
            LogManager.write("error", "sendCheckUploadLog," + fileName + "," + e.toString(), null);
        }
    }

    private void sendUploadConfirm(String fileName, int result, int year, int month, int day) {
        try {
            Header header = getHeader(BackendMsgID.Reserved2, 2);
            UploadConfirm confirm = new UploadConfirm();
            confirm.year = year;
            confirm.month = month;
            confirm.day = day;
            confirm.type = 1;
            confirm.result = (byte) result;
            confirm.fileName = fileName;
            byte[] data = HantekController.getInstance().sendUploadConfirm(header, confirm);

            boolean online = false;
            if (NetworkUtils.isOnline(getApplication())) {
                online = true;
                this.hantekComm.send(data);
            }
            LogManager.write("comm2", "S:" + header.toString() + ",online:" + online, null);
        } catch (Exception e) {
            LogManager.write("error", "sendUploadConfirm," + fileName + "," + e.toString(), null);
        }
    }

    private int getRouteID(int routeID, String branch) {
        // 其他路線固定為65535
        if (routeID == 65535)
            return 65535;

        // 新營客運(999), 興南客運(7600), 定義5碼的路線ID.
        if (SystemPara.getInstance().getCustomerID() == 999 || SystemPara.getInstance().getCustomerID() == 7600) {
            try {
                routeID = Integer.parseInt(branch) + routeID * 10;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return routeID;
    }

    @Override
    public void retry(ForwardMessage message) {
        if (message.getComm1Ack() == 0) {
            LogManager.write("comm1", String.format("S-retry: ID=%s, SEQ=%s, COUNT=%s.", message.getMsgID(), message.getSequence(), message.getSendComm1Count()), null);
            this.ttiaComm.send(message.getData());
            // Log.w(TAG, String.format("Retry to [1], ID=%s, SEQ=%s, COUNT=%s. Forward List:%s.", message.getMsgID(), message.getSequence(), message.getSendComm1Count(), ForwardManager.getInstance().getForwardCount()));
        }

        if (message.getComm2Ack() == 0) {
            LogManager.write("comm2", String.format("S-retry: ID=%s, SEQ=%s, COUNT=%s.", message.getMsgID(), message.getSequence(), message.getSendComm2Count()), null);
            this.hantekComm.send(message.getData());
            // Log.w(TAG, String.format("Retry to [2], ID=%s, SEQ=%s COUNT=%s. Forward List:%s.", message.getMsgID(), message.getSequence(), message.getSendComm2Count(), ForwardManager.getInstance().getForwardCount()));
        }
    }

    private int[] convertToIntArray(byte[] input, int size) {
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }

    @Override
    public boolean isConfiguration() {
        int check = SharedPreferencesHelper.getInstance(this).getConfig();
        if (check == 1) {
            SystemPara.getInstance().setCustomerID(Integer.parseInt(SharedPreferencesHelper.getInstance(this).getCustomerID()));
            SystemPara.getInstance().setCarID(Integer.parseInt(SharedPreferencesHelper.getInstance(this).getCarID()));
            SystemPara.getInstance().setIdStorage(SharedPreferencesHelper.getInstance(this).getIDStorage() ? 0 : 1);
        }

        return check == 1;
    }

    private boolean isDone = false;

    @Override
    public boolean isDown() {
        return isDone;
    }

    @Override
    public boolean checkRegisterResponse() {
        if (isDone)
            return true;

        RegisterResponse response = SystemPara.getInstance().getRegisterResponse();
        if (response == null) {
            return false;
        }
        if (response.schedule == 0) {
            return false;
        }
        if (response.routeID == 0) {
            return false;
        }

        try {
            // 新營客運 5碼路線ID，取4碼
            if (SystemPara.getInstance().getCustomerID() == 999 || SystemPara.getInstance().getCustomerID() == 7600) {
                response.routeID = response.routeID / 10;
            }
            if (response.routeID == 0) {
                return false;
            }

            if (!doRoadModify(response.routeID, response.routeDirect, Character.toString((char) response.routeBranch), false)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // String branch = String.valueOf(response.routeBranch);
        ArrayList road = RoadManager.getInstance().queryRoad(response.routeID, response.routeDirect);
        if (road.size() == 0) {
            // 無路線資料
            LogManager.write("debug", String.format("no road list: %s %s %s.", response.routeID, response.routeDirect, Character.toString((char) response.routeBranch)), null);
            return false;
        }

        int id = SharedPreferencesHelper.getInstance(this).getRoadID();
        int direct = SharedPreferencesHelper.getInstance(this).getRoadDirect();
        String branch = SharedPreferencesHelper.getInstance(this).getRoadBranch();

        LogManager.write("debug", String.format("Last road: %s %s %s", id, direct, branch), null);
        return true;
    }

    @Override
    public boolean download(DownloadReceiver receiver) {
        LogManager.write("debug", "DownloadReceiver~", null);
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("customer_id", SharedPreferencesHelper.getInstance(this).getCustomerID());
        intent.putExtra("receiver", receiver);
        startService(intent);
        return false;
    }

    @Override
    public boolean login(String driverID) {
        LogManager.write("debug", String.format("login:%s.", driverID), null);
        SharedPreferencesHelper.getInstance(getApplication()).setDriverID(driverID);
        SystemPara.getInstance().setDriverID(Long.parseLong(driverID));
        doLogon((byte) (coldReset ? 0 : 1), (byte) 1);
        // coldReset = false;
        return false;
    }

    @Override
    public boolean systemCheck() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean logoff() {
        LogManager.write("debug", String.format("logoff"), null);
        isLogon = false;
        return false;
    }

    @Override
    public boolean changeRoad(int roadID, int direct, String branch) {
        LogManager.write("debug", String.format("changeRoad:%s,%s,%s.", roadID, direct, branch), null);
        boolean ret = doRoadModify(roadID, direct, branch, true);
        if (!ret) {
            return false;
        }

        isDone = true;
        return true;
    }

    @Override
    public ArrayList<Road> queryRoad(int id, int direct) {
        return RoadManager.getInstance().queryRoad(id, direct);
    }

    @Override
    public boolean startWork(boolean manually) {
        LogManager.write("debug", (manually ? "Manually" : "Auto") + " startWork.", null);
        playStation = true;
        RoadManager.getInstance().reset();
        LED.getInstance().forceStopPlay();
        if (manually) {
            SharedPreferencesHelper.getInstance(this).setStatus(1);
            AudioPlayer.getInstance().setRoadWelcome(RoadManager.getInstance().getCurrentRoad().audioNumber, "c", "m");
            AudioPlayer.getInstance().playWelcome();

            //客製功能-發送勤務狀態
            int infoID2 = SystemPara.getInstance().getInfoID();
            int manuallyID = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 100 + Calendar.getInstance().get(Calendar.MINUTE);
            int infoID = (infoID2 == 0 ? manuallyID : infoID2);
            SystemPara.getInstance().setInfoID(infoID);
            SharedPreferencesHelper.getInstance(this).setInfoID(infoID);

            int preType = SystemPara.getInstance().getCurrentDutyStatus().getValue();
            DutyStatus newType = DutyStatus.Start;
            SystemPara.getInstance().setCurrentDutyStatus(newType);
            SharedPreferencesHelper.getInstance(this).setDutyStatus(newType.getValue());

            EventReport0x4000 event = new EventReport0x4000();
            event.monitorData = this.getMonitorDataType2();
            event.infoID = SystemPara.getInstance().getInfoID();
            event.type = (byte) newType.getValue();
            event.preType = (byte) preType;
            sendEventMessage(EventCode.EventCode4000, event);

            int preBusType = SystemPara.getInstance().getCurrentBusStatus().getValue();
            SystemPara.getInstance().setCurrentBusStatus(BusStatus.OnRoad);
            SharedPreferencesHelper.getInstance(this).setBusStatus(BusStatus.OnRoad.getValue());

            EventReport0x0020 event0020 = new EventReport0x0020();
            event0020.monitorData = this.getMonitorDataType2();
            event0020.type = (byte) BusStatus.OnRoad.getValue();
            event0020.preType = (byte) preBusType;
            sendEventMessage(EventCode.UpdateCarStatus, event0020);
        }

        // 歡迎詞
        LEDPlayer.getInstance().startWork(manually);
        return false;
    }

    @Override
    public boolean stopWork() {
        LogManager.write("debug", "stopWork", null);
        playStation = false;
        SharedPreferencesHelper.getInstance(this).setStatus(0);
        LEDPlayer.getInstance().stopWork();
        LED.getInstance().forceStopPlay();
        AudioPlayer.getInstance().interruptPlay();
        return false;
    }

    @Override
    public boolean changeBusStatus(int status) {
        try {
            int preType = SystemPara.getInstance().getCurrentBusStatus().getValue();
            SystemPara.getInstance().setCurrentBusStatus(BusStatus.forValue(status));
            SharedPreferencesHelper.getInstance(this).setBusStatus(status);

            LogManager.write("debug", "changeBusStatus: " + status, null);
            EventReport0x0020 event = new EventReport0x0020();
            event.monitorData = this.getMonitorDataType2();
            event.type = (byte) status;
            event.preType = (byte) preType;
            sendEventMessage(EventCode.UpdateCarStatus, event);
        } catch (Exception e) {
            LogManager.write("error", "changeBusStatus: " + status, null);
        }
        return false;
    }

    @Override
    public boolean changeStation(int stationID) {
        LogManager.write("debug", "changeStation: " + stationID, null);
        RoadManager.getInstance().setNextStation(stationID);

        Station station = RoadManager.getInstance().getNextStation();
        LogManager.write("debug", "NextStation: " + station.zhName + " ( " + station.stop_id + " )", null);
        Road road = RoadManager.getInstance().getCurrentRoad();

        LEDPlayer.getInstance().play(station, road.audioGender, road.audioType, "out");
        AudioPlayer.getInstance().play(station, road.audioGender, road.audioType, "out");
        return false;
    }

    @Override
    public boolean changeDirect(int direct) {
        LogManager.write("debug", "change direct: " + direct, null);
        LED.getInstance().forceStopPlay();
        if (RoadManager.getInstance().toggle()) {
            Road tmpRoad = RoadManager.getInstance().getCurrentRoad();
            SharedPreferencesHelper.getInstance(this).setRoadData(tmpRoad.id, tmpRoad.direct, tmpRoad.branch);

            try {
                // 2016-02-24 變更方向後, 回傳中心.
                RoadModification roadModify = new RoadModification();
                roadModify.routeID = getRouteID(tmpRoad.id, tmpRoad.branch);
                roadModify.routeDirect = (byte) tmpRoad.direct;
                roadModify.routeBranch = tmpRoad.branch.getBytes()[0];
                this.sendMessage(BackendMsgID.RoadModification, roadModify, null);

                // TODO 變更勤務狀態or行車狀態
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else
            return false;
    }

    @Override
    public boolean changeDutyStatus(int status) {
        try {
            int preType = SystemPara.getInstance().getCurrentDutyStatus().getValue();
            SystemPara.getInstance().setCurrentDutyStatus(DutyStatus.forValue(status));
            SharedPreferencesHelper.getInstance(this).setDutyStatus(status);

            LogManager.write("debug", "changeDutyStatus: " + DutyStatus.forValue(status).toString(), null);
            EventReport0x4000 event = new EventReport0x4000();
            event.monitorData = this.getMonitorDataType2();
            event.infoID = SystemPara.getInstance().getInfoID();
            event.type = (byte) status;
            event.preType = (byte) preType;
            sendEventMessage(EventCode.EventCode4000, event);

//            if (status == DutyStatus.Stop.getValue()) {
//                int preBusType = SystemPara.getInstance().getCurrentBusStatus().getValue();
//                SystemPara.getInstance().setCurrentBusStatus(BusStatus.Offline);
//                SharedPreferencesHelper.getInstance(this).setBusStatus(BusStatus.Offline.getValue());
//
//                EventReport0x0020 event0020 = new EventReport0x0020();
//                event0020.monitorData = this.getMonitorDataType2();
//                event0020.type = (byte) BusStatus.Offline.getValue();
//                event0020.preType = (byte) preBusType;
//                sendEventMessage(EventCode.UpdateCarStatus, event0020);
//            }
        } catch (Exception e) {
            LogManager.write("error", "changeDutyStatus: " + status, null);
        }
        return false;
    }

    @Override
    public boolean replyNotifyMessage(int infoID, int reportType) {
        // 確認回覆
        if (reportType == 0 && infoID != 0) {
            try {
                int preBusType = SystemPara.getInstance().getCurrentBusStatus().getValue();
                SystemPara.getInstance().setCurrentBusStatus(BusStatus.OnRoad);
                SharedPreferencesHelper.getInstance(this).setBusStatus(BusStatus.OnRoad.getValue());

                EventReport0x0020 event0020 = new EventReport0x0020();
                event0020.monitorData = this.getMonitorDataType2();
                event0020.type = (byte) BusStatus.OnRoad.getValue();
                event0020.preType = (byte) preBusType;
                sendEventMessage(EventCode.UpdateCarStatus, event0020);
            } catch (Exception e) {
                LogManager.write("error", "replyNotifyMessage: infoID=" + infoID + ", reportType=" + reportType, null);
            }
        }

        LogManager.write("debug", "replyNotifyMessage: infoID=" + infoID + ", reportType=" + reportType, null);
        EventReport0x0080 event = new EventReport0x0080();
        event.monitorData = this.getMonitorDataType2();
        event.infoID = infoID;
        event.type = (byte) reportType;
        event.reserved = 0;
        sendEventMessage(EventCode.DriverReport, event);

        if (infoID != 0 && reportType != 2) {
            //test
            //固定班表確認INFO ID
            if (dummy_branch != null) {
                this.changeRoad(dummy_roadID, dummy_direct, dummy_branch);
                dummy_branch = null;
            }
            SystemPara.getInstance().setInfoID(infoID);
            SharedPreferencesHelper.getInstance(this).setInfoID(infoID);
            SystemPara.getInstance().setFireCar(true);
        } else {
            SystemPara.getInstance().setFireCar(false);
        }
        return true;
    }

    @Override
    public boolean secondGo() {
        doLogon((byte) 0x01, (byte) 0x01);
        return false;
    }

    @Override
    public int getGpsStatus() {
        if (!GpsReceiver.getInstance().isEnable())
            return -1;

        return GpsReceiver.getInstance().getRawStatus().equalsIgnoreCase("A") ? 1 : 0;
    }

    @Override
    public int getAccStatus() {
        return DioController.getInstance().getAccStatus(false) ? 1 : 0;
    }

    @Override
    public int getNetworkStatus() {
        if (NetworkUtils.isConnectedWifi(getApplication()))
            return 0;

        if (NetworkUtils.isConnectedMobile(getApplication()))
            return 1;

        return -1;
    }

    @Override
    public int getComm1Status() {
        if (this.ttiaComm == null)
            return -1;

        if (Utility.dateDiffNow(this.ttiaComm.getLastReceiveTime()) > 60000)
            return 0;

        return 1;
    }

    @Override
    public int getComm2Status() {
        if (this.hantekComm == null)
            return -1;

        if (Utility.dateDiffNow(this.hantekComm.getLastReceiveTime()) > 60000)
            return 0;

        return 1;
    }

    @Override
    public int getSignalStrengths() {
        return Phone.getInstance().getSignalStrengths();
    }

    @Override
    public int getDI1() {
        try {
            if (!DioController.getInstance().is5668())
                return -1;
            return DioController.getInstance().getDiHi(1) ? 1 : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int getDI2() {
        try {
            if (!DioController.getInstance().is5668())
                return -1;
            return DioController.getInstance().getDiHi(2) ? 1 : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int getttyUSB1() {
        if (!sp1)
            return 0;
        return 1;
    }

    @Override
    public int getttyUSB2() {
        if (!sp2)
            return 0;
        return 1;
    }

    @Override
    public int getttyUSB3() {
        if (!sp3)
            return 0;
        return 1;
    }

    @Override
    public int getAdvertFileSize() {
        return DownloadService.getPrepareSize();
    }

    @Override
    public void onAccelerationTrigger(byte type, int speed) {
        EventReport0x0004 event = new EventReport0x0004();
        event.monitorData = this.getMonitorDataType2();
        event.type = type;
        event.speed = speed;
        sendEventMessage(EventCode.Acceleration, event);
    }
}