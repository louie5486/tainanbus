package com.hantek.ttia.dl;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

import com.hantek.ttia.module.AccTimer;
import com.hantek.ttia.module.AudioPlayer;
import com.hantek.ttia.module.NetworkUtils;
import com.hantek.ttia.module.Radius;
import com.hantek.ttia.module.SharedPreferencesHelper;
import com.hantek.ttia.module.SystemConfig;
import com.hantek.ttia.module.advertutils.Advert;
import com.hantek.ttia.module.ledutils.DownloadFactory;
import com.hantek.ttia.module.ledutils.LEDInfo;
import com.hantek.ttia.module.ledutils.LEDPlayer;
import com.hantek.ttia.module.reportutils.RegularTransfer;
import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadDataFactory;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.module.roadutils.RoadUpdater;
import com.hantek.ttia.module.sqliteutils.AdvertEntity;
import com.hantek.ttia.module.sqliteutils.DatabaseHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import component.LogManager;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    public static final int CLOSE_PROGRESS = 8345;
    public static final int NOTIFY_PROGRESS = 8346;

    static final String TAG = DownloadService.class.getName();

    //無版本確認
    //static final String CLOUD_ADVERT_URL = "http://61.222.88.241:8089/BUS/customerID/advert/update.xml";

    //版本確認
    static final String CLOUD_ADVERT_URL = "http://61.222.88.241:8089/BUS/customerID/advert/advert.xml";
    static final String CLOUD_ADVERT_DL = "http://61.222.88.241:8089/BUS/customerID/advert";

    private static boolean advertRunning = false;
    private static int prepareDownload = 0;

    public DownloadService() {
        super("DownloadService");
    }

    public static int getPrepareSize() {
        if (!advertRunning)
            return 0;
        return prepareDownload;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        try {
            String customerID = intent.getStringExtra("customer_id");
            ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");

            // 2016-03-23 參數必須先讀出, 離線也可以運作站名播報
            getLocalConfig();

            waitingNetwork(1);
            // download system
            SystemConfig config = DownloadFactory.getCloudDataSystem(customerID);
            if (config != null) {
                saveConfig(config);
                LogManager.write("debug", "fetch system.xml success...", null);
            } else {
                LogManager.write("debug", "fetch system.xml fail...", null);
            }

            waitingNetwork(2);
            // download LED style(loop).
            List<LEDInfo> tmpLEDInfoList = DownloadFactory.getCloudData(customerID);
            if (tmpLEDInfoList != null) {
                SharedPreferencesHelper.getInstance(getBaseContext()).setLEDinfo(tmpLEDInfoList);
                LogManager.write("debug", "fetch led.xml success...", null);
            } else {
                LogManager.write("debug", "fetch led.xml fail...", null);
            }
            LEDPlayer.getInstance().setData(SharedPreferencesHelper.getInstance(getBaseContext()).getLEDinfo());

            waitingNetwork(3);
            // download LED style(radius)
            List<LEDInfo> tmpRadius = DownloadFactory.getCloudDataSys(customerID);
            if (tmpRadius != null) {
                SharedPreferencesHelper.getInstance(getBaseContext()).setLEDinfoRadius(tmpRadius);
                LogManager.write("debug", "fetch led_radius.xml success...", null);
            } else {
                LogManager.write("debug", "fetch led_radius.xml fail...", null);
            }
            LEDPlayer.getInstance().setRadiusData(SharedPreferencesHelper.getInstance(getBaseContext()).getLEDinfoRadius());

            waitingNetwork(4);
            // download LED style(welcome)
            List<LEDInfo> tmpWelcome = DownloadFactory.getCloudDataWel(customerID);
            if (tmpWelcome != null) {
                SharedPreferencesHelper.getInstance(getBaseContext()).setLEDinfoWelcome(tmpWelcome);
                LogManager.write("debug", "fetch led_welcome.xml success...", null);
            } else {
                LogManager.write("debug", "fetch led_welcome.xml fail...", null);
            }
            LEDPlayer.getInstance().setWelcome(SharedPreferencesHelper.getInstance(getBaseContext()).getLEDinfoWelcome());

            waitingNetwork(5);
            try {
                // download advert
                List<Advert> downloadList = new ArrayList<Advert>();
                if (intent.hasExtra("url"))
                    downloadList = (List<Advert>) intent.getSerializableExtra("url");
                advertRunning = true;
                downloadAdvert(downloadList, customerID, receiver);
            } catch (Exception ex) {
                LogManager.write("error", "advert," + customerID + "," + ex.toString(), null);
            } finally {
                advertRunning = false;
            }

            // 2016-03-23
            waitingNetwork(6);
            // download road
            if (roadRequest(customerID)) {
                RoadManager.getInstance().setRoadData(RoadDataFactory.loadRoadData());
                LogManager.write("debug", "fetch road success...", null);
            } else {
                LogManager.write("debug", "fetch road fail...", null);
            }

            Bundle resultData = new Bundle();
            // resultData.putInt("progress", 100);
            // receiver.send(UPDATE_PROGRESS, resultData);
            receiver.send(CLOSE_PROGRESS, resultData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean waitingNetwork(int index) throws InterruptedException {
        int counter = 0;
        while (!NetworkUtils.isOnline(DownloadService.this)) {
            Thread.sleep(1000);
            if (counter == 0) {
                LogManager.write("debug", "download waiting...#" + index, null);
            }
            counter++;
            if (counter >= 60)
                counter = 0;
        }

        return true;
    }

    private void saveConfig(SystemConfig config) {
        SharedPreferencesHelper.getInstance(this).setAdvertVersion(config.advertVersion);
        SharedPreferencesHelper.getInstance(this).setRoadVersion(config.roadVersion);
        SharedPreferencesHelper.getInstance(this).setWelcomeVersion(config.welcomeVersion);
        SharedPreferencesHelper.getInstance(this).setRadiusVersion(config.radiusVersion);

        if (config.inRadius != null) {
            SharedPreferencesHelper.getInstance(this).setInRadius(config.inRadius);
            RoadManager.getInstance().setInRadius(config.inRadius.distance);
            AudioPlayer.getInstance().setInRadius(config.inRadius);
        }

        if (config.outRadius != null) {
            SharedPreferencesHelper.getInstance(this).setOutRadius(config.outRadius);
            RoadManager.getInstance().setOutRadius(config.outRadius.distance);
            AudioPlayer.getInstance().setOutRadius(config.outRadius);
        }

        if (config.report > 0) {
            SharedPreferencesHelper.getInstance(this).setReport(config.report);
            RegularTransfer.getInstance().setTransfer(config.report);
        }

        if (config.acc > 0) {
            SharedPreferencesHelper.getInstance(this).setACC(config.acc);
            AccTimer.getInstance().setAcc(config.acc);
        }

        SharedPreferencesHelper.getInstance(this).setGender(config.gender);
        SharedPreferencesHelper.getInstance(this).setLang(config.lang);

        SharedPreferencesHelper.getInstance(this).setVOICE_WELCOME(config.welcomeVoice);
        SharedPreferencesHelper.getInstance(this).setUSE_WELCOME(config.speakWelcome ? 1 : 0);
        AudioPlayer.getInstance().setWelcome(config.speakWelcome, config.welcomeVoice, config.gender, config.lang);

        SharedPreferencesHelper.getInstance(this).setVOICE_STOP(config.stopVoice);
        SharedPreferencesHelper.getInstance(this).setUSE_STOP(config.speakStop ? 1 : 0);
        AudioPlayer.getInstance().setStop(config.speakStop, config.stopVoice, config.gender, config.lang);

        SharedPreferencesHelper.getInstance(this).setRPM(config.RPM);
        SharedPreferencesHelper.getInstance(this).setAccelerate(config.accelerate);
        SharedPreferencesHelper.getInstance(this).setDecelerate(config.decelerate);
        SharedPreferencesHelper.getInstance(this).setHalt(config.halt);
        SharedPreferencesHelper.getInstance(this).setMovement(config.movement);
    }

    private void getLocalConfig() {
        SharedPreferencesHelper.getInstance(this).getAdvertVersion();
        SharedPreferencesHelper.getInstance(this).getRoadVersion();
        SharedPreferencesHelper.getInstance(this).getWelcomeVersion();
        SharedPreferencesHelper.getInstance(this).getRadiusVersion();

        Radius inRadius = SharedPreferencesHelper.getInstance(this).getInRadius();
        if (inRadius != null) {
            RoadManager.getInstance().setInRadius(inRadius.distance);
            AudioPlayer.getInstance().setInRadius(inRadius);
        }

        Radius outRadius = SharedPreferencesHelper.getInstance(this).getOutRadius();
        if (outRadius != null) {
            RoadManager.getInstance().setOutRadius(outRadius.distance);
            AudioPlayer.getInstance().setOutRadius(outRadius);
        }

        int reportTick = SharedPreferencesHelper.getInstance(this).getReport();
        if (reportTick > 0)
            RegularTransfer.getInstance().setTransfer(reportTick);

        int accTick = SharedPreferencesHelper.getInstance(this).getACC();
        if (accTick > 0) {
            AccTimer.getInstance().setAcc(accTick);
        }

        String gender = SharedPreferencesHelper.getInstance(this).getGender();
        String lang = SharedPreferencesHelper.getInstance(this).getLang();

        String fileName = SharedPreferencesHelper.getInstance(this).getVOICE_WELCOME();
        int useWelcome = SharedPreferencesHelper.getInstance(this).getUSE_WELCOME();
        AudioPlayer.getInstance().setWelcome(useWelcome == 1, fileName, gender, lang);

        String fileNameSTOP = SharedPreferencesHelper.getInstance(this).getVOICE_STOP();
        int useSTOP = SharedPreferencesHelper.getInstance(this).getUSE_STOP();
        AudioPlayer.getInstance().setStop(useSTOP == 1, fileNameSTOP, gender, lang);

        SharedPreferencesHelper.getInstance(this).getRPM();
        SharedPreferencesHelper.getInstance(this).getAccelerate();
        SharedPreferencesHelper.getInstance(this).getDecelerate();
        SharedPreferencesHelper.getInstance(this).getHalt();
        SharedPreferencesHelper.getInstance(this).getMovement();

        LEDPlayer.getInstance().setData(SharedPreferencesHelper.getInstance(getBaseContext()).getLEDinfo());
        LEDPlayer.getInstance().setRadiusData(SharedPreferencesHelper.getInstance(getBaseContext()).getLEDinfoRadius());
        LEDPlayer.getInstance().setWelcome(SharedPreferencesHelper.getInstance(getBaseContext()).getLEDinfoWelcome());
    }

    private boolean downloadAdvert(List<Advert> downloadList, String customerID, ResultReceiver receiver) {
        advertLog("*** Advert Start Download ***");

        // step1. 下載遠端資料
        String cloudXMLString = getCloudAdvertSetting(customerID);
        advertLog("Advert cloud advert.xml:" + cloudXMLString.length());
        if (cloudXMLString.length() == 0)
            return false;

        List<Advert> cloud = parse(cloudXMLString); // getCloudAdvertData(customerID);
        advertLog("Advert cloud size:" + cloud.size());
        if (cloud.size() == 0)
            return false;

        // step2. 載入本機Advert檔案
        List<Advert> local = getAdvertFolder();
        if (local == null) {
            advertLog("Advert folder is not exist.");
            return false;
        }

        // 2.載入DB設定
        List<Advert> tmpSPList = new ArrayList<>();
        Cursor cursor = DatabaseHelper.getInstance(this).getAdvert();
        if (cursor != null && cursor.getCount() >= 1) {
            tmpSPList = load(cursor);
            cursor.close();
        }

        //帶入版本
        for (int i = 0; i < local.size(); i++) {
            Advert localAdvert = local.get(i);
            boolean noData = true;
            for (int j = 0; j < tmpSPList.size(); j++) {
                Advert dbAdvert = tmpSPList.get(j);
                if (localAdvert.folderName.equalsIgnoreCase(dbAdvert.folderName)) {
                    noData = false;
                    String[] localFile = localAdvert.fileItem;
                    String[] dbFile = dbAdvert.fileItem;
                    for (int k = 0; k < localFile.length; k++) {
                        boolean find = false;
                        for (int l = 0; l < dbFile.length; l++) {
                            if (localFile[k].replace(".mp3", "").contains(dbFile[l].split("\\^")[0])) {
                                localFile[k] = dbFile[l];
                                find = true;
//                                Log.d(TAG, "Advert overwrite version local:" + localFile[k] + " DB:" + dbFile[l]);
                            }

                            if (find)
                                break;
                        }

                        // 沒有版本資料, 設定為0
                        if (!find) {
                            DatabaseHelper.getInstance(this).insertAdvert(localAdvert.folderName, localFile[k].replace(".mp3", ""), "0");
                            localFile[k] = localFile[k].replace(".mp3", "") + "^0";
//                            Log.d(TAG, "Advert overwrite fail local:" + localFile[k] + ", folder:" + localAdvert.folderName);
                        }
                    }
                }
            }

            if (noData) {
                for (int ii = 0; ii < localAdvert.fileItem.length; ii++) {
                    DatabaseHelper.getInstance(this).insertAdvert(localAdvert.folderName, localAdvert.fileItem[ii].replace(".mp3", ""), "0");
                    localAdvert.fileItem[ii] = localAdvert.fileItem[ii].replace(".mp3", "") + "^0";
                }
            }
        }

        // step3. 檢查Advert檔案與資料
        for (Advert cloudAdvert : cloud) {
            boolean localNoData = true;
            for (Advert localAdvert : local) {
                if (cloudAdvert.folderName.equalsIgnoreCase(localAdvert.folderName)) {
                    localNoData = false;
                    // add advert
                    for (String cloudFile : cloudAdvert.fileItem) {
                        String cloudFileName = cloudFile.split("\\^")[0];
                        String cloudFileVersion = cloudFile.split("\\^")[1];
                        boolean update = true;
                        for (String localFile : localAdvert.fileItem) {
//                            Log.d(TAG, "Advert download check cloudFile:" + cloudFile + ", localFile:" + localFile + ", folderName:" + cloudAdvert.folderName);
                            String localFileName = localFile.split("\\^")[0];
                            String localFileVersion = localFile.split("\\^")[1];
                            if (cloudFileName.replace(".mp3", "").equalsIgnoreCase(localFileName.replace(".mp3", ""))) {
                                update = !cloudFileVersion.equalsIgnoreCase(localFileVersion);
                                break;
                            }
                        }

                        if (update) {
                            DatabaseHelper.getInstance(this).deleteAdvert(localAdvert.folderName, cloudFileName);
                            Advert tmpAdvert = new Advert();
                            tmpAdvert.folderName = cloudAdvert.folderName;
                            tmpAdvert.fileItem = new String[]{cloudFileName};
                            tmpAdvert.version = cloudFileVersion;
                            downloadList.add(tmpAdvert);
                        }
                    }
                    break;
                }
            }

            if (localNoData) {
                for (String name : cloudAdvert.fileItem) {
                    Advert tmpAdvert = new Advert();
                    tmpAdvert.folderName = cloudAdvert.folderName;
                    tmpAdvert.fileItem = new String[]{name.split("\\^")[0]};
                    tmpAdvert.version = name.split("\\^")[1];
                    downloadList.add(tmpAdvert);
                }
            }
        }

        // delete advert
        List<Advert> deleteList = new ArrayList<>();
        for (Advert localAdvert : local) {
            boolean localNoData = true;
            for (Advert cloudAdvert : cloud) {
                if (cloudAdvert.folderName.equalsIgnoreCase(localAdvert.folderName)) {
                    localNoData = false;
                    // delete advert
                    for (String localFile : localAdvert.fileItem) {
                        String localFileName = localFile.split("\\^")[0];
                        boolean delete = true;
                        for (String cloudFile : cloudAdvert.fileItem) {
                            String cloudFileName = cloudFile.split("\\^")[0];
//                            Log.d(TAG, "Advert delete check cloudFile:" + cloudFile + ", localFile:" + localFile + ", folder:" + cloudAdvert.folderName);
                            if (cloudFileName.replace(".mp3", "").equalsIgnoreCase(localFileName.replace(".mp3", ""))) {
                                delete = false;
                                break;
                            }
                        }

                        if (delete) {
                            Advert tmpAdvert = new Advert();
                            tmpAdvert.folderName = cloudAdvert.folderName;
                            tmpAdvert.fileItem = new String[]{localFileName};
                            deleteList.add(tmpAdvert);
                        }
                    }
                    break;
                }
            }

            if (cloud.size() > 0 && localNoData) {
                for (String name : localAdvert.fileItem) {
                    Advert tmpAdvert = new Advert();
                    tmpAdvert.folderName = localAdvert.folderName;
                    tmpAdvert.fileItem = new String[]{name.split("\\^")[0]};
                    deleteList.add(tmpAdvert);
                }
            }
        }

        prepareDownload = downloadList.size();
        advertLog("Advert prepare download:" + downloadList.size());
        advertLog("Advert prepare delete:" + deleteList.size());

        // step4. 開始下載
        int successCounter = 0;
        int failCounter = 0;

        if (prepareDownload > 0) {
            String urls = CLOUD_ADVERT_DL.replace("customerID", customerID); // one customer, one folder.
            for (Advert a : downloadList) {

                // 直到有網路才下載
                while (!NetworkUtils.isOnline(DownloadService.this)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Calendar sTime = Calendar.getInstance();
                boolean success = false;
                String urlToDownload = urls + "/" + a.folderName + "/" + a.fileItem[0] + ".mp3";
                String fileName = a.folderName + "/" + a.fileItem[0] + ".mp3";

                int retryCounter = 0;
                do {
                    try {
                        URL url = new URL(urlToDownload);
                        URLConnection connection = url.openConnection();
                        connection.connect();

                        // this will be useful so that you can show a typical 0-100% progress bar
                        int fileLength = connection.getContentLength();

                        // download the file
                        InputStream input = new BufferedInputStream(connection.getInputStream());

                        // create folder 'advertXX'
                        File file = new File(getFolder(a.folderName + "/"));
                        file.mkdirs();

                        OutputStream output = new FileOutputStream(getFolder(fileName));
                        byte data[] = new byte[1024];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            // publishing the progress....
                            Bundle resultData = new Bundle();
                            resultData.putInt("progress", (int) (total * 100 / fileLength));
                            receiver.send(UPDATE_PROGRESS, resultData);
                            output.write(data, 0, count);
                        }

                        output.flush();
                        output.close();
                        input.close();
                        DatabaseHelper.getInstance(this).insertAdvert(a.folderName, a.fileItem[0], a.version);
                        success = true;
                    } catch (Exception e) {
                        retryCounter += 1;
                        e.printStackTrace();
                    }
                    //下載失敗超過10次則放棄, 下次開機在下載
                } while (!success && retryCounter < 10);

                if (success) {
                    successCounter += 1;
                    prepareDownload -= 1;
                } else
                    failCounter += 1;

                long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
                advertLog(String.format("Advert url:%s,%s,EOT:%04d.", urlToDownload, success ? "OK" : "Fail", EOT));
            }

            advertLog("Advert result: " + successCounter + ", fail:" + failCounter);
        }

        // step 5. delete advert
        if (deleteList.size() > 0) {
            try {
                deleteFile(deleteList);
            } catch (Exception e) {
                advertLog("Advert delete fail," + e.toString());
            }
        }

        advertLog("*** Advert End Download ***");
        return true;
    }

    public boolean roadRequest(String customerID) {
        List<Road> cloudRoadData = RoadUpdater.getCloudRoadData(customerID);

        if (cloudRoadData == null)
            return false;
        else {
            try {
                List<Road> localRoadData = RoadManager.getInstance().getLocalRoadData();
                Log.d(TAG, String.format("Cloud total:%s, Local total:%s", cloudRoadData.size(), localRoadData.size()));

                List<Road> downloadList = new ArrayList<Road>();
                for (Road remote : cloudRoadData) {
                    boolean update = true;
                    for (Road local : localRoadData) {
                        if (local.id == remote.id && local.branch.equalsIgnoreCase(remote.branch) && local.direct == remote.direct) {
                            if (local.version == remote.version) {
                                // 版本相同 不需更新
                                update = false;
                                Log.d(TAG, "Same version " + local.toString());
                                break;
                            }
                        }
                    }

                    if (update) {
                        downloadList.add(remote);
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<Road> removeList = new ArrayList<Road>();
                // 移除路線
                for (Road local : localRoadData) {
                    boolean foundData = false;
                    for (Road remote : cloudRoadData) {
                        if (local.id == remote.id && local.branch.equalsIgnoreCase(remote.branch) && local.direct == remote.direct) {
                            foundData = true;
                            break;
                        }
                    }

                    if (!foundData)
                        removeList.add(local);
                }

                if (removeList.size() > 0) {
                    for (Road road : removeList) {
                        String fileName = String.format("%04d%s%s.txt", road.id, road.branch, road.direct);
                        RoadDataFactory.deleteFile(fileName);
                    }
                }

                for (Road road : downloadList) {
                    String fileName = String.format("%04d%s%s.txt", road.id, road.branch, road.direct);
                    RoadUpdater.fetchRoadFile(fileName, customerID);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogManager.write("error", String.format("*** fetch Road data, %s.%s ***", e.toString(), customerID), null);
                return false;
            }
            return true;
        }
    }

    private void advertLog(String content) {
        LogManager.write("advert", content, null);
    }

    /**
     * 取回音效檔版本
     */
    private List<Advert> load(Cursor cursor) {
        return AdvertEntity.parse(cursor);
    }

    private void deleteFile(List<Advert> deleteList) {
        for (Advert a : deleteList) {
            DatabaseHelper.getInstance(this).deleteAdvert(a.folderName, a.fileItem[0]);
            String fileName = a.folderName + "/" + a.fileItem[0] + ".mp3";
            File file = new File(getFolder(fileName));
            if (file.exists() && file.delete()) {
                advertLog("Advert Delete success: " + file.getAbsolutePath());
            } else {
                advertLog("Advert Delete fail: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * 取得雲端的音效資料
     *
     * @return advert xml
     */
    public String getCloudAdvertSetting(String customerID) {
        HttpClient client = new DefaultHttpClient();
        String url = CLOUD_ADVERT_URL.replace("customerID", customerID); // one customer, one folder.
        HttpGet get = new HttpGet(url);

        String ret;
        int retryCounter = 0;
        do {
            try {
                HttpResponse response = client.execute(get);
                HttpEntity r_entity = response.getEntity();
                String xmlString = EntityUtils.toString(r_entity);

                try {
                    xmlString = new String(xmlString.getBytes(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                // System.out.println(xmlString);
                ret = xmlString;
            } catch (Exception e) {
                e.printStackTrace();
                ret = "";

            }

            //下載失敗 延遲1秒
            if (ret.length() == 0) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            retryCounter += 1;
        } while (retryCounter <= 3 && ret.length() == 0);
        // 嘗試3次

        return ret;
    }

    /**
     * obsolete 取得雲端的音效資料
     *
     * @return advert list
     */
    public List<Advert> getCloudAdvertData(String customerID) {
        // Log.d(TAG, String.format("*** fetch advert_%s data ***", customerID));
        // LogManager.write("ROAD", String.format("*** fetch advert_%s data ***", customerID), null);
        HttpClient client = new DefaultHttpClient();
        String url = CLOUD_ADVERT_URL.replace("customerID", customerID); // one customer, one folder.
        HttpGet get = new HttpGet(url);

        try {
            HttpResponse response = client.execute(get);
            HttpEntity r_entity = response.getEntity();
            String xmlString = EntityUtils.toString(r_entity);

            try {
                xmlString = new String(xmlString.getBytes(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println(xmlString);
            SharedPreferencesHelper.getInstance(DownloadService.this).setAdvert(xmlString);
            // Log.d(TAG, "Download advert: " + xmlString);
            return parseAdvertData(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * obsolete 解析音效版本資料
     *
     * @param xmlString origin content
     * @return road list
     */
    private List<Advert> parseAdvertData(String xmlString) {
        List<Advert> tmp = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            try {
                Document doc = db.parse(is);
                NodeList nl = doc.getElementsByTagName("f");
                for (int i = 0; i < nl.getLength(); i++) {
                    try {
                        Node nNode = nl.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;
                            System.out.println("Folder name: " + eElement.getAttribute("name"));
                            System.out.println("File name: " + eElement.getElementsByTagName("file").item(0).getTextContent());
                            Advert advert = new Advert();
                            advert.folderName = eElement.getAttribute("name");
                            advert.fileItem = eElement.getElementsByTagName("file").item(0).getTextContent().split("\\|");
                            tmp.add(advert);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * 解析音效版本資料
     *
     * @param xmlString origin content
     * @return road list
     */
    private List<Advert> parse(String xmlString) {
        List<Advert> tmp = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            try {
                Document doc = db.parse(is);
                Element root = doc.getDocumentElement();
                NodeList items = root.getChildNodes();
                int size = items.getLength();

                for (int i = 0; i < size; i++) {
                    try {
                        Node item = items.item(i);
                        if (item.getNodeName().equalsIgnoreCase("version")) {

                        } else if (item.getNodeName().equalsIgnoreCase("f")) {
                            if (item.getNodeType() == Node.ELEMENT_NODE) {
                                Element eElement = (Element) item;
                                Advert advert = Advert.parse(eElement);
                                tmp.add(advert);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public static String getFilePath(String folder, String fileName, String type) {
        return getFolder("advert" + type + folder + "/" + fileName);
    }

    private static String getFolder(String fileName) {
        return String.format("%s/%s/%s", getExternalPath(), "advert", fileName);
    }

    private static String getExternalPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private ArrayList<Advert> getAdvertFolder() {
        String dirPath = String.format("%s/%s", getExternalPath(), "advert"); // main folder
        File dirFile = new File(dirPath);
        dirFile.mkdirs();

        File[] list = dirFile.getAbsoluteFile().listFiles();

        if (list == null)
            return null;

        ArrayList<Advert> tmpAdvertList = new ArrayList<Advert>();
        for (File fileItem : list) {
            if (fileItem.isDirectory()) {
                Advert advert = new Advert();
                advert.folderName = fileItem.getName();
                advert.fileItem = fileItem.getAbsoluteFile().list();
                tmpAdvertList.add(advert);

                System.out.println("Advert local getName: " + fileItem.getName() + ", getDir getAbsolutePath: " + fileItem.getAbsolutePath());
            }
        }

        return tmpAdvertList;
    }

//    private boolean sendPost(String text){
//        HttpPost httpPost = new HttpPost("http://61.222.88.241:8089/bus/128/update/");
//    }
//
//    private boolean writeLog(byte[] data) {
//        Log.d(TAG, "writeLog" + data.length);
//        final String BOUNDARY = "==================================";
//        final String HYPHENS = "--";
//        final String CRLF = "\r\n";
//        URL url = null;
//        try {
//            url = new URL("http://61.222.88.241:8089/bus/128/update/");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        HttpURLConnection conn = null;
//        try {
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");                        // method一定要是POST
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setUseCaches(false);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        // 把Content Type設為multipart/form-data
//        // 以及設定Boundary，Boundary很重要!
//        // 當你不只一個參數時，Boundary是用來區隔參數的
//        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
//
//        // 下面是開始寫參數
//        String strContentDisposition = "Content-Disposition: form-data; name=\"image\"; filename=\"image\"";
//        String strContentType = "Content-Type: text/xml";
//        try {
//            DataOutputStream dataOS = new DataOutputStream(conn.getOutputStream());
//            dataOS.writeBytes(HYPHENS + BOUNDARY + CRLF);        // 寫--==================================
//            dataOS.writeBytes(strContentDisposition + CRLF);    // 寫(Disposition)
//            dataOS.writeBytes(strContentType + CRLF);            // 寫(Content Type)
//            dataOS.writeBytes(CRLF);
//
////            int iBytesAvailable = fileInputStream.available();
//            byte[] byteData = data; // new byte[iBytesAvailable];
//            // int iBytesRead = fileInputStream.read(byteData, 0, iBytesAvailable);
////            while (iBytesRead > 0) {
//            dataOS.write(byteData, 0, data.length);    // 開始寫內容
////                iBytesAvailable = fileInputStream.available();
////                iBytesRead = fileInputStream.read(byteData, 0, iBytesAvailable);
////            }
//            dataOS.writeBytes(CRLF);
//            dataOS.writeBytes(HYPHENS + BOUNDARY + HYPHENS);    // (結束)寫--==================================--
////            fileInputStream.close();
//            dataOS.flush();
//            dataOS.close();
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
