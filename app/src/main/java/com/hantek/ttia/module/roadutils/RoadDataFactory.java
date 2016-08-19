package com.hantek.ttia.module.roadutils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * 路線資料處理
 *
 * @author wsh
 */
public class RoadDataFactory {
    private static final String TAG = RoadDataFactory.class.getName();

    private static String ENCODING = "UTF-8";
    private static final String ROAD_FOLDER_NAME = "Road";
    private static boolean isFileEncodeCheck = false;

    public static List<Road> loadRoadData(Context context) {
        List<Road> roadList = new LinkedList<Road>();
        Calendar tmpStartTime = Calendar.getInstance();
        try {
            AssetManager am = context.getAssets();
            String[] fileNameList = am.list(ROAD_FOLDER_NAME);
            for (String fileName : fileNameList) {
                InputStreamReader inputStreamReader = null;
                try {
                    InputStream inputStream = am.open(String.format("%s/%s", ROAD_FOLDER_NAME, fileName));
                    inputStreamReader = new InputStreamReader(inputStream, ENCODING);
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(inputStreamReader, inputStream.available());
                        Road roadData = generate(fileName, reader);
                        roadList.add(roadData);
                        Log.d(TAG, String.format("add Road id:%s mp3:%s", roadData.id, fileName));
                    } catch (Exception e) {
                        Log.e(TAG, String.format("mp3:%s %s", fileName, e.getMessage()));
                    } finally {
                        try {
                            reader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, String.format("Load mp3:%s %s", fileName, e.getMessage()));
                    e.printStackTrace();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            // default
            roadList.add(defaultRoad());
        }

        Calendar tmpEndTime = Calendar.getInstance();
        Log.d(TAG, String.format("Load Road EOT=%04d", tmpEndTime.getTimeInMillis() - tmpStartTime.getTimeInMillis()));
        return roadList;
    }

    public static List<Road> loadRoadData() {
        List<Road> roadList = new LinkedList<Road>();
        Calendar tmpStartTime = Calendar.getInstance();
        try {
            String[] fileNameList = getRoadFile();
            for (String fileName : fileNameList) {
                try {
                    Road roadData = readFile(new File(String.format("%s/%s/%s", getExternalPath(), ROAD_FOLDER_NAME, fileName)));
                    if (roadData != null) {
                        roadList.add(roadData);
                        Log.d(TAG, String.format("add road id:%04d FN:%s", roadData.id, fileName));
                    }
                } catch (Exception e) {
                    Log.e(TAG, String.format("Load FN:%s %s", fileName, e.getMessage()));
                    e.printStackTrace();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            // default
            roadList.add(defaultRoad());
        }

        Calendar tmpEndTime = Calendar.getInstance();
        Log.d(TAG, String.format("Load Road Data EOT=%04d", tmpEndTime.getTimeInMillis() - tmpStartTime.getTimeInMillis()));
        return roadList;
    }

    public static boolean save(String content, String fileName,String encode) {
        String filePath = String.format("%s/%s/%s", getExternalPath(), ROAD_FOLDER_NAME, fileName);

        File roadFile = new File(filePath);
        roadFile.delete();
        roadFile.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(roadFile, true);
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,"UTF-8");
                outputStreamWriter.write(content);
                outputStreamWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static boolean deleteFile(String fileName) {
        String filePath = String.format("%s/%s/%s", getExternalPath(), ROAD_FOLDER_NAME, fileName);
        File roadFile = new File(filePath);
        return roadFile.delete();
    }

    public static void deleteAllFile(){
        try{
            String[] file_list = getRoadFile();
            for (String n: file_list)
                deleteFile(n);
        }catch(Exception x){x.printStackTrace();}
    }


    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }



    private static Road generate(String fileName, BufferedReader reader) throws NumberFormatException, IOException {
        String readLine;
        int line = 1;

        // 路線檔名稱: xxxxyz.txt
        // xxxx=0000~9999
        // y=0x30('0')主線 or 0x41~0x5A('A'~'Z')支線
        // z=0(其他) or 1(去程) or 2(回程)
        Road tmpRoad = new Road();
        tmpRoad.id = Integer.parseInt(fileName.substring(fileName.length() - 10, fileName.length() - 6));
        tmpRoad.branch = fileName.substring(fileName.length() - 6, fileName.length() - 5);
        tmpRoad.direct = Integer.parseInt(fileName.substring(fileName.length() - 5, fileName.length() - 4));

        while ((readLine = reader.readLine()) != null) {


            switch (line) {
                case 1:
                    readLine = readLine.replaceAll("\uFEFF", "");
                    System.out.println("line len: " + readLine.length() + " data:" + bytesToHex(readLine.getBytes()));
                    tmpRoad.totalStation = Integer.parseInt(readLine);
                    break;
                case 2:
                    tmpRoad.version = Integer.parseInt(readLine);
                    break;
                case 3:
                    String[] tmpLine3 = readLine.split(";");
                    tmpRoad.audioGender = tmpLine3[0];
                    tmpRoad.audioType = tmpLine3[1];
                    break;
                case 4:
                    String[] tmpLine4 = readLine.trim().split(";");
                    tmpRoad.beginStation = tmpLine4[0].trim();
                    tmpRoad.endStation = tmpLine4[1].trim();
                    tmpRoad.type = Integer.parseInt(tmpLine4[2].trim());
                    try {
                        tmpRoad.distance = Integer.parseInt(tmpLine4[3].trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        tmpRoad.driveTime = Integer.parseInt(tmpLine4[4].trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (tmpLine4.length > 5) {
                            tmpRoad.audioNumber = tmpLine4[5].trim();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    if (!readLine.trim().equalsIgnoreCase("")) {
                        Station station = Station.Parse(readLine);
                        tmpRoad.stationArrayList.add(station);
                    }
                    break;
            }

            line++;
        }

    /* DEBUG */
        StringBuffer sb = new StringBuffer();
        sb.append(" id:" + tmpRoad.id);
        sb.append(" branch:" + tmpRoad.branch);
        sb.append(" direct:" + tmpRoad.direct);
        sb.append(" path:" + fileName);
        Log.d(TAG, tmpRoad.toString());
        /* DEBUG */
        return tmpRoad;
    }

    private static Road defaultRoad() {
        Road defaultRoad = new Road();
        defaultRoad.id = 65535;
        defaultRoad.direct = 0;
        defaultRoad.branch = "0";
        defaultRoad.beginStation = "其他路線";
        return defaultRoad;
    }

    private static String[] getRoadFile() {
        String dirPath = String.format("%s/%s", getExternalPath(), ROAD_FOLDER_NAME);
        File dirFile = new File(dirPath);
        String[] list = dirFile.getAbsoluteFile().list();
        if (list != null)
            Arrays.sort(list);
        return list == null ? new String[0] : list;
    }



    private static boolean is_fileCheck(File file){
//        if (isFileEncodeCheck) return true;
        try{
            byte[] bin = new byte[3];
            FileInputStream inp = new FileInputStream(file);
            if (inp.available()>3){
                inp.read(bin,0,3);
                System.out.println("File encode check: " + file.getName() + " data:" + bytesToHex(bin));
                inp.close();
            }
            if (bin[0] == (byte)0xEF && bin[1] == (byte)0xBB && bin[2] == (byte)0xBF){
                ENCODING = "UTF-8";
            }else if (bin[0] == (byte)0x3c && bin[1] == (byte)0x21 && bin[2] == (byte)0x44){
                file.delete();
                return false;
            }else{
                ENCODING = "UTF-16";
            }
            isFileEncodeCheck = true;
        }catch (Exception x){
            x.printStackTrace();
        }
        return true;
    }

    private static Road readFile(File file) {
        try {
            if (!is_fileCheck(file)) return null;
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
//            BufferedReader br = new BufferedReader(new FileReader(file));
            Road roadData = generate(file.getName(), br);
            br.close();
            return roadData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getExternalPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
}
