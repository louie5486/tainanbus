package com.hantek.ttia.module.roadutils;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import component.LogManager;

public class RoadUpdater {
    static final String TAG = RoadUpdater.class.getName();
    static final String CLOUD_URL = "http://61.222.88.241:8089/BUS/customerID/road/update.xml";
    static final String DOWNLOAD_URL = "http://61.222.88.241:8089/BUS/customerID/road/";

    /**
     * 取得雲端的路線資料
     *
     * @return road list
     */
    public static List<Road> getCloudRoadData(String customerID) {
        LogManager.write("road", String.format("*** fetch Road data, %s. ***", customerID), null);
        String url = CLOUD_URL.replace("customerID", customerID);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity r_entity = response.getEntity();
            String xmlString = EntityUtils.toString(r_entity);
            xmlString = new String(xmlString.getBytes(), "UTF-8");
            //test git
            //test git 2222
            return parseRoadData(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.write("road", String.format("*** fetch Road fail, %s. ***", customerID), null);
            return null;
        }
    }

    /**
     * 下載路線資料
     *
     * @param fileName filename
     * @return filename
     */
    public static String fetchRoadFile(String fileName, String customerID) {
        boolean fetchResult = false;
        String url = DOWNLOAD_URL.replace("customerID", customerID);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url + fileName);
            HttpResponse response = client.execute(get);
            HttpEntity r_entity = response.getEntity();
            String content = EntityUtils.toString(r_entity, "UTF-8"); // web file encoding

            if (RoadDataFactory.save(content, fileName)) {
                Log.d(TAG, content);
                fetchResult = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LogManager.write("road", String.format("fetch road file: %s, %s.", url + fileName, fetchResult), null);
        }
        return fileName;
    }

    /**
     * 解析路線版本資料
     *
     * @param xmlString origin content
     * @return road list
     */
    private static List<Road> parseRoadData(String xmlString) {
        Calendar sTime = Calendar.getInstance();
        List<Road> tmp = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            try {
                Document doc = db.parse(is);
                NodeList nl = doc.getElementsByTagName("line");
                for (int i = 0; i < nl.getLength(); i++) {
                    try {
                        int id = Integer.parseInt(doc.getElementsByTagName("id").item(i).getFirstChild().getNodeValue());
                        int version = Integer.parseInt(doc.getElementsByTagName("version").item(i).getFirstChild().getNodeValue());
                        int direct = Integer.parseInt(doc.getElementsByTagName("direct").item(i).getFirstChild().getNodeValue());
                        String branch = doc.getElementsByTagName("branch").item(i).getFirstChild().getNodeValue();

                        LogManager.write("road", "Cloud has Road id:" + id + " branch:" + branch + " direct:" + direct + " version:" + version, null);

                        Road road = new Road();
                        road.id = id;
                        road.branch = branch;
                        road.direct = direct;
                        road.version = version;
                        tmp.add(road);
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

        long EOT = Calendar.getInstance().getTimeInMillis() - sTime.getTimeInMillis();
        LogManager.write("road", String.format("Cloud total: %s, EOT:%04d.", tmp.size(), EOT), null);
        return tmp;
    }

    public static String getMD5Hash(File file) throws IOException {
        if (!file.exists())
            throw new IOException("The file is not exist.");

        FileInputStream fis = null;
        DigestInputStream dis = null;
        byte[] buff = new byte[1024];
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            dis = new DigestInputStream(fis, md);

            // Read bytes from the file.
            while (dis.read(buff) != -1) ;

            byte[] md5Digests = md.digest();
            return byteArray2Hex(md5Digests);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            buff = null;
            if (fis != null) fis.close();
            if (dis != null) dis.close();
        }
        return null;
    }

    /**
     * 　　 * Convert byte array into hex.
     * 　　 * @param hash
     * 　　 * @return
     */
    public static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
