package component;

import android.location.Location;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by louie on 2013/12/17.
 */
public class gps_test implements Observer {
    private String gpsTime = "010000";
    private String gpsDate = "20011010";
    private String Lat = "0";
    private String Lng = "0";
    private String LatStr = "N";
    private String LngStr = "E";
    private String velocity = "0";
    private String altitude = "0";
    private boolean is_valid = false;
    private int sat_num;
    private DateTime day_tuil = new DateTime();
    private DecimalFormat dec_util = new DecimalFormat("000");
    private NumberFormat formatter1 = new DecimalFormat("00.00000");
    private NumberFormat formatter2 = new DecimalFormat("000.00000");
    private boolean is_readyUSE = false;
    private Location now_location = null;
    private Date gps_d = new Date();

    public Location getNow_location() {
        return now_location;
    }

    public void setNow_location(Location now_location) {
        this.now_location = now_location;
    }

    private String locationToString(Location location){
        String p;
        p = String.format("緯度：%s\n經度：%s\n精度：%s\n標高：%s\n時間：%s\n速度：%s\n方位：%s\n",
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                location.getAltitude(),
                location.getTime(),
                location.getSpeed(),
                location.getBearing());
        return p;
    }
    @Override
    public void update(Observable observable, Object o) {
        if (o == null)return;
        String gps_tmp;
        int speed = 0;
        try{
            if (Location.class.isInstance(o)){
                Location lo = (Location)o;
                now_location = lo;
                gps_d.setTime(lo.getTime());
                gps_tmp = day_tuil.addHour_UTC(gps_d,0);
                gpsDate = gps_tmp.substring(0, 8);
                gpsTime = gps_tmp.substring(8);
                if (lo.getLatitude()<0){
                    LatStr = "S";
                }else{
                    LatStr = "N";
                }
                if (lo.getLongitude()<0){
                    LngStr = "W";
                }else{
                    LngStr = "E";
                }
                Lat = formatter1.format(lo.getLatitude());
                Lng = formatter2.format(lo.getLongitude());

                //1 m/s = 1.944 kt = 3.6 km/h = 2.237 mph = 39.37 in/s
                //1KT(knot) = 1.852 km/h = 0.5144 m/s
//                if (lo.getSpeed()>0){
//                    speed = (int)(lo.getSpeed()*1.944); // kt
//                    speed = (int)(lo.getSpeed()*1.944*1.852);         // km/h
//                }
//                velocity = dec_util.format(speed);
                altitude = dec_util.format(lo.getBearing());
//                Log.d("gps_debug ", " speed:  " + lo.getSpeed() + " " +speed + " altitude: " + lo.getBearing());
//                DebugParam.getInstance().addMessage(" speed:  " + lo.getSpeed() + " " +speed + " altitude: " + lo.getBearing());
                is_readyUSE = true;
//                this.setIs_valid(true);
//                toFormatString();
//                Log.d("gps_debug ",locationToString(lo));
            }
        }catch(Exception x){
            x.printStackTrace();
        }

    }

    public void toFormatString(){
        StringBuilder str_builder = new StringBuilder();
        try{
            str_builder.append(this.gpsDate).append(",");
            str_builder.append(this.gpsTime).append(",");
            str_builder.append((this.is_valid==true?"1":"0")).append(",");
            str_builder.append(this.Lat).append(",");
            str_builder.append(this.Lng).append(",");
            str_builder.append(this.altitude).append(",");
            str_builder.append(this.velocity).append(",");
            str_builder.append(this.sat_num);
            Log.d("gps_debug", str_builder.toString());
        }catch(Exception x){x.printStackTrace();}
    }

    public boolean isIs_readyUSE() {
        return is_readyUSE;
    }

    public int getSat_num() {
        return sat_num;
    }

    public void setSat_num(int sat_num) {
        this.sat_num = sat_num;
        if (this.sat_num < 4){
            this.setIs_valid(false);
        }else{
            this.setIs_valid(true);
        }
//        Log.d("gps_debug_sate:", ""+sat_num);
//        this.toFormatString();
    }

    public boolean isIs_valid() {
        return is_valid;
    }

    public void setIs_valid(boolean is_valid) {
        this.is_valid = is_valid;
        if (!this.isIs_valid()){
            this.velocity = "0";
        }
//        toFormatString();
    }

    public String getGpsTime() {
        return gpsTime;
    }

    public String getGpsDate() {
        return gpsDate;
    }

    public String getLat() {
        return Lat;
    }

    public String getLng() {
        return Lng;
    }

    public String getVelocity() {
        return (velocity == null? "0": Integer.valueOf(this.velocity).toString());
    }

    public void setAngle(String alt) {
        this.altitude = alt;
    }

    public String getAngle(){
        return (altitude == null? "0": Integer.valueOf(this.altitude).toString());
    }

    public int getSpeeed(){
        return (this.velocity == null? 0 : Integer.parseInt(this.velocity));
    }

    //NMEA setting
    public void setVelocity(String ve) {
        int speed = 0;
        try{
            if (ve == null || ve.length()==0)return;
            float sp = Float.parseFloat(ve);
            if (sp > 0){
                speed = (int)(sp * 1.852);
            }
        }catch(Exception x){
            x.printStackTrace();
            return;
        }
        this.velocity = String.valueOf(speed);
//        Log.d("gps_debug ", " speed:  " + ve + " " +this.velocity);
//        DebugParam.getInstance().addMessage(" speed:  " + ve + " " +this.velocity);
    }

    public String getAltitude() {
        return altitude;
    }

    public String getLatStr() {
        return LatStr;
    }

    public String getLngStr() {
        return LngStr;
    }

    public void setGps_d(long tt) {
        String gps_tmp;
        try{
            gps_d.setTime(tt);
            gps_tmp = day_tuil.addHour_UTC(gps_d,0);
            gpsDate = gps_tmp.substring(0, 8);
            gpsTime = gps_tmp.substring(8);
//            this.toFormatString();
        }catch(Exception x){}
    }

    public Date getGps_d() {
        return gps_d;
    }
}
