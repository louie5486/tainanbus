package com.hantek.ttia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.snowdream.android.api.autoupdate.UpdateAPI;
import com.hantek.ttia.dl.DownloadReceiver;
import com.hantek.ttia.module.SharedPreferencesHelper;
import com.hantek.ttia.module.Utility;
import com.hantek.ttia.module.handshake.RequestTask;
import com.hantek.ttia.module.handshake.ResponseManager;
import com.hantek.ttia.module.handshake.RoadRequest;
import com.hantek.ttia.protocol.a1a4.BackendMsgID;
import com.hantek.ttia.services.IService;
import com.hantek.ttia.services.SystemService;

import component.LogManager;
import tw.com.hantek.www.ota.IRemoteService;

public class LoginActivity extends Activity implements View.OnClickListener, Runnable {
    private static final String TAG = LoginActivity.class.getName();

    static final String REMOTE_PACKET = "tw.com.hantek.www.ota";
    static final String REMOTE_CLASS = "tw.com.hantek.www.ota.RemoteService";

    private static final String UPDATE_URL = "http://61.222.88.241:8089/bus/APK/update.xml";
    private static final String UPDATE_URL_CUSTOMER = "http://61.222.88.241:8089/bus/customerID/APK/update.xml";

    private TextView driverIDTextView;
    private Button loginButton, clearButton, backspaceButton;
    private Button digiButton1, digiButton2, digiButton3, digiButton4, digiButton5, digiButton6, digiButton7, digiButton8, digiButton9, digiButton0;
    private TextView car_id;
    private TextView version;

    private StringBuilder driverID = new StringBuilder();

    // logon process
    private UserLoginTask mAuthTask = null;
    private View mLoginFormView;
    private View mLoginStatusView;
    private boolean waitingLogonResult = false;

    private boolean isDone = false;
    private String theme;

    private IService imService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            imService = ((SystemService.LocalBinder) service).getService();

            Log.d(TAG, "***** onServiceConnected *****");
            if (!imService.isConfiguration()) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), SettingsActivity.class);
                startActivity(intent);
            } else {
                String cid = SharedPreferencesHelper.getInstance(LoginActivity.this).getCustomerID();
                String cid2 = SharedPreferencesHelper.getInstance(LoginActivity.this).getCarID();
                car_id.setText(cid + "-" + cid2);
                try {
                    version.setText(String.valueOf(Utility.getVersionCode(LoginActivity.this)));
                } catch (Exception e) {
                    version.setText("E00");
                }

                String id = SharedPreferencesHelper.getInstance(LoginActivity.this).getDriverID();
                if (id.equalsIgnoreCase("0"))
                    id = "";
                driverID = new StringBuilder(id);
                updateDriverID();
                String cID = SharedPreferencesHelper.getInstance(getApplication()).getCustomerID();

                if (!isDone) {
                    isDone = true;

                    RoadRequest request = new RoadRequest(LoginActivity.this, mMainHandler);
                    request.setCustomerID(cID);
                    RequestTask task = new RequestTask(LoginActivity.this, request);
                    task.execute("路線檢查");

                    imService.download(new DownloadReceiver(new Handler()));
                }

                // 2016-01-19 改為區別客運
                UpdateAPI update = new UpdateAPI(LoginActivity.this);
                update.setmUpdateUrl(UPDATE_URL_CUSTOMER.replace("customerID", cID));
                update.check();

                new Handler().post(LoginActivity.this);
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

    tw.com.hantek.www.ota.IRemoteService otaService = null;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mAIDLConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            otaService = IRemoteService.Stub.asInterface(service);
            String cID = SharedPreferencesHelper.getInstance(getApplication()).getCustomerID();
            try {
                String packageName = Utility.getPackageName(LoginActivity.this);
                otaService.add(UPDATE_URL_CUSTOMER.replace("customerID", cID), packageName);
                Log.d(TAG, "OTA Service:" + otaService.getName() + ", packageName:" + packageName);
            } catch (RemoteException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            otaService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            LogManager.write("info", "onCreate-finish: state: " + savedInstanceState, null);
            finish();
            return;
        }

        LogManager.write("info", "onCreate-finish: state: " + savedInstanceState, null);
        Log.d(TAG, "onCreate");
        // before set view
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // retrieve ui status.
        theme = SharedPreferencesHelper.getInstance(this).getUITheme();
        if (theme.equalsIgnoreCase("day"))
            this.setContentView(R.layout.activity_logon);
        else
            this.setContentView(R.layout.activity_logon_night);

        initView();

        Intent intent = new Intent();
        intent.setClass(getApplication(), SystemService.class);
        this.startService(intent);

        //  ***** launch OTA app *****
        final Intent intent0 = new Intent(REMOTE_CLASS);
        ComponentName cn = new ComponentName(REMOTE_PACKET, REMOTE_CLASS);
        intent0.setComponent(cn);
        startService(intent0);

        boolean bindOTA = bindService(intent0, mAIDLConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService:" + IRemoteService.class.getName() + ", RET:" + bindOTA);
        // ******************************
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        bindService(new Intent(this, SystemService.class), mConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        unbindService(mConnection);
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                if (login()) {
                    SharedPreferencesHelper.getInstance(this).setAutoLogin(true);
                }
                break;
            case R.id.clearButton:
                if (mAuthTask != null || waitingLogonResult)
                    return;

                this.driverID = new StringBuilder();
                this.updateDriverID();
                break;
            case R.id.backspaceButton:
                if (this.driverID.length() > 0) {
                    this.driverID.deleteCharAt(this.driverID.length() - 1);
                    this.updateDriverID();
                }
                break;
            default:
                if (mAuthTask != null || waitingLogonResult)
                    return;

                Button clickButton = (Button) v;
                this.driverID.append(clickButton.getText().toString());
                try {
                    if (verifyDriverID())
                        this.updateDriverID();
                    else
                        throw new Exception("out of range");
                } catch (Exception e) {
                    this.driverID.deleteCharAt(this.driverID.length() - 1);
                    Log.d(TAG, this.driverID.toString());
                }
                break;
        }
    }

    private void initView() {
        this.car_id = (TextView) findViewById(R.id.car_id);
        this.version = (TextView) findViewById(R.id.version);

        String cid = SharedPreferencesHelper.getInstance(LoginActivity.this).getCustomerID();
        String cid2 = SharedPreferencesHelper.getInstance(LoginActivity.this).getCarID();
        car_id.setText(cid + "-" + cid2);

        this.loginButton = (Button) findViewById(R.id.loginButton);
        this.loginButton.setOnClickListener(this);
//        this.loginButton.setTypeface(Utility.getType(getApplication()));

        this.clearButton = (Button) findViewById(R.id.clearButton);
        this.clearButton.setOnClickListener(this);

        this.backspaceButton = (Button) findViewById(R.id.backspaceButton);
        this.backspaceButton.setOnClickListener(this);

        this.driverIDTextView = (TextView) findViewById(R.id.roadTextView);
        this.driverIDTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!theme.equalsIgnoreCase("day")) {
                    setContentView(R.layout.activity_logon);
                    SharedPreferencesHelper.getInstance(LoginActivity.this).setUITheme("day");
                    theme = "day";
                    initView();
                } else {
                    setContentView(R.layout.activity_logon_night);
                    SharedPreferencesHelper.getInstance(LoginActivity.this).setUITheme("night");
                    theme = "night";
                    initView();
                }
            }
        });
        this.updateDriverID();

        this.digiButton0 = (Button) findViewById(R.id.digiButton0);
        this.digiButton1 = (Button) findViewById(R.id.digiButton1);
        this.digiButton2 = (Button) findViewById(R.id.digiButton2);
        this.digiButton3 = (Button) findViewById(R.id.digiButton3);
        this.digiButton4 = (Button) findViewById(R.id.digiButton4);
        this.digiButton5 = (Button) findViewById(R.id.digiButton5);
        this.digiButton6 = (Button) findViewById(R.id.digiButton6);
        this.digiButton7 = (Button) findViewById(R.id.digiButton7);
        this.digiButton8 = (Button) findViewById(R.id.digiButton8);
        this.digiButton9 = (Button) findViewById(R.id.digiButton9);

        this.digiButton0.setOnClickListener(this);
        this.digiButton1.setOnClickListener(this);
        this.digiButton2.setOnClickListener(this);
        this.digiButton3.setOnClickListener(this);
        this.digiButton4.setOnClickListener(this);
        this.digiButton5.setOnClickListener(this);
        this.digiButton6.setOnClickListener(this);
        this.digiButton7.setOnClickListener(this);
        this.digiButton8.setOnClickListener(this);
        this.digiButton9.setOnClickListener(this);

        this.mLoginFormView = findViewById(R.id.login_form);
        this.mLoginStatusView = findViewById(R.id.login_status);
    }

    private boolean login() {
        if (verifyDriverID()) {
            if (mAuthTask == null && !waitingLogonResult) {
                waitingLogonResult = true;
                showProgress(true);
                mAuthTask = new UserLoginTask(mMainHandler, this.driverID.toString());
                mAuthTask.execute((Void) null);
                return true;
            }
        }

        return false;
        // debug
        // DatabaseHelper.getInstance(getApplication()).exportDB();
    }

    private void updateDriverID() {
        if (this.driverID.length() == 0) {
            this.driverIDTextView.setText(R.string.msg_input_driverid);
            this.driverIDTextView.setTextAppearance(getApplicationContext(), R.style.txt_login_driver_id_warn);
        } else {
            if (theme.equalsIgnoreCase("day"))
                this.driverIDTextView.setTextAppearance(getApplicationContext(), R.style.txt_login_driver_id);
            else
                this.driverIDTextView.setTextAppearance(getApplicationContext(), R.style.txt_login_driver_id_night);
            this.driverIDTextView.setText(this.driverID.toString());
        }
    }

    private boolean verifyDriverID() {
        try {
            long tmpDriverID = Long.parseLong(this.driverID.toString());
            // protocol定義
            if (tmpDriverID >= 0 && tmpDriverID <= Math.pow(2, 32)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void run() {
        if (SharedPreferencesHelper.getInstance(this).getStatus() == 1) {
            LogManager.write("debug", "login retrieve.", null);
            // 回復狀態
            login();
        } else if (SharedPreferencesHelper.getInstance(this).getAutoLogin()) {
            LogManager.write("debug", "auto login.", null);
            // 自動登入
            login();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final int TIME_OUT_SECOND = 6;
        private int logonResult = -1; // 0:success, 1~255:fail
        private String driverID;
        private Handler mContext;

        UserLoginTask(Handler mMainHandler, String driverID) {
            mContext = mMainHandler;
            this.driverID = driverID;
            logonResult = -1;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            imService.login(driverID);

            boolean result = false;

            try {
                result = ResponseManager.getInstance().waitResponse(BackendMsgID.RegisterRequest.getValue(), TIME_OUT_SECOND * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String response = "";
            if (result) {
                response = ResponseManager.getInstance().getResponse(BackendMsgID.RegisterRequest.getValue()).toString();
            }

            waitingLogonResult = false;
            return true; // response.equalsIgnoreCase("0"); // 不管結果,直接登入.
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent();
                intent.setClass(mLoginFormView.getContext(), MainActivity.class);
                startActivity(intent);

//                Intent intent = new Intent();
//                intent.setClass(mLoginFormView.getContext(), SystemCheckActivity.class);
//                startActivity(intent);

//                mContext.sendMessage(mContext.obtainMessage(3));
            } else {
                SystemPara.getInstance().setDriverID(0);
                SharedPreferencesHelper.getInstance(getApplication()).setDriverID("");
                Toast.makeText(getBaseContext(), "login fail", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            waitingLogonResult = false;
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public final static int HDL_CONNECTED = 0;
    public final static int HDL_DISCONNECTED = 1;
    private final Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HDL_CONNECTED) {
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Intent intent = new Intent();
//                        intent.setClass(getBaseContext(), MainActivity.class);
//                        startActivity(intent);
//                    }
//                });
            } else if (msg.what == HDL_DISCONNECTED) {
//                Intent intent = new Intent();
//                intent.setClass(getBaseContext(), MainActivity.class);
//                startActivity(intent);
            } else if (msg.what == 3) {
//                RoadRequest request = new RoadRequest(mMainHandler);
//                RequestTask task = new RequestTask(LoginActivity.this, request);
//                task.execute("路線檢查");
            }
        }
    };
}