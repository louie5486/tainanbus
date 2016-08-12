package com.hantek.ttia.module.sqliteutils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.hantek.ttia.module.gpsutils.GpsClock;
import com.hantek.ttia.module.forwardutils.ForwardMessage;

import component.LogManager;

public class PacketEntity implements BaseColumns {
    final static String TAG = PacketEntity.class.getName();

    public static final String TABLE_NAME = "PACKET";
    public static final String MSGID = "msg_id";
    public static final String SEQ = "sequence";
    public static final String MESSAGE = "message";
    public static final String CREATETIME = "createtime";
    public static final String SENDTIME = "sendtime";
    public static final String ACK = "ack"; // TTIA comm server
    public static final String ACK2 = "ack2"; // Hantek comm server
    public static final String REP_LEVEL = "rep_level";

    public static final String CREATE_SQL = "CREATE TABLE " + TABLE_NAME + "(" + //
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + //
            SEQ + " VARCHAR(20), " + //
            MSGID + " int, " + //
            MESSAGE + " VARCHAR(1500), " + //
            CREATETIME + " datetime, " + //
            SENDTIME + " datetime, " + //
            ACK + " int, " + //
            ACK2 + " int, " + //
            REP_LEVEL + " int " + //
            ")";

    public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static long insert(SQLiteOpenHelper dbHelper, String msgID, String sequence, String payload, Date sendTime, int ack, int ack2) throws Exception {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        long newRowID = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(MSGID, msgID);
            values.put(SEQ, sequence);
            values.put(MESSAGE, payload);
            values.put(CREATETIME, String.format("%tY%1$tm%1$td%1$tH%1$tM%1$tS", sendTime));
            values.put(SENDTIME, String.format("%tY%1$tm%1$td%1$tH%1$tM%1$tS", sendTime));
            values.put(ACK, ack);
            values.put(ACK2, ack2);
            values.put(REP_LEVEL, 0);
            newRowID = db.insert(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        Log.d(TAG, String.format("Insert: ID=%s, SEQ=%s, NewRowID=%s, EOT:%04d.", msgID, sequence, newRowID, EOT));
//        LogManager.write("db", String.format("Insert: %s=%s, %s=%s, NewRowID=%s. EOT:%04d.", MSGID, msgID, SEQ, sequence, newRowID, EOT), null);
        return newRowID;
    }

    public static int updateAck(SQLiteOpenHelper dbHelper, String msgID, String sequence) {
        String whereClause = String.format("%s=%s and %s=%s", SEQ, sequence, MSGID, msgID);
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(ACK, 1);
            affected = db.update(TABLE_NAME, values, whereClause, null);

//            String sql = String.format("UPDATE %s SET %s=1 WHERE %s", TABLE_NAME, ACK, whereClause);
//            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        if (affected > 1) {
            Log.w(TAG, String.format("Double Update Ack:%s, affected:%s, EOT:%04d.", whereClause, affected, EOT));
        } else
            Log.d(TAG, String.format("Update Ack:%s, affected:%s, EOT:%04d.", whereClause, affected, EOT));
//        LogManager.write("db", String.format("Update Ack:%s, affect row=%s. EOT:%04d.", whereClause, rows, EOT), null);
        return affected;
    }

    public static int updateAck2(SQLiteOpenHelper dbHelper, String msgID, String sequence) {
        String whereClause = String.format("%s=%s and %s=%s", SEQ, sequence, MSGID, msgID);
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(ACK2, 1);
            affected = db.update(TABLE_NAME, values, whereClause, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        if (affected > 1) {
            Log.w(TAG, String.format("Double Update Ack2:%s, affected:%s, EOT:%04d.", whereClause, affected, EOT));
        } else
            Log.d(TAG, String.format("Update Ack2:%s, affected:%s, EOT:%04d.", whereClause, affected, EOT));
//        LogManager.write("db", String.format("Update Ack2:%s, affect row=%s. EOT:%04d.", whereClause, rows, EOT), null);
        return affected;
    }

    public static int updateAckList(SQLiteOpenHelper dbHelper, ArrayList<ForwardMessage> al) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(ACK, 1);

            int size = al.size();
            for (int i = 0; i < size; i++) {
                ForwardMessage tmpMsg = al.get(i);
                String whereClause = String.format("%s=%s and %s=%s", SEQ, tmpMsg.getSequence(), MSGID, tmpMsg.getMsgID());
                affected += db.update(TABLE_NAME, values, whereClause, null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        Log.d(TAG, String.format("UpdateBatch Ack:%s, affected:%s, EOT:%04d.", al.size(), affected, EOT));
        return affected;
    }

    public static int updateAck2List(SQLiteOpenHelper dbHelper, ArrayList<ForwardMessage> al) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(ACK2, 1);

            int size = al.size();
            for (int i = 0; i < size; i++) {
                ForwardMessage tmpMsg = al.get(i);
                String whereClause = String.format("%s=%s and %s=%s", SEQ, tmpMsg.getSequence(), MSGID, tmpMsg.getMsgID());
                affected += db.update(TABLE_NAME, values, whereClause, null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        Log.d(TAG, String.format("UpdateBatch Ack2:%s, affected:%s, EOT:%04d.", al.size(), affected, EOT));
        return affected;
    }

    public static int updateTime(SQLiteOpenHelper dbHelper, String msgID, String sequence, Date sendtime) {
        String time = String.format("%tY%1$tm%1$td%1$tH%1$tM%1$tS", sendtime);
        String whereClause = String.format("%s='%s' and %s='%s'", SEQ, sequence, MSGID, msgID);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(SENDTIME, time);
            affected = db.update(TABLE_NAME, values, whereClause, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        Log.d(TAG, String.format("Update Time:%s, affected:%s.", whereClause, affected));
        return affected;
    }

    public static Cursor getPacket(SQLiteOpenHelper dbHelper, String selection, String[] selectionArgs, String orderBy) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, //
                    new String[]{MSGID, SEQ, MESSAGE, SENDTIME, ACK, ACK2, REP_LEVEL}, //
                    selection, // WHERE
                    selectionArgs, // WHERE
                    null, // GROUP BY
                    null, // HAVING
                    orderBy // ORDER BY
            );
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        int count = cursor == null ? 0 : cursor.getCount();
        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        LogManager.write("db", String.format("get packet: selection='%s', count=%s, EOT:%04d.", selection, count, EOT), null);
        return cursor;
    }

    public static Cursor getPacket(SQLiteOpenHelper dbHelper, String selection, String[] selectionArgs, String orderBy, String limit) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, //
                    new String[]{MSGID, SEQ, MESSAGE, SENDTIME, ACK, ACK2, REP_LEVEL}, //
                    selection, // WHERE
                    selectionArgs, // WHERE
                    null, // GROUP BY
                    null, // HAVING
                    orderBy, // ORDER BY
                    limit
            );
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        int count = cursor == null ? 0 : cursor.getCount();
        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        LogManager.write("db", String.format("get packet: selection='%s', count=%s, EOT:%04d.", selection, count, EOT), null);
        return cursor;
    }

    public static int deleteAll(SQLiteOpenHelper dbHelper) {
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTime(GpsClock.getInstance().getTime());
        dateTime.add(Calendar.DAY_OF_WEEK, -14); // keep 14 days
        String whereClause = ACK + " = '1' and " + ACK2 + " = '1' and " + CREATETIME + " < '" + String.format("%tY%1$tm%1$td", dateTime) + "000000'";

        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            affected = db.delete(TABLE_NAME, whereClause, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        LogManager.write("db", String.format("Delete all:%s, affected:%s, EOT:%04d.", whereClause, affected, EOT), null);
        return affected;
    }

    public static int ack1(SQLiteOpenHelper dbHelper) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(ACK, 1);
            values.put(ACK2, 1);
            affected = db.update(TABLE_NAME, values, "", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        LogManager.write("db", String.format("(DEBUG) Update Ack:%s, affected:%s, EOT:%04d.", "1", affected, EOT), null);
        return affected;
    }

    public static int ack0(SQLiteOpenHelper dbHelper) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(ACK, 0);
            values.put(ACK2, 0);
            affected = db.update(TABLE_NAME, values, "", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        LogManager.write("db", String.format("(DEBUG) Update Ack:%s, affected:%s, EOT:%04d.", "0", affected, EOT), null);
        return affected;
    }
}