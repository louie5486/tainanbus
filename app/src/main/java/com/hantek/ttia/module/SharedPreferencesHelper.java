package com.hantek.ttia.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hantek.ttia.R;
import com.hantek.ttia.module.ledutils.LEDInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesHelper {
    static final String DRIVER_ID = "driver_id";
    static final String SEQUENCE = "sequence";

    static final String ROAD_ID = "road_id";
    static final String ROAD_DIRECT = "road_direct";
    static final String ROAD_BRANCH = "road_branch";

    static final String SAVE_CONFIG = "save_config";
    static final String UI_THEME = "ui_theme";

    static final String UI_STATUS = "ui_status";

    static final String LED_INFO = "led_info";
    static final String LED_INFO_RADIUS = "led_info_radius";
    static final String LED_INFO_WELCOME = "led_info_welcome";

    static final String IN_RADIUS = "in_radius";
    static final String OUT_RADIUS = "out_radius";

    static final String VER_ADVERT = "ver_advert";
    static final String VER_ROAD = "ver_road";
    static final String VER_WELCOME = "ver_welcome";
    static final String VER_RADIUS = "ver_radius";

    static final String REPORT = "report";
    static final String ACC = "acc";
    static final String GENDER = "gender";
    static final String LANG = "lang";
    static final String USE_WELCOME = "use_welcome";
    static final String VOICE_WELCOME = "voice_welcome";

    static final String USE_STOP = "use_stop";
    static final String VOICE_STOP = "voice_stop";

    static final String RPM = "rpm";
    static final String ACCELERATE = "accelerate";
    static final String DECELERATE = "decelerate";
    static final String HALT = "halt";
    static final String MOVEMENT = "movement";

    static final String ADVERT = "advert";
    static final String ADVERT_VER = "advert_version";

    static final String AUTO_LOGIN = "auto_login";
    static final String INFO_ID = "info_id";
    static final String DUTY_STATUS = "duty_status";
    static final String BUS_STATUS = "bus_status";

    static SharedPreferencesHelper instance = new SharedPreferencesHelper();
    static Context mContext;

    public static SharedPreferencesHelper getInstance(Context context) {
        mContext = context;
        return instance;
    }

    public boolean getIDStorage() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getBoolean(mContext.getString(com.hantek.ttia.R.string.pref_key_id_storage), false);
    }

    public String getCustomerID() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(mContext.getString(com.hantek.ttia.R.string.pref_key_customer_id), "0");
    }

    public boolean setCustomerID(String id) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(mContext.getString(com.hantek.ttia.R.string.pref_key_customer_id), id);
        return edt.commit();
    }

    public String getCarID() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(mContext.getString(com.hantek.ttia.R.string.pref_key_car_id), "0");
    }

    public boolean setCarID(String carID) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(mContext.getString(com.hantek.ttia.R.string.pref_key_car_id), carID);
        return edt.commit();
    }

    public boolean getSystemIMEI() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getBoolean(mContext.getString(R.string.pref_key_use_imei), false);
    }

    public boolean setSystemIMEI(boolean value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putBoolean(mContext.getString(com.hantek.ttia.R.string.pref_key_use_imei), value);
        return edt.commit();
    }

    public String getIMEI() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(mContext.getString(R.string.pref_key_imei), mContext.getString(R.string.pref_default_imei));
    }

    public boolean setIMEI(String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(mContext.getString(com.hantek.ttia.R.string.pref_key_imei), value);
        return edt.commit();
    }

    public int getSequence() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(SEQUENCE, 0);
    }

    public boolean setSequence(int sequence) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(SEQUENCE, sequence);
        return edt.commit();
    }

    public int getConfig() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(SAVE_CONFIG, 0);
    }

    public boolean setConfig(int config) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(SAVE_CONFIG, config);
        return edt.commit();
    }

    public String getDriverID() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(DRIVER_ID, "");
    }

    public boolean setDriverID(String driverID) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(DRIVER_ID, driverID);
        return edt.commit();
    }

    public String getUITheme() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(UI_THEME, "");
    }

    public boolean setUITheme(String theme) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(UI_THEME, theme);
        return edt.commit();
    }

    public int getRoadID() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(ROAD_ID, 65535);
    }

    public int getRoadDirect() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(ROAD_DIRECT, 0);
    }

    public String getRoadBranch() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(ROAD_BRANCH, "0");
    }

    public boolean setStatus(int status) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(UI_STATUS, status);
        return edt.commit();
    }

    public int getStatus() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(UI_STATUS, 0);
    }

    public boolean setRoadData(int id, int direct, String branch) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(ROAD_ID, id);
        edt.putInt(ROAD_DIRECT, direct);
        edt.putString(ROAD_BRANCH, branch);
        return edt.commit();
    }

    public ArrayList<LEDInfo> getLEDinfo() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set tmp = new LinkedHashSet();
        tmp = settings.getStringSet(LED_INFO, tmp);
        ArrayList<LEDInfo> infoList = set2List(tmp);
        return infoList;
    }

    public boolean setLEDinfo(List<LEDInfo> info) {
        Set<String> tmp = list2Set(info);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putStringSet(LED_INFO, tmp);
        return edt.commit();
    }

    public ArrayList<LEDInfo> getLEDinfoRadius() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set tmp = new LinkedHashSet();
        tmp = settings.getStringSet(LED_INFO_RADIUS, tmp);
        ArrayList<LEDInfo> infoList = set2List(tmp);
        return infoList;
    }

    public boolean setLEDinfoRadius(List<LEDInfo> info) {
        Set<String> tmp = list2Set(info);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putStringSet(LED_INFO_RADIUS, tmp);
        return edt.commit();
    }

    public ArrayList<LEDInfo> getLEDinfoWelcome() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set tmp = new LinkedHashSet();
        tmp = settings.getStringSet(LED_INFO_WELCOME, tmp);
        ArrayList<LEDInfo> infoList = set2List(tmp);
        return infoList;
    }

    public boolean setLEDinfoWelcome(List<LEDInfo> info) {
        Set<String> tmp = list2Set(info);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putStringSet(LED_INFO_WELCOME, tmp);
        return edt.commit();
    }

    public Version getAdvertVersion() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String data = settings.getString(VER_ADVERT, "");
        if (data.equalsIgnoreCase(""))
            return null;
        return Version.parse(data);
    }

    public boolean setAdvertVersion(Version version) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(VER_ADVERT, version.getSharedPrefFormat());
        return edt.commit();
    }

    public Version getRoadVersion() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String data = settings.getString(VER_ROAD, "");
        if (data.equalsIgnoreCase(""))
            return null;
        return Version.parse(data);
    }

    public boolean setRoadVersion(Version version) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(VER_ROAD, version.getSharedPrefFormat());
        return edt.commit();
    }

    public Version getWelcomeVersion() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String data = settings.getString(VER_WELCOME, "");
        if (data.equalsIgnoreCase(""))
            return null;
        return Version.parse(data);
    }

    public boolean setWelcomeVersion(Version version) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(VER_WELCOME, version.getSharedPrefFormat());
        return edt.commit();
    }

    public Version getRadiusVersion() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String data = settings.getString(VER_RADIUS, "");
        if (data.equalsIgnoreCase(""))
            return null;
        return Version.parse(data);
    }

    public boolean setRadiusVersion(Version version) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(VER_RADIUS, version.getSharedPrefFormat());
        return edt.commit();
    }

    public Radius getInRadius() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String data = settings.getString(IN_RADIUS, "");
        if (data.equalsIgnoreCase(""))
            return null;
        return Radius.parse(data);
    }

    public boolean setInRadius(Radius radius) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(IN_RADIUS, radius.getSharedPrefFormat());
        return edt.commit();
    }

    public Radius getOutRadius() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String data = settings.getString(OUT_RADIUS, "");
        if (data.equalsIgnoreCase(""))
            return null;
        return Radius.parse(data);
    }

    public boolean setOutRadius(Radius radius) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(OUT_RADIUS, radius.getSharedPrefFormat());
        return edt.commit();
    }

    public int getReport() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(REPORT, 0);
    }

    public boolean setReport(int report) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(REPORT, report);
        return edt.commit();
    }

    public int getACC() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(ACC, 0);
    }

    public boolean setACC(int acc) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(ACC, acc);
        return edt.commit();
    }

    public String getGender() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(GENDER, "");
    }

    public boolean setGender(String gender) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(GENDER, gender);
        return edt.commit();
    }

    public String getLang() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(LANG, "");
    }

    public boolean setLang(String lang) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(LANG, lang);
        return edt.commit();
    }

    public int getRPM() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(RPM, 0);
    }

    public boolean setRPM(int rpm) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(RPM, rpm);
        return edt.commit();
    }

    public int getAccelerate() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(ACCELERATE, 0);
    }

    public boolean setAccelerate(int accelerate) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(ACCELERATE, accelerate);
        return edt.commit();
    }

    public int getDecelerate() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(DECELERATE, 0);
    }

    public boolean setDecelerate(int decelerate) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(DECELERATE, decelerate);
        return edt.commit();
    }

    public int getHalt() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(HALT, 0);
    }

    public boolean setHalt(int halt) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(HALT, halt);
        return edt.commit();
    }

    public int getUSE_WELCOME() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(USE_WELCOME, 0);
    }

    public boolean setUSE_WELCOME(int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(USE_WELCOME, value);
        return edt.commit();
    }

    public int getUSE_STOP() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(USE_STOP, 0);
    }

    public boolean setUSE_STOP(int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(USE_STOP, value);
        return edt.commit();
    }

    public String getVOICE_WELCOME() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(VOICE_WELCOME, "");
    }

    public boolean setVOICE_WELCOME(String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(VOICE_WELCOME, value);
        return edt.commit();
    }

    public String getVOICE_STOP() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(VOICE_STOP, "");
    }

    public boolean setVOICE_STOP(String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(VOICE_STOP, value);
        return edt.commit();
    }

    public int getMovement() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(MOVEMENT, 0);
    }

    public boolean setMovement(int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(MOVEMENT, value);
        return edt.commit();
    }

    public String getAdvert() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(ADVERT, "");
    }

    public boolean setAdvert(String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putString(ADVERT, value);
        return edt.commit();
    }

    public boolean getAutoLogin() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getBoolean(AUTO_LOGIN, false);
    }

    public boolean setAutoLogin(boolean autoLogin) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putBoolean(AUTO_LOGIN, autoLogin);
        return edt.commit();
    }

    public int getInfoID() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(INFO_ID, 0);
    }

    public boolean setInfoID(int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(INFO_ID, value);
        return edt.commit();
    }

    public int getDutyStatus() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(DUTY_STATUS, 1); // 1:ready
    }

    public boolean setDutyStatus(int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(DUTY_STATUS, value);
        return edt.commit();
    }

    public int getBusStatus() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getInt(BUS_STATUS, 1); // 1:ready
    }

    public boolean setBusStatus(int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edt = settings.edit();
        edt.putInt(BUS_STATUS, value);
        return edt.commit();
    }

    public void exportXML() {
        try {
            if (mContext == null)
                return;
            String pack_name = mContext.getPackageName();
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + pack_name + "//shared_prefs//" + pack_name + "_preferences.xml";

                String backupDBPath = pack_name + "_preferences.xml";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(mContext, "Backup xml Successful !", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Backup Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private Set<String> list2Set(List<LEDInfo> info) {
        Set<String> tmp = new LinkedHashSet<>();
        for (LEDInfo s : info) {
            tmp.add(s.getStorageFormat());
            System.out.println("setLEDinfo: " + s);
        }

        Iterator i = tmp.iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            System.out.println("checkLEDinfo: " + s);
        }
        return tmp;
    }

    private ArrayList<LEDInfo> set2List(Set tmp) {
        ArrayList<LEDInfo> infoList = new ArrayList<>();
        Iterator i = tmp.iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            LEDInfo info = LEDInfo.parse(s);
            infoList.add(info);
            System.out.println("getLEDinfo: " + info.toString());
        }
        return infoList;
    }
}
