package com.hantek.ttia.module.sqliteutils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.hantek.ttia.module.advertutils.Advert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class AdvertEntity implements BaseColumns {
    final static String TAG = PacketEntity.class.getName();
    public static final String TABLE_NAME = "ADVERT";

    public static final String FOLDER = "folder";
    public static final String FILE_NAME = "file_name";
    public static final String VERSION = "version";

    public static final String CREATE_SQL = "CREATE TABLE " + TABLE_NAME + "(" + //
            FOLDER + " VARCHAR(20), " + //
            FILE_NAME + " VARCHAR(20), " + //
            VERSION + " VARCHAR(20) " + //
            ")";

    public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static long insert(SQLiteOpenHelper dbHelper, String folder, String file_name, String version) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        long newRowID = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(FOLDER, folder);
            values.put(FILE_NAME, file_name);
            values.put(VERSION, version);
            newRowID = db.insert(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        Log.d(TAG, String.format("Advert DB insert:%s,%s,%s,EOT:%04d" , folder, file_name, version, EOT));
        return newRowID;
    }

    public static int updateVersion(SQLiteOpenHelper dbHelper, String folder, String file_name, String version) {
        String whereClause = String.format("%s='%s' and %s='%s'", FOLDER, folder, FILE_NAME, file_name);
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        int affected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(VERSION, version);
            affected = db.update(TABLE_NAME, values, whereClause, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        Log.d(TAG, String.format("Advert DB update:%s,%s,%s,EOT:%04d", folder, file_name, version, EOT));
        return affected;
    }

    public static int deleteAdvert(SQLiteOpenHelper dbHelper, String folder, String file_name) {
        String whereClause = String.format("%s='%s' and %s='%s'", FOLDER, folder, FILE_NAME, file_name);
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
        Log.d(TAG, String.format("Advert DB delete:%s,%s,EOT:%04d", folder, file_name, EOT));
        return affected;
    }

    public static Cursor getData(SQLiteOpenHelper dbHelper, String selection, String[] selectionArgs, String orderBy) {
        Calendar sTime = Calendar.getInstance();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, //
                    new String[]{FOLDER, FILE_NAME, VERSION}, //
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

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        Log.d(TAG, String.format("Advert GetData: EOT:%04d", EOT));
        return cursor;
    }

    public static List<Advert> parse(Cursor cursor) {
        HashMap<String, ArrayList<String>> map = new LinkedHashMap();
        cursor.moveToFirst();
        do {
            String folder = cursor.getString(cursor.getColumnIndex(FOLDER)).trim();
            String fileName = cursor.getString(cursor.getColumnIndex(FILE_NAME)).trim();
            String version = cursor.getString(cursor.getColumnIndex(VERSION)).trim();
            ArrayList tmp = new ArrayList();
            if (map.containsKey(folder)) {
                tmp = map.get(folder);
            }

            tmp.add(fileName + "^" + version);
//            Log.d("TAG", "Advert add:" + fileName + "^" + version + ", KEY:" + folder);
            map.put(folder, tmp);
        } while (cursor.moveToNext());

        List<Advert> tmpList = new ArrayList<>();
        for (String folder : map.keySet()) {
            ArrayList<String> list = map.get(folder);
//            Log.d("TAG", "Advert size:" + list.size() + ", KEY:" + folder);
            Advert advert = new Advert();
            advert.folderName = folder.trim();
            advert.fileItem = new String[list.size()];
            list.toArray(advert.fileItem);
            tmpList.add(advert);
        }

        return tmpList;
    }
}
