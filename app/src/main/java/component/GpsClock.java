package component;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by louie on 2014/2/6.
 */
public class GpsClock extends TimerTask {
    private static GpsClock instance;
    private Date time;
    private static Timer ptime = new Timer();
    private long long_time;
    private boolean valid = false;
    private long offset_time;
    private boolean correct_time = true;
    private boolean use_systemTime = true;
    private SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmmss");

    public static GpsClock getInstance(){
        if (instance == null){
            instance = new GpsClock();
            ptime.schedule(instance,1000,1000);
        }
        return instance;
    }

    public GpsClock(){
        time = Calendar.getInstance().getTime();
        if (use_systemTime)valid = true;
    }

    @Override
    public void run() {
        if (!use_systemTime)
        runTime();
    }



    public synchronized void runTime(){
        try{
            if (valid){
                long_time = time.getTime();
                long_time = long_time + 1000;
                time.setTime(long_time);
                offset_time =  long_time - Calendar.getInstance().getTimeInMillis();
                correct_time = true;
            }else{
                if (correct_time){
                    long_time = Calendar.getInstance().getTimeInMillis() + offset_time;
                    time.setTime(long_time);
                }else{
                    time = Calendar.getInstance().getTime();
                }
            }
        }catch(Exception x){}
    }

    public Date getTime() {
        if (use_systemTime)return Calendar.getInstance().getTime();
        return time;
    }

    public Date getless_24Time() {
        Calendar p = Calendar.getInstance();
        if (!use_systemTime)
        p.setTime(time);
        p.add(Calendar.HOUR, - 48);
        return p.getTime();
    }

    public Date getless_Time(int n) {
        Calendar p = Calendar.getInstance();
        if (!use_systemTime)
            p.setTime(time);
        p.add(Calendar.HOUR, - n);
        return p.getTime();
    }

    public synchronized void setTime(Date time) {
        if (use_systemTime){
            this.time = Calendar.getInstance().getTime();
            return;
        }
        this.time = time;
        valid = true;
//        try{
//            Calendar.getInstance().setTime(time);
//
//            SystemClock.setCurrentTimeMillis(time.getTime());
//        }catch(Exception x){x.printStackTrace();}
    }

//    public void systemSet_OffsetTime(Context context) {
//        try {
//            System.out.println("GpsClock offset Setting- " + offset_time);
//            if (!valid)return;
//            correct_time = true;
//            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//            SharedPreferences.Editor edt = settings.edit();
//            edt.putString("barcode", SystemParam.getInstance().getEmployee());
//            if (use_systemTime){
//                edt.putLong("offsettime", 0);
//            }else{
//                edt.putLong("offsettime", offset_time);
//            }
//            edt.putString("closetime", simDate.format(getTime()));
//            edt.putBoolean("auto_login", true);
//            edt.commit();
//        } catch (Exception e) {
//            Log.e("SetSharedPref", e.getMessage());
//        }
//    }

    public long getOffset_time() {
        return offset_time;
    }

    public void setOffset_time(long offset_time) {
        if (offset_time == -1)return;
        this.offset_time = offset_time;
        correct_time = true;
    }

    public boolean isCorrect_time() {
        return correct_time;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isUse_systemTime() {
        return use_systemTime;
    }

    public synchronized void setUse_systemTime(boolean use_systemTime) {
        this.use_systemTime = use_systemTime;
        if (use_systemTime){
            correct_time = true;
            this.time = Calendar.getInstance().getTime();
        }
    }
}
