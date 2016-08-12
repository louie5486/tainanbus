package com.hantek.ttia.module;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.telephony.TelephonyManager;

public class Utility {
    private static MediaPlayer mp = null;

    public static void SoundPlay(Context context, int resID) {
        try {
            if (mp != null)
                mp.release();

            mp = MediaPlayer.create(context, resID);
            mp.start();
        } catch (Exception x) {
        }
    }

//    public static boolean SoundPlay(Context context, String filePath) {
//        try {
//            if (mp != null)
//                mp.release();
//
//            mp = MediaPlayer.create(context, Uri.parse(filePath));
//            Log.d("Player", mp.getCurrentPosition() + " / " + mp.getDuration());
//            mp.start();
//            return true;
//        } catch (Exception x) {
//            return false;
//        }
//    }

    public static MediaPlayer soundPlay(Context context, String filePath) {
        try {
            if (mp != null)
                mp.release();

            mp = MediaPlayer.create(context, Uri.parse(filePath));
            mp.start();
            return mp;
        } catch (Exception x) {
            return null;
        }
    }

    public static MediaPlayer getPlay() {
        return mp;
    }

    // public static void disableKeyguard(Context context) {
    // KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    // KeyguardManager.KeyguardLock mLock = keyguardManager.newKeyguardLock("");
    // mLock.disableKeyguard();
    // }

    // public static void setUnlock(Activity activity) {
    // Window win = activity.getWindow();
    // WindowManager.LayoutParams winParams = win.getAttributes();
    // winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
    // WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    // win.setAttributes(winParams);
    // }

    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        if (imei == null)
            return "";
        return imei;
    }

    public static String getIMSI(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = mTelephonyManager.getSubscriberId();
        if (imsi == null)
            return "";
        return imsi;
    }

    /**
     * Degress * 100 + minutes 也就是 度 * 100 + 分(TTIA)
     *
     * @param gps
     * @return
     */
    public static byte nmeaToDu(String gps) {
        try {
            return (byte) (Double.parseDouble(gps) / 100d);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * (TTIA)
     *
     * @param gps
     * @return
     */
    public static byte nmeaToFen(String gps) {
        try {
            double s1 = Double.parseDouble(gps);
            int s2 = (int) (Double.parseDouble(gps) / 100d) * 100;
            int s3 = (int) (s1 - s2);
            return (byte) s3;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * (TTIA) 取得分的小數
     *
     * @param gps
     * @return
     */
    public static int nmeaToMiao(String gps) {
        try {
            String[] bList = gps.split("\\.");
            String miao = bList[1];
            while (miao.length() < 4)
                miao += "0";//右補0
            miao = miao.substring(0, 4);//取4碼
            return Integer.valueOf(miao);
        } catch (Exception e) {
            return 0;
        }
    }
//    public static int nmeaToMiao(String gps) {
//        try {
//            double d = Double.parseDouble(gps);
//            DecimalFormat df = new DecimalFormat("0.0000");
//            String tmp = df.format(d);
//            String[] aList = tmp.split("\\.");
//            return Integer.parseInt(aList[1]);
//        } catch (Exception e) {
//            return 99;
//        }
//    }

    public static long dateDiff(Date current, Date previous) {
        return current.getTime() - previous.getTime();
    }

    /**
     * @return TimeInMillis
     */
    public static long dateDiff(long currentTimeInMillis, long previousTimeInMillis) {
        return currentTimeInMillis - previousTimeInMillis;
    }

    /**
     * @return TimeInMillis
     */
    public static long dateDiffNow(Calendar previousTime) {
        return System.currentTimeMillis() - previousTime.getTimeInMillis();
        // return Calendar.getInstance().getTimeInMillis() - previousTime.getTimeInMillis();
    }

    public static float DM2DD(String astr) {
        try {
            String[] aList;
            aList = astr.split("\\.");
            return Float.parseFloat(aList[0].substring(0, aList[0].length() - 2)) + Float.parseFloat(aList[0].substring(aList[0].length() - 2) + "." + aList[1]) / 60;
        } catch (Exception e) {
            return 0;
        }
    }

    public static double du2Nmea(double du) {
        try {
            int integer = (int) du;
            double value = du - integer;
            return integer * 100 + value * 60;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Du
     *
     * @param sLon1
     * @param sLat1
     * @param sLon2
     * @param sLat2
     * @return meter
     */
    public static double calcDistance(double sLon1, double sLat1, double sLon2, double sLat2) {
        final double sPi = 3.14159265;
        double sdy = 0.0;
        double sdx = 0.0;
        double sDeltaMi = 0.0;

        sdx = (sLon2 - sLon1) * 2 * sPi * 3960 * Math.cos(sLat1 * sPi / 180) / 360;
        sdy = (sLat2 - sLat1) * 2 * sPi * 3960 / 360;
        sDeltaMi = Math.sqrt((sdy * sdy + sdx * sdx));
        return sDeltaMi * 1609.344;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String getVersionName(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo;
        pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        String version = pInfo.versionName;
        return version;
    }

    public static String getPackageName(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo;
        pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        String version = pInfo.packageName;
        return version;
    }

    public static int getVersionCode(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo;
        pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return pInfo.versionCode;
    }

    public static Typeface getType(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/kaiu.ttf");
    }
}
