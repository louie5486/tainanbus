package component;

/**
 * Created by louie on 2013/12/16.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTime {
    public DateTime() {
    }

    public Date getDate(){
        return GpsClock.getInstance().getTime();
    }

    public Date getDate(String format){
        try{
            SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cd = Calendar.getInstance();
            cd.setTime(GpsClock.getInstance().getTime());
            cd.setTime(simDate.parse(format));
            return cd.getTime();
        }catch(Exception x){}
        return null;
    }

    public Date getDate(String value, String format){
        try{
            SimpleDateFormat simDate = new SimpleDateFormat(format);
            Calendar cd = Calendar.getInstance();
            cd.setTime(simDate.parse(value));
            return cd.getTime();
        }catch(Exception x){}
        return null;
    }

    /**
     * Transfer the DaeTime To the Format of Second
     *
     * @param dateTime
     * @return String
     */
    public long DateTimeToSec(String dateTime) {
        if (dateTime == null)
            return -1;
        long Sec = 0;
        long yy = Integer.parseInt(dateTime.substring(0, 4));
        long mon = Integer.parseInt(dateTime.substring(4, 6));
        long dd = Integer.parseInt(dateTime.substring(6, 8));
        long hh = Integer.parseInt(dateTime.substring(8, 10));
        long min = Integer.parseInt(dateTime.substring(10, 12));
        long ss = Integer.parseInt(dateTime.substring(12, 14));

        Sec = (yy * 365 * 24 * 60 * 60) + (mon * 30 * 24 * 60 * 60)
                + (dd * 24 * 60 * 60) + (hh * 60 * 60) + (min * 60) + ss;
        return Sec;
    }

    public long TimeToSec(String dateTime) {
        if (dateTime == null)
            return -1;
        long Sec = 0;
        long hh = Integer.parseInt(dateTime.substring(0, 2));
        long min = Integer.parseInt(dateTime.substring(2, 4));
        long ss = Integer.parseInt(dateTime.substring(4));

        Sec = (hh * 60 * 60) + (min * 60) + ss;
        return Sec;
    }

    public String getId_fromNow(){
        GregorianCalendar cd1 = new GregorianCalendar();
        return String.valueOf(cd1.getTimeInMillis());
    }

    public String getLocaleSystemTime(String style){
        return this.getLocaleSystemTime(style, TimeZone.getDefault());
    }

    public String getLocaleFormatTime(String time_str, String style){
        return this.getLocaleFormatTime(time_str, style, TimeZone.getDefault());
    }

    public String getLocaleFormatTime(Date tt, String style){
        return this.getLocaleFormatTime(tt, style, TimeZone.getDefault());
    }

    public String getLocaleSystemTime(String style, TimeZone zone){
        SimpleDateFormat simDate;
        try{
            if (style != null){
                simDate = new SimpleDateFormat(style);
            }else{
                simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            }
            simDate.setTimeZone(zone);
//			java.text.DateFormat dateFormat = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM,	locale);
            return simDate.format(GpsClock.getInstance().getTime());
        }catch(Exception x){x.printStackTrace();}
        return this.getSystemDate();
    }

    public String getLocaleFormatTime(String time_str, String style, TimeZone zone){
        SimpleDateFormat simDate;
        SimpleDateFormat simDate2 =  new SimpleDateFormat("yyyyMMddHHmmss");
        Date src_date;
        try{
            if (style != null){
                simDate = new SimpleDateFormat(style);
            }else{
                simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            }
            simDate2.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
            src_date = simDate2.parse(time_str);
            Calendar cd1 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
            cd1.setTime(src_date);
            simDate.setTimeZone(zone);
//			java.text.DateFormat dateFormat = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM,	locale);
            return simDate.format(cd1.getTime());
        }catch(Exception x){x.printStackTrace();}
        return this.getSystemDate();
    }

    public String getLocaleFormatTime(Date src_date, String style, TimeZone zone){
        SimpleDateFormat simDate;
        SimpleDateFormat simDate2 =  new SimpleDateFormat("yyyyMMddHHmmss");
        try{
            if (style != null){
                simDate = new SimpleDateFormat(style);
            }else{
                simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            }
            simDate2.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
            Calendar cd1 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
            cd1.setTime(src_date);
            simDate.setTimeZone(zone);
//			java.text.DateFormat dateFormat = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM,	locale);
            return simDate.format(cd1.getTime());
        }catch(Exception x){x.printStackTrace();}
        return this.getSystemDate();
    }

    /**
     * Get The System Date&Time
     *
     * @return String
     */
    public String getSystemDateTime() {
        GregorianCalendar cd1 = new GregorianCalendar();
        cd1.setTime(GpsClock.getInstance().getTime());
        String dateTime = "";
        int s_yy = cd1.get(Calendar.YEAR);
        dateTime += s_yy;
        int s_mon = (cd1.get(Calendar.MONTH) + 1);
        if (s_mon < 10) {
            dateTime += "0";
        }
        dateTime += s_mon;
        int s_dd = cd1.get(Calendar.DAY_OF_MONTH);
        if (s_dd < 10) {
            dateTime += "0";
        }
        dateTime += s_dd;
        int s_hh = cd1.get(Calendar.HOUR_OF_DAY);
        if (s_hh < 10) {
            dateTime += "0";
        }
        dateTime += s_hh;
        int s_min = cd1.get(Calendar.MINUTE);
        if (s_min < 10) {
            dateTime += "0";
        }
        dateTime += s_min;
        int s_ss = cd1.get(Calendar.SECOND);
        if (s_ss < 10) {
            dateTime += "0";
        }
        dateTime += s_ss;
        return dateTime;
    }

    public String getFormatedSystemDateTime() {
        GregorianCalendar cd1 = new GregorianCalendar();
        cd1.setTime(GpsClock.getInstance().getTime());
        String dateTime = "";
        int s_yy = cd1.get(Calendar.YEAR);
        dateTime += s_yy + "/";
        int s_mon = (cd1.get(Calendar.MONTH) + 1);
        if (s_mon < 10) {
            dateTime += "0";
        }
        dateTime += s_mon + "/";
        int s_dd = cd1.get(Calendar.DAY_OF_MONTH);
        if (s_dd < 10) {
            dateTime += "0";
        }
        dateTime += s_dd + "-";
        int s_hh = cd1.get(Calendar.HOUR_OF_DAY);
        if (s_hh < 10) {
            dateTime += "0";
        }
        dateTime += s_hh + ":";
        int s_min = cd1.get(Calendar.MINUTE);
        if (s_min < 10) {
            dateTime += "0";
        }
        dateTime += s_min + ":";
        int s_ss = cd1.get(Calendar.SECOND);
        if (s_ss < 10) {
            dateTime += "0";
        }
        dateTime += s_ss;
        return dateTime;
    }

    /**
     * Get The System Date
     *
     * @return
     */
    public String getSystemDate() {
        GregorianCalendar cd1 = new GregorianCalendar();
        cd1.setTime(GpsClock.getInstance().getTime());
        //    String dateTime = "";
        int s_yy = cd1.get(Calendar.YEAR);
        String dateTime = String.valueOf(s_yy);
        int s_mon = (cd1.get(Calendar.MONTH) + 1);
        if (s_mon < 10) {
            dateTime += "0";
        }
        dateTime += s_mon;
        int s_dd = cd1.get(Calendar.DAY_OF_MONTH);
        if (s_dd < 10) {
            dateTime += "0";
        }
        dateTime += s_dd;
        return dateTime;
    }

    /**
     * Get The System Time
     *
     * @return
     */
    public String getTime() {
        return getSystemDateTime().substring(8, 14);
    }

    public String getYear() {
        return getSystemDate().substring(0, 4);
    }

    public String getFormatDate(String value, String f_value){
        if (f_value==null||f_value.length()==0) return null;
        try{
            SimpleDateFormat simDate2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cd = Calendar.getInstance();
            cd.setTime(simDate2.parse(value));
            simDate2.applyPattern(f_value);
            return simDate2.format(cd.getTime());
        }catch(Exception x){}
        return null;
    }

    public int CompareNowTime(Date t) {
        long perTime;
        long comparetime = 1;
        if (t==null) return (int)comparetime;
        try {
            Calendar cd = Calendar.getInstance();
            cd.setTime(t);
            Calendar now = Calendar.getInstance();
            now.setTime(GpsClock.getInstance().getTime());

            perTime = now.getTimeInMillis() - cd.getTimeInMillis();
            if (perTime > 0) {
                comparetime = perTime / 1000;
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return (int)comparetime;
    }

    public int CompareNowTime(String dateStr) {
        long perTime;
        long comparetime = 1;
        if (dateStr==null || dateStr.length()!=14) return (int)comparetime;
        try {
            SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cd = Calendar.getInstance();
            cd.setTime(simDate.parse(dateStr));
            Calendar now = Calendar.getInstance();
            now.setTime(GpsClock.getInstance().getTime());

            perTime = now.getTimeInMillis() - cd.getTimeInMillis();
            if (perTime > 0) {
                comparetime = perTime / 1000;
//				System.out.println("Compare Time in sec result" + (int)comparetime);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return (int)comparetime;
    }

    public String getDateBefA1(String dateStr, String orgStr) {
        String dateTime = "";
        long perTime;
        try {
            SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cd1 = Calendar.getInstance();
            cd1.setTime(simDate.parse(dateStr));
            if (!orgStr.equals("")) {
                Calendar cd2 = Calendar.getInstance();
                cd2.setTime(simDate.parse(orgStr));
                perTime = cd1.getTimeInMillis() - cd2.getTimeInMillis();
                if (perTime < 0) {
                    perTime = 10000;
                }
            } else {
                perTime = 10000;
            }
            perTime = perTime / 2;
            System.out.println("A1 Node Period time is : " + perTime);
            //15秒前
            long kk = cd1.getTimeInMillis() - perTime;
            cd1.setTimeInMillis(kk);
            //轉換格式
            int s_yy = cd1.get(Calendar.YEAR);
            dateTime += s_yy;
            int s_mon = (cd1.get(Calendar.MONTH) + 1);
            if (s_mon < 10) {
                dateTime += "0";
            }
            dateTime += s_mon;
            int s_dd = cd1.get(Calendar.DAY_OF_MONTH);
            if (s_dd < 10) {
                dateTime += "0";
            }
            dateTime += s_dd;
            int s_hh = cd1.get(Calendar.HOUR_OF_DAY);
            if (s_hh < 10) {
                dateTime += "0";
            }
            dateTime += s_hh;
            int s_min = cd1.get(Calendar.MINUTE);
            if (s_min < 10) {
                dateTime += "0";
            }
            dateTime += s_min;
            int s_ss = cd1.get(Calendar.SECOND);
            if (s_ss < 10) {
                dateTime += "0";
            }
            dateTime += s_ss;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return dateTime;
    }

    public int getDaysOfTheMonth(int yy, int mon) {
        int days;
        if (yy <= 1752 && yy % 4 == 0 && mon == 2) {
            days = 29;
        } else if (yy > 1752
                && ((yy % 4 == 0 && yy % 100 != 0) || yy % 400 == 0)
                && mon == 2) {
            days = 29;
        } else if (mon == 1 || mon == 3 || mon == 5 || mon == 7 || mon == 8
                || mon == 10 || mon == 12) {
            days = 31;
        } else if (mon == 4 || mon == 6 || mon == 9 || mon == 11) {
            days = 30;
        } else {
            days = 28;
        }
        return days;
    }

    public String addHour_UTC(String str1, int UTC){
        if (str1 == null||str1.length()<14) return "00000000000000";
        String dateTime = "20010000000000";
        long perTime;
        try {
//			System.out.println("obj: " + str1 + "  UTC:"+UTC);
            SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cd1 = Calendar.getInstance();
            cd1.setTime(simDate.parse(str1));
            cd1.add(Calendar.HOUR,UTC);
            dateTime = simDate.format(cd1.getTime());
        }catch(Exception x){
            x.printStackTrace();
        }
        return dateTime;
    }

    public String addHour_UTC(Date src_day, int UTC){
        String dateTime = "20010000000000";
        long perTime;
        try {
            SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cd1 = Calendar.getInstance();
            cd1.setTime(src_day);
            cd1.add(Calendar.HOUR,UTC);
            dateTime = simDate.format(cd1.getTime());
        }catch(Exception x){
            x.printStackTrace();
        }
        return dateTime;
    }

    public int CompareTime(String dateStr_new, String dateStr_old) {
        long perTime;
        long comparetime = 1;
        // return value -1 ,is data format error
        if (null == dateStr_new || dateStr_new.equals("")) return -1;
        if (null == dateStr_old || dateStr_old.equals("")) return -1;
        try {
            SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cd = Calendar.getInstance();
            cd.setTime(simDate.parse(dateStr_old));
            Calendar now = Calendar.getInstance();
            now.setTime(simDate.parse(dateStr_new));

            perTime = now.getTimeInMillis() - cd.getTimeInMillis();
            comparetime = perTime / 1000;
//			System.out.println("Compare Time in sec result: " + (int)comparetime);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return (int)comparetime;
    }



    public static void main(String[] args) {
        //    System.out.println(getDaysOfTheMonth(2003,10));
//		System.out.println(getDateBefA1("20050101202031", "20050101202032"));
    }
}