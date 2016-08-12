package com.hantek.ttia.module.sqliteutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.hantek.ttia.module.forwardutils.ForwardMessage;

public class DatabaseHelper extends SQLiteOpenHelper {
    static final String TAG = DatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "TTIA.db";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper helper;
    private static Context mContext;

    public static DatabaseHelper getInstance(Context context) {
        if (helper == null) {
            synchronized (DatabaseHelper.class) {
                if (helper == null) {
                    helper = new DatabaseHelper(context);
                }
            }
        }

        mContext = context;
        return helper;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, String.format("----- DatabaseHelper %s %s -----", DATABASE_NAME, DATABASE_VERSION));
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            Log.d(TAG, String.format("----- onCreate %s %s -----", DATABASE_NAME, DATABASE_VERSION));
            sqLiteDatabase.execSQL(PacketEntity.CREATE_SQL);
            sqLiteDatabase.execSQL(AdvertEntity.CREATE_SQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(TAG, String.format("----- onUpgrade %s %s -----", oldVersion, newVersion));
    }

    public int countPacket() {
        Cursor cursor = getReadableDatabase().rawQuery(String.format("select count(*) from Packet where %s", PacketEntity.ACK + " = '0' or " + PacketEntity.ACK2 + " = '0'"), null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int count() {
        Cursor cursor = getReadableDatabase().rawQuery("select count(*) from Packet", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public void insertPacket(String msgID, String sequence, String payload, Date sendTime, int ack, int ack2) throws Exception {
        PacketEntity.insert(this, msgID, sequence, payload, sendTime, ack, ack2);
    }

    public void updateTTIAAck(int msgID, int sequence) {
        PacketEntity.updateAck(this, String.valueOf(msgID), String.valueOf(sequence));
    }

    public void updateTTIAAckList(ArrayList<ForwardMessage> al) {
        PacketEntity.updateAckList(this, al);
    }

    public void updateHantekAck(int msgID, int sequence) {
        PacketEntity.updateAck2(this, String.valueOf(msgID), String.valueOf(sequence));
    }

    public void updateHantekList(ArrayList<ForwardMessage> al) {
        PacketEntity.updateAck2List(this, al);
    }

    public void updateTime(String msgID, String sequence, Date sendtime) {
        PacketEntity.updateTime(this, msgID, sequence, sendtime);
    }

    public void deletePacket() {
        PacketEntity.deleteAll(this);
    }

    public int ack1() {
        return PacketEntity.ack1(this);
    }

    public int ack0() {
        return PacketEntity.ack0(this);
    }

    public Cursor getPacket() {
        return PacketEntity.getPacket(this, PacketEntity.ACK + " = '0' or " + PacketEntity.ACK2 + " = '0'", null, PacketEntity.SEQ);
    }

    public Cursor getPacket(int beginLimit, int endLimit) {
        return PacketEntity.getPacket(this, PacketEntity.ACK + " = '0' or " + PacketEntity.ACK2 + " = '0'", null, PacketEntity.SEQ, beginLimit + "," + endLimit);
    }

    public boolean insertAdvert(String folder, String fileName, String version) {
        return AdvertEntity.insert(this, folder, fileName, version) >= 1;
    }

    public boolean updateAdvert(String folder, String fileName, String version) {
        return AdvertEntity.updateVersion(this, folder, fileName, version) > 0;
    }

    public boolean deleteAdvert(String folder, String fileName) {
        return AdvertEntity.deleteAdvert(this, folder, fileName) > 0;
    }

    public Cursor getAdvert() {
        return AdvertEntity.getData(this, "", null, "");
    }

    public void exportDB() {
        try {
            if (mContext == null)
                return;

            String pack_name = mContext.getPackageName();
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + pack_name + "//databases//" + DATABASE_NAME;
                Log.d(TAG, "backup DB:" + currentDBPath);
                String backupDBPath = DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(mContext, "Backup db Successful !", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Backup Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void importDB() {
        try {
            if (mContext == null) return;
            String pack_name = mContext.getPackageName();
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//" + pack_name
                        + "//databases//" + DATABASE_NAME;
                Log.d("ImportDB", currentDBPath);
                String backupDBPath = DATABASE_NAME; // From SD directory.
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(mContext, "Import Successful!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Import Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteDB() {
        String pack_name = mContext.getPackageName();
        File data = Environment.getDataDirectory();
        String currentDBPath = "//data//" + pack_name + "//databases//" + DATABASE_NAME;
        Log.d(TAG, "delete DB" + currentDBPath);
        File currentDB = new File(data, currentDBPath);
        currentDB.delete();
    }
}
