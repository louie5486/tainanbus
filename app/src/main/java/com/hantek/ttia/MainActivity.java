package com.hantek.ttia;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hantek.ttia.module.DateTime;
import com.hantek.ttia.module.SharedPreferencesHelper;
import com.hantek.ttia.module.Utility;
import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.protocol.a1a4.BusStatus;
import com.hantek.ttia.protocol.a1a4.DutyStatus;
import com.hantek.ttia.protocol.a1a4.NotifyMessage;
import com.hantek.ttia.services.IService;
import com.hantek.ttia.services.SystemService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import component.LogManager;

public class MainActivity extends ActionBarActivity implements Runnable { // implements ActionBar.TabListener {
    private static final String TAG = MainActivity.class.getName();

    public static final int LOGOFF = 1;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded fragment in
     * memory. If this becomes too memory intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * 按鈕間隔
     */
    final int CLICK_INTERVAL = 500;
    private long lastClickEvent = 0;

    private NotifyMessageDialog dialog = null;

    private Handler mHandler = null;
    private UserLogoffTask mAuthTask = null;
    private ProgressDialog mProgressDlg;

    private QuickChangeDialogFragment selectDialog = new QuickChangeDialogFragment();
    private SelectDirectFragment selectDirectFragment = new SelectDirectFragment();
    private RoadBranchDialogFragment roadBranchDialogFragment = new RoadBranchDialogFragment();
    private SelectDirectFragment2 selectDirectFragment2 = new SelectDirectFragment2();

    private IService imService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            imService = ((SystemService.LocalBinder) service).getService();
            mHandler.postDelayed(MainActivity.this, 1000);

            Log.d(TAG, "***** onServiceConnected *****");
            if (imService.checkRegisterResponse()) {
                Log.d(TAG, "***** checkRegisterResponse success *****");
                if (!imService.isDown() && RoadManager.getInstance().getCurrentRoad() != null) {
                    // auto login
                    int id = SharedPreferencesHelper.getInstance(MainActivity.this).getRoadID();
                    int direct = SharedPreferencesHelper.getInstance(MainActivity.this).getRoadDirect();
                    String branch = SharedPreferencesHelper.getInstance(MainActivity.this).getRoadBranch();

                    LogManager.write("debug", String.format("retrieve: ID:%s, Direct:%s, Branch:%s.", id, direct, branch), null);
                    if (submitRoad(id, direct, branch)) {
                        startWork(false);
                    }
                }
            } else {
                Log.d(TAG, "***** checkRegisterResponse fail *****");
                if (SharedPreferencesHelper.getInstance(MainActivity.this).getStatus() == 0) {
                    mSectionsPagerAdapter.firstPage.switchSelectRoad();
                } else {
                    // auto login
                    int id = SharedPreferencesHelper.getInstance(MainActivity.this).getRoadID();
                    int direct = SharedPreferencesHelper.getInstance(MainActivity.this).getRoadDirect();
                    String branch = SharedPreferencesHelper.getInstance(MainActivity.this).getRoadBranch();

                    LogManager.write("debug", String.format("retrieve: ID:%s, Direct:%s, Branch:%s.", id, direct, branch), null);
                    if (submitRoad(id, direct, branch)) {
                        startWork(false);
                    }
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "***** onServiceDisconnected *****");

            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            imService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        // before set view
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // actionBar.setDisplayShowHomeEnabled(false);
        // actionBar.setDisplayShowTitleEnabled(false);
        // actionBar.setDisplayUseLogoEnabled(false);
        actionBar.hide();

        mHandler = new InnerHandler(getMainLooper());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        // mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
        // @Override
        // public void onPageSelected(int position) {
        // actionBar.setSelectedNavigationItem(position);
        // }
        // });

        // For each of the sections in the app, add a tab to the action bar.
        // for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
        // // Create a tab with text corresponding to the page title defined by
        // // the adapter. Also specify this Activity object, which implements
        // // the TabListener interface, as the callback (listener) for when
        // // this tab is selected.
        // actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        // }

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "setOnPageChangeListener: " + position);
                Intent intent = new Intent();
                intent.setAction(FragmentTestMode.TEST);
                switch (position) {
                    case 0:
                        intent.putExtra("test", false);
                        break;
                    case 1:
                        intent.putExtra("test", true);
                        break;
                    default:
                        intent.putExtra("test", false);
                        break;
                }

                sendBroadcast(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume");
        bindService(new Intent(this, SystemService.class), mConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "OnPause");
        mHandler.removeCallbacks(this);
        unbindService(mConnection);
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // return super.onKeyDown(keyCode, event);
        return false;
    }

    @Override
    public void run() {
        try {
            doNotifyMessage();
            this.mSectionsPagerAdapter.firstPage.setTime(new DateTime().getFormatedSystemTime());

            if (imService != null) {
                this.mSectionsPagerAdapter.firstPage.setGps(imService.getGpsStatus());
                this.mSectionsPagerAdapter.firstPage.setAcc(imService.getAccStatus());
                this.mSectionsPagerAdapter.firstPage.setMode(imService.getNetworkStatus());
                this.mSectionsPagerAdapter.firstPage.setSignal(imService.getSignalStrengths());

                this.mSectionsPagerAdapter.firstPage.setComm1(imService.getComm1Status());
                this.mSectionsPagerAdapter.firstPage.setComm2(imService.getComm2Status());

                this.mSectionsPagerAdapter.firstPage.setDi1(imService.getDI1());
                this.mSectionsPagerAdapter.firstPage.setDi2(imService.getDI2());

                this.mSectionsPagerAdapter.firstPage.setttyUSB1(imService.getttyUSB1());
                this.mSectionsPagerAdapter.firstPage.setttyUSB2(imService.getttyUSB2());
                this.mSectionsPagerAdapter.firstPage.setttyUSB3(imService.getttyUSB3());

                int size = imService.getAdvertFileSize();
                this.mSectionsPagerAdapter.firstPage.setAdvert(size == 0 ? "" : String.valueOf(size));
            }

            if (SystemPara.getInstance().getFireCar()) {
                SystemPara.getInstance().setFireCar(false);
                startWork(true); // 2015/01/13 v1.9
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.postDelayed(this, 1000);
    }

    public void onLogoff() {
        showProgressDlg("登出..");
        SharedPreferencesHelper.getInstance(this).setAutoLogin(false);
        mAuthTask = new UserLogoffTask("");
        mAuthTask.execute((Void) null);
    }

    public void startWork(boolean manually) {
        if (RoadManager.getInstance().getCurrentRoad() != null) {
            this.imService.startWork(manually);
            this.mSectionsPagerAdapter.firstPage.switchOnRoad();
        }
    }

    public void stopWork() {
        this.mSectionsPagerAdapter.firstPage.switchMain();
        imService.stopWork();
    }

    public void secondGo() {
        imService.secondGo();
    }

    public ArrayList<Road> queryRoad(int roadID, int direct) {
        return RoadManager.getInstance().queryRoad(roadID, direct);
    }

    public ArrayList<Road> queryRoad(int roadID, int direct, String branch) {
        return RoadManager.getInstance().queryRoad(roadID, direct, branch);
    }

    public ArrayList<Road> getBranch(int roadID) {
        return RoadManager.getInstance().hasBranch(roadID, true);
    }

    public boolean submitRoad(int roadID, int direct, String branch) {
        try {
            if (direct == -1) {
                ArrayList<Road> tmp;
                // 取回相同車輛ID
                if (branch.equalsIgnoreCase("-1")) {
                    tmp = queryRoad(roadID, -1);
                } else
                    tmp = queryRoad(roadID, -1, branch);

                if (tmp.size() == 0) {

                } else if (tmp.size() == 1) {
                    direct = tmp.get(0).direct;
                } else {
                    if (branch.equalsIgnoreCase("-1")) {
                        boolean hasMain = checkMain(tmp);
                        boolean hasSub = checkSub(tmp);
                        if (hasSub || hasMain && hasSub) {
                            selectDirect2(tmp);
                        } else {
                            Message msg = mHandler.obtainMessage(InnerHandler.SELECT_DIRECT, tmp);
                            msg.sendToTarget();
                        }
                    } else {
                        Message msg = mHandler.obtainMessage(InnerHandler.SELECT_DIRECT, tmp);
                        msg.sendToTarget();
                    }
                    return true;
                }
            }

            boolean ret = this.imService.changeRoad(roadID, direct, branch);
            if (!ret)
                return false;

            this.mSectionsPagerAdapter.firstPage.switchMain();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkMain(ArrayList<Road> data) {
        boolean hasMain = false;
        boolean hasSub = false;

        for (Road road : data) {
            if (road.branch.equalsIgnoreCase("0")) {
                hasMain = true;
            } else
                hasSub = true;
        }

        return hasMain;
    }

    public boolean checkSub(ArrayList<Road> data) {
        boolean hasMain = false;
        boolean hasSub = false;

        for (Road road : data) {
            if (road.branch.equalsIgnoreCase("0")) {
                hasMain = true;
            } else
                hasSub = true;
        }

        return hasSub;
    }

    public void quitSelectRoad() {
        this.mSectionsPagerAdapter.firstPage.switchMain();
    }

    public void updateBusStatus(BusStatus status) {
        this.imService.changeBusStatus(status.getValue());
    }

    /**
     * 變更勤務狀態
     *
     * @param status 狀態
     */
    public void updateDutyStatus(DutyStatus status) {
        this.imService.changeDutyStatus(status.getValue());
        if (status == DutyStatus.Stop) {
            stopWork();

            int infoID = 0;
            SystemPara.getInstance().setInfoID(infoID);
            SharedPreferencesHelper.getInstance(this).setInfoID(infoID);
        }
    }

    /**
     * 處理提示訊息
     */
    private void doNotifyMessage() {
        if (dialog == null)
            dialog = new NotifyMessageDialog(this);

        if (!dialog.isShowing() && SystemPara.getInstance().getNotifyMessageQueueSize() > 0) {
            com.hantek.ttia.protocol.a1a4.Message message = SystemPara.getInstance().getNotifyMessageQueue();

            NotifyMessage notifyMessage = (NotifyMessage) message.payload;
            dialog = new NotifyMessageDialog(this);
            dialog.setParameter(notifyMessage.action, notifyMessage.infoID, notifyMessage.information);
            dialog.setCancelable(false);
            dialog.show();

            Utility.SoundPlay(getBaseContext(), R.raw.sound2);
        }
    }

    public void gotoChooseCarStatus() {
        this.mSectionsPagerAdapter.firstPage.switchModifyCarStatus();
    }

    public void gotoChooseDutyStatus() {
        this.mSectionsPagerAdapter.firstPage.switchModifyDutyStatus();
    }

    public void gotoSelectRoad() {
        this.mSectionsPagerAdapter.firstPage.switchSelectRoad();
    }

    public boolean select(Fragment fragment) {
        if (isClickTooClosely()) return false;

        if (!isFragmentExists()) {
            selectDialog.setTargetFragment(fragment, 0);
            selectDialog.show(getSupportFragmentManager(), QuickChangeDialogFragment.TAG);
        } else {
            Log.d(TAG, "select on top.");
        }
        return true;
    }

    public boolean selectDirect(ArrayList<Road> tmp) {
        Fragment checkFragment = getSupportFragmentManager().findFragmentByTag(SelectDirectFragment.TAG);
        if (checkFragment == null) {
            selectDirectFragment.setRoad(tmp);
            selectDirectFragment.show(getSupportFragmentManager(), SelectDirectFragment.TAG);
        } else {
            Log.d(TAG, "selectDirect on top.");
        }
        return true;
    }

    public boolean selectDirect2(ArrayList<Road> tmp) {
        if (isClickTooClosely()) return false;

        Fragment checkFragment = getSupportFragmentManager().findFragmentByTag(SelectDirectFragment2.TAG);
        if (checkFragment == null) {
            selectDirectFragment2.setRoad(tmp);
            selectDirectFragment2.show(getSupportFragmentManager(), SelectDirectFragment2.TAG);
        } else {
            Log.d(TAG, "selectDirect2 on top.");
        }
        return true;
    }

    /**
     * 選擇主/支線
     *
     * @param roadID
     * @param direct
     * @param branch
     * @return
     */
    public boolean selectBranch(int roadID, int direct, String branch) {
        if (isClickTooClosely()) return false;

        if (!isFragmentExists()) {
            roadBranchDialogFragment.setData(roadID, direct, branch);
            roadBranchDialogFragment.show(getSupportFragmentManager(), RoadBranchDialogFragment.TAG);
        } else {
            Log.d(TAG, "Fragment on top.");
        }
        return true;
    }

    public void changeDirect() {
        imService.changeDirect(0);
    }

    public void changeStation(int stationID) {
        try {
            imService.changeStation(stationID);
        } catch (Exception e) {
            LogManager.write("error", "changeStation: " + stationID, null);
        }
    }

    public boolean getUITheme() {
        return SharedPreferencesHelper.getInstance(this).getUITheme().equalsIgnoreCase("day");
    }

    public void notifyMessage(int infoID, int reportType) {
        imService.replyNotifyMessage(infoID, reportType);
    }

    /**
     * 檢查fragment是否在處理中..
     *
     * @return exist
     */
    private boolean isFragmentExists() {
        Fragment prevCarTypeDialog = getSupportFragmentManager().findFragmentByTag(QuickChangeDialogFragment.TAG);
        Fragment prevSelectDirect = getSupportFragmentManager().findFragmentByTag(SelectDirectFragment.TAG);
        Fragment branchDialog = getSupportFragmentManager().findFragmentByTag(RoadBranchDialogFragment.TAG);

        return !(prevCarTypeDialog == null && prevSelectDirect == null && branchDialog == null);
    }

    /**
     * {程式保護機制}避免操作過快, 降低錯誤機率
     *
     * @return
     */
    private boolean isClickTooClosely() {
        if (Math.abs(Calendar.getInstance().getTimeInMillis() - lastClickEvent) < CLICK_INTERVAL)
            return true;

        lastClickEvent = Calendar.getInstance().getTimeInMillis();
        return false;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment_FirstTab firstPage = new Fragment_FirstTab();
        //        private Fragment_SecondTab secondPage = new Fragment_SecondTab();
        private Fragment testTab = new Fragment_TestTab();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: " + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return firstPage;
                case 1:
                    return testTab;
            }

            return firstPage;
        }

        @Override
        public int getCount() {
            // Show n total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }

            return getString(R.string.title_section1).toUpperCase(l);
        }
    }

    public class InnerHandler extends Handler {
        static final int SELECT_DIRECT = 1;

        public InnerHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SELECT_DIRECT:
                    ArrayList<Road> tmp = (ArrayList<Road>) msg.obj;
                    selectDirect(tmp);
                    break;
            }
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate the user.
     */
    public class UserLogoffTask extends AsyncTask<Void, Void, Boolean> {

        // private final String mPassword;

        UserLogoffTask(String password) {
            // mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            closeProgressDlg();
            imService.logoff();
            if (success) {
                finish();
            } else {
                Toast.makeText(getBaseContext(), "***** LOGOFF *****", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            closeProgressDlg();
        }
    }

    private void showProgressDlg(String msg) {
        closeProgressDlg();
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDlg.setMessage(msg);
        mProgressDlg.setCancelable(false);
        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDlg.setIndeterminate(true);
        mProgressDlg.show();
    }

    private void closeProgressDlg() {
        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
            mProgressDlg = null;
        }
    }
}
