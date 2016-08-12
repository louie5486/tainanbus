package com.hantek.ttia.module;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class Phone extends PhoneStateListener {
    static final String TAG = Phone.class.getName();

    private static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;

    private static final int SIGNAL_STRENGTH_POOR = 1;

    private static final int SIGNAL_STRENGTH_MODERATE = 2;

    private static final int SIGNAL_STRENGTH_GOOD = 3;

    private static final int SIGNAL_STRENGTH_GREAT = 4;

    private static int signalStrengths = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
    private static int phoneStatus = TelephonyManager.CALL_STATE_IDLE;

    private static Phone phone = null;
    private TelephonyManager telephonyManager;

    public static Phone getInstance() {
        if (phone == null) {
            synchronized (Phone.class) {
                if (phone == null) {
                    phone = new Phone();
                }
            }
        }

        return phone;
    }

    public void startListener(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int event = LISTEN_SIGNAL_STRENGTHS | LISTEN_CALL_STATE | LISTEN_DATA_CONNECTION_STATE;
        telephonyManager.listen(this, event);
    }

    public void stopListener() {
        telephonyManager.listen(this, LISTEN_NONE);
    }

    public int getPhoneStatus() {
        return phoneStatus;
    }

    public int getSignalStrengths() {
        if (getSimCard() == TelephonyManager.SIM_STATE_ABSENT || getSimCard() == TelephonyManager.SIM_STATE_UNKNOWN)
            return 0;

        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE)
            return 0;

        return signalStrengths;
    }

    public int getSimCard() {
        return telephonyManager.getSimState();
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        phoneStatus = state;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        StringBuffer sb = new StringBuffer();
        sb.append("getCdmaDbm=" + signalStrength.getCdmaDbm());
        sb.append(", getCdmaEcio=" + signalStrength.getCdmaEcio());
        sb.append(", getEvdoDbm=" + signalStrength.getEvdoDbm());
        sb.append(", getEvdoEcio=" + signalStrength.getEvdoEcio());
        sb.append(", getEvdoSnr=" + signalStrength.getEvdoSnr());
        sb.append(", getGsmBitErrorRate=" + signalStrength.getGsmBitErrorRate());
        sb.append(", getGsmSignalStrength=" + signalStrength.getGsmSignalStrength());
        Log.d(TAG, sb.toString());
//        getLevel(signalStrength);
        signalStrengths = getLevelMethod(signalStrength);
    }

//    public int getLevel(SignalStrength signalStrength) {
//        int level;
//
//        if (signalStrength.isGsm()) {
//            level = getLteLevel(signalStrength);
//            if (level == SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
//                level = getGsmLevel(signalStrength);
//            }
//        } else {
//            int cdmaLevel = getCdmaLevel(signalStrength);
//            int evdoLevel = getEvdoLevel(signalStrength);
//            if (evdoLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
//                /* We don't know evdo, use cdma */
//                level = cdmaLevel;
//            } else if (cdmaLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
//                /* We don't know cdma, use evdo */
//                level = evdoLevel;
//            } else {
//                /* We know both, use the lowest level */
//                level = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
//            }
//        }
////        if (DBG) log("getLevel=" + level);
//        return level;
//    }

    public int getCdmaLevel(SignalStrength signalStrength) {
        final int cdmaDbm = signalStrength.getCdmaDbm();
        final int cdmaEcio = signalStrength.getCdmaEcio();
        int levelDbm;
        int levelEcio;

        if (cdmaDbm >= -75) levelDbm = SIGNAL_STRENGTH_GREAT;
        else if (cdmaDbm >= -85) levelDbm = SIGNAL_STRENGTH_GOOD;
        else if (cdmaDbm >= -95) levelDbm = SIGNAL_STRENGTH_MODERATE;
        else if (cdmaDbm >= -100) levelDbm = SIGNAL_STRENGTH_POOR;
        else levelDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        // Ec/Io are in dB*10
        if (cdmaEcio >= -90) levelEcio = SIGNAL_STRENGTH_GREAT;
        else if (cdmaEcio >= -110) levelEcio = SIGNAL_STRENGTH_GOOD;
        else if (cdmaEcio >= -130) levelEcio = SIGNAL_STRENGTH_MODERATE;
        else if (cdmaEcio >= -150) levelEcio = SIGNAL_STRENGTH_POOR;
        else levelEcio = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        int level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
//        if (DBG) log("getCdmaLevel=" + level);
        return level;
    }

    public int getEvdoLevel(SignalStrength signalStrength) {
        int evdoDbm = signalStrength.getEvdoDbm();
        int evdoSnr = signalStrength.getEvdoSnr();
        int levelEvdoDbm;
        int levelEvdoSnr;

        if (evdoDbm >= -65) levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
        else if (evdoDbm >= -75) levelEvdoDbm = SIGNAL_STRENGTH_GOOD;
        else if (evdoDbm >= -90) levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
        else if (evdoDbm >= -105) levelEvdoDbm = SIGNAL_STRENGTH_POOR;
        else levelEvdoDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        if (evdoSnr >= 7) levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
        else if (evdoSnr >= 5) levelEvdoSnr = SIGNAL_STRENGTH_GOOD;
        else if (evdoSnr >= 3) levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
        else if (evdoSnr >= 1) levelEvdoSnr = SIGNAL_STRENGTH_POOR;
        else levelEvdoSnr = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
//        if (DBG) log("getEvdoLevel=" + level);
        return level;
    }

//    public int getLteLevel(SignalStrength signalStrength) {
//        /*
//         * TS 36.214 Physical Layer Section 5.1.3 TS 36.331 RRC RSSI = received
//         * signal + noise RSRP = reference signal dBm RSRQ = quality of signal
//         * dB= Number of Resource blocksxRSRP/RSSI SNR = gain=signal/noise ratio
//         * = -10log P1/P2 dB
//         */
//        int rssiIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN, rsrpIconLevel = -1, snrIconLevel = -1;
//        int mLteRsrp = getLteRsrpMethod(signalStrength);
//
//        if (mLteRsrp > -44) rsrpIconLevel = -1;
//        else if (mLteRsrp >= -85) rsrpIconLevel = SIGNAL_STRENGTH_GREAT;
//        else if (mLteRsrp >= -95) rsrpIconLevel = SIGNAL_STRENGTH_GOOD;
//        else if (mLteRsrp >= -105) rsrpIconLevel = SIGNAL_STRENGTH_MODERATE;
//        else if (mLteRsrp >= -115) rsrpIconLevel = SIGNAL_STRENGTH_POOR;
//        else if (mLteRsrp >= -140) rsrpIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
//
//        int mLteRssnr = getLteRssnr(signalStrength);
//        /*
//         * Values are -200 dB to +300 (SNR*10dB) RS_SNR >= 13.0 dB =>4 bars 4.5
//         * dB <= RS_SNR < 13.0 dB => 3 bars 1.0 dB <= RS_SNR < 4.5 dB => 2 bars
//         * -3.0 dB <= RS_SNR < 1.0 dB 1 bar RS_SNR < -3.0 dB/No Service Antenna
//         * Icon Only
//         */
//        if (mLteRssnr > 300) snrIconLevel = -1;
//        else if (mLteRssnr >= 130) snrIconLevel = SIGNAL_STRENGTH_GREAT;
//        else if (mLteRssnr >= 45) snrIconLevel = SIGNAL_STRENGTH_GOOD;
//        else if (mLteRssnr >= 10) snrIconLevel = SIGNAL_STRENGTH_MODERATE;
//        else if (mLteRssnr >= -30) snrIconLevel = SIGNAL_STRENGTH_POOR;
//        else if (mLteRssnr >= -200)
//            snrIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
//
////        if (DBG) log("getLTELevel - rsrp:" + mLteRsrp + " snr:" + mLteRssnr + " rsrpIconLevel:"
////                + rsrpIconLevel + " snrIconLevel:" + snrIconLevel);
//
//        /* Choose a measurement type to use for notification */
//        if (snrIconLevel != -1 && rsrpIconLevel != -1) {
//            /*
//             * The number of bars displayed shall be the smaller of the bars
//             * associated with LTE RSRP and the bars associated with the LTE
//             * RS_SNR
//             */
//            return (rsrpIconLevel < snrIconLevel ? rsrpIconLevel : snrIconLevel);
//        }
//
//        if (snrIconLevel != -1) return snrIconLevel;
//
//        if (rsrpIconLevel != -1) return rsrpIconLevel;
//
//        int mLteSignalStrength = 99;
//        /* Valid values are (0-63, 99) as defined in TS 36.331 */
//        if (mLteSignalStrength > 63) rssiIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
//        else if (mLteSignalStrength >= 12) rssiIconLevel = SIGNAL_STRENGTH_GREAT;
//        else if (mLteSignalStrength >= 8) rssiIconLevel = SIGNAL_STRENGTH_GOOD;
//        else if (mLteSignalStrength >= 5) rssiIconLevel = SIGNAL_STRENGTH_MODERATE;
//        else if (mLteSignalStrength >= 0) rssiIconLevel = SIGNAL_STRENGTH_POOR;
////        if (DBG) log("getLTELevel - rssi:" + mLteSignalStrength + " rssiIconLevel:"
////                + rssiIconLevel);
//        return rssiIconLevel;
//    }

    private int getGsmLevel(SignalStrength signalStrength) {
        int level = 0;

        // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
        // asu = 0 (-113dB or less) is very weak
        // signal, its better to show 0 bars to the user in such cases.
        // asu = 99 is a special case, where the signal strength is unknown.
        int asu = signalStrength.getGsmSignalStrength();
        if (asu <= 2 || asu == 99)
            level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        else if (asu >= 12)
            level = SIGNAL_STRENGTH_GREAT;
        else if (asu >= 8)
            level = SIGNAL_STRENGTH_GOOD;
        else if (asu >= 5)
            level = SIGNAL_STRENGTH_MODERATE;
        else
            level = SIGNAL_STRENGTH_POOR;
        // if (DBG) log("getGsmLevel=" + level);
        Log.d(TAG, "asu=" + asu);
        return level;
    }

    private int getLevelMethod(SignalStrength signalStrength) {
        Class signalStrengthClass = signalStrength.getClass();
        try {
            Method method = signalStrengthClass.getMethod("getLevel", null);
            method.setAccessible(true);
            Integer bars = (Integer) method.invoke(signalStrength, (Object[]) null);
            Log.d(TAG, "getLevel=" + bars);
            return bars;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

//    private int getLteRsrpMethod(SignalStrength signalStrength) {
//        Class signalStrengthClass = signalStrength.getClass();
//        try {
//            Method method = signalStrengthClass.getMethod("getLteRsrp", null);
//            method.setAccessible(true);
//            Integer bars = (Integer) method.invoke(signalStrength, (Object[]) null);
//            Log.d(TAG, "getLteRsrp=" + bars);
//            return bars;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return -140;
//    }

//    private int getLteRssnr(SignalStrength signalStrength) {
//        Class signalStrengthClass = signalStrength.getClass();
//        try {
//            Method method = signalStrengthClass.getMethod("getLteRssnr", null);
//            method.setAccessible(true);
//            Integer bars = (Integer) method.invoke(signalStrength, (Object[]) null);
//            Log.d(TAG, "getLteRssnr=" + bars);
//            return bars;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return -200;
//    }
}
