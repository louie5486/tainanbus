package com.hantek.ttia.module;

import android.content.Context;

import android.util.Log;

import component.LogManager;
import funtoro.coord.CoordManager;
import funtoro.mcu.IDiDo;
import funtoro.mcu.IDiDo.OnDiDoListener;
import funtoro.mcu.McuCtrlManager;

/**
 * 5668  controller
 */
public class DioController implements OnDiDoListener {
    private static final String TAG = DioController.class.getName();
    private static final String IO_NAME = "mcu_ctrl";
    private static DioController dio = new DioController();

    private boolean is5668 = false;

    private boolean accState;
    private boolean diHi1;
    private boolean diHi2;
    private boolean diHi3;
    private boolean diHi4;


    private McuCtrlManager MCM = null;
    private IDiDo dido = null;

    public static DioController getInstance() {
        return dio;
    }

    public boolean start(Context context) {
        try {
            this.MCM = (McuCtrlManager) context.getSystemService(IO_NAME);
            this.dido = (IDiDo) MCM.getIDiDoImpl();
            this.init();

            this.dido.setDiDoListener(this);
            is5668 = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Acc Using 5668:" + is5668);
        return false;
    }

    public void setDevice(boolean using5668) {
        this.is5668 = using5668;
    }

    public boolean getAccStatus(boolean query) {
        try {
            if (this.dido != null) {
                if (query)
                    this.accState = convertAcc(this.dido.getACCState());

                // Log.d(TAG, "Get Acc Status: " + this.accState);
                return this.accState;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            Log.d(TAG, "Get Acc Status fail: " + this.accState);
        }

        return true;
    }

    void init() {
        getIO(1);
        getIO(2);
        getIO(3);
        getIO(4);
        if (this.dido != null) {
            this.accState = convertAcc(this.dido.getACCState());
        }
    }

    void getIO(int index) {
        if (this.dido != null) {
            boolean tmpStatus = convertDio(this.dido.getDIState(index));
            switch (index) {
                case 1:
                    this.diHi1 = tmpStatus;
                    break;
                case 2:
                    this.diHi2 = tmpStatus;
                    break;
                case 3:
                    this.diHi3 = tmpStatus;
                    break;
                case 4:
                    this.diHi4 = tmpStatus;
                    break;
            }
        }
    }

    public boolean getDiHi(int index) {
        if (!is5668)
            return false;

        if (this.dido != null) {
            switch (index) {
                case 1:
                    return this.diHi1;
                case 2:
                    return this.diHi2;
                case 3:
                    return this.diHi3;
                case 4:
                    return this.diHi4;
            }
        }

        return false;
    }

    public boolean powerOnDisable() {
        if (this.MCM == null)
            return false;

        String response = "";
        try {
            response = this.MCM.execCommand("#PWR_ON_REQ=DISABLE\r\n", "#PWR_ON_REQ=");
            Log.d(TAG, "Acc powerOnDisable response=" + response);
            if (response.equalsIgnoreCase("#PWR_ON_REQ=INACTIVE"))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean powerOnEnable() {
        // restore it.
        if (this.MCM == null)
            return false;

        String response = "";
        try {
            response = this.MCM.execCommand("#PWR_ON_REQ=ENABLE\r\n", "#PWR_ON_REQ=");
            Log.d(TAG, "Acc powerOnEnable response=" + response);
            if (response.equalsIgnoreCase("#PWR_ON_REQ=ACTIVE"))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * set delay time to shutdown Android system.
     *
     * @param delayMinuteTime
     * @return
     */
    public boolean powerTurnOff(int delayMinuteTime) {
        if (this.MCM == null)
            return false;

        String response = "";
        try {
            String cmd = String.format("#POWER TURN OFF=DISABLE,%04d\r\n", delayMinuteTime);
            // 0002 = 2minutes
            response = this.MCM.execCommand(cmd, "#KEEP POWER ON=");
            LogManager.write("debug", "acc,powerTurnOff cmd=" + cmd + ", response=" + response, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getTabletMcuVersion() {
        if (this.MCM == null)
            return "error";

        String response = "";
        try {
            // Tablet mcu
            response = this.MCM.execCommand("#SILICON MCU FW=?\r\n", "#SILICON MCU FW=");
            Log.d(TAG, "TabletMcuVersion resp=" + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String getCradleMcuVersion() {
        if (this.MCM == null)
            return "error";

        String response = "";
        try {
            // Cradle mcu
            response = this.MCM.execCommand("#ms56652 MCU Ap FW=?\r\n", "#MCU Ap FW=");
            Log.d(TAG, "CradleMcuVersion resp=" + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void onACCEvent(int arg0) {
        Log.d(TAG, "ACC Event Status: " + arg0);
        this.accState = convertAcc(arg0);
    }

    @Override
    public void onDIStateUpdated(int index, int state) {
        Log.d(TAG, String.format("DI State Updated index=%s, state=%s.", index, state));
        if (index == 1) {
            this.diHi1 = convertDio(state);
        } else if (index == 2) {
            this.diHi2 = convertDio(state);
        }
    }

    public void shutdown(Context context) {
        CoordManager CM = (CoordManager) context.getSystemService("coord");

        // shutdown system API
        CM.shutdownSystem();
    }

    public boolean is5668() {
        return is5668;
    }

    private boolean convertAcc(int state) {
        return state == 1;
    }

    private boolean convertDio(int state) {
        return state == 1;
    }
}
