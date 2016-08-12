package com.hantek.ttia.module.ledutils;

import com.github.snowdream.android.util.Log;
import com.hantek.ttia.module.Radius;
import com.hantek.ttia.module.SystemConfig;
import com.hantek.ttia.module.Version;

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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DownloadFactory {
    static final String TAG = DownloadFactory.class.getName();
    static final String CLOUD_URL = "http://61.222.88.241:8089/BUS/customerID/led.xml";
    static final String SYS_CLOUD_URL = "http://61.222.88.241:8089/BUS/customerID/system.xml";
    static final String WEL_CLOUD_URL = "http://61.222.88.241:8089/BUS/customerID/led_welcome.xml";
    static final String RADIUS_CLOUD_URL = "http://61.222.88.241:8089/BUS/customerID/led_radius.xml";

    /**
     * 取得雲端的資料
     *
     * @return advert list
     */
    public static List<LEDInfo> getCloudData(String customerID) {
        HttpClient client = new DefaultHttpClient();
        String url = CLOUD_URL.replace("customerID", customerID);
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity r_entity = response.getEntity();
            String xmlString = EntityUtils.toString(r_entity, "UTF-16");
            xmlString = new String(xmlString.getBytes());
            System.out.println(xmlString);
            return parseData(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析音效版本資料
     *
     * @param xmlString origin content
     * @return road list
     */
    private static List<LEDInfo> parseData(String xmlString) {
        List<LEDInfo> tmp = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            try {
                Document doc = db.parse(is);
                NodeList nl = doc.getElementsByTagName("info");
                for (int i = 0; i < nl.getLength(); i++) {
                    try {
                        Node nNode = nl.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;

                            LEDInfo info = LEDInfo.parse(eElement);
                            tmp.add(info);

                            System.out.println(info.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * 取得進出站雲端的資料
     *
     * @return advert list
     */
    public static List<LEDInfo> getCloudDataSys(String customerID) {
        HttpClient client = new DefaultHttpClient();
        String url = RADIUS_CLOUD_URL.replace("customerID", customerID);
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity r_entity = response.getEntity();
            String xmlString = EntityUtils.toString(r_entity, "UTF-16");
            xmlString = new String(xmlString.getBytes());
            System.out.println(xmlString);
            return parseDataSys(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析進出站資料
     *
     * @param xmlString origin content
     * @return list
     */
    private static List<LEDInfo> parseDataSys(String xmlString) {
        List<LEDInfo> tmp = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            try {
                Document doc = db.parse(is);
                NodeList nl = doc.getElementsByTagName("info");
                for (int i = 0; i < nl.getLength(); i++) {
                    try {
                        Node nNode = nl.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;

                            LEDInfo info = LEDInfo.parse(eElement);
                            tmp.add(info);

                            System.out.println(info.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * 取得雲端的資料
     *
     * @return list
     */
    public static List<LEDInfo> getCloudDataWel(String customerID) {
        HttpClient client = new DefaultHttpClient();
        String url = WEL_CLOUD_URL.replace("customerID", customerID);
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity r_entity = response.getEntity();
            String xmlString = EntityUtils.toString(r_entity, "UTF-16");
            xmlString = new String(xmlString.getBytes());
            System.out.println(xmlString);
            return parseDataSys(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 取得資料
     *
     * @return Sys config
     */
    public static SystemConfig getCloudDataSystem(String customerID) {
        HttpClient client = new DefaultHttpClient();
        String url = SYS_CLOUD_URL.replace("customerID", customerID);
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity r_entity = response.getEntity();
            String xmlString = EntityUtils.toString(r_entity, "UTF-16");
            xmlString = new String(xmlString.getBytes());
            System.out.println(xmlString);
            return parse(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static SystemConfig parse(String xmlString) {
        SystemConfig config = new SystemConfig();

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
                if (size < 0) {
                    Log.e("Reponse Error!");
                }

                for (int i = 0; i < size; ++i) {
                    Node item = items.item(i);
                    if (item.getNodeName().equalsIgnoreCase("report")) {
                        config.report = Integer.parseInt(item.getFirstChild().getNodeValue());
                    } else if (item.getNodeName().equalsIgnoreCase("acc")) {
                        config.acc = Integer.parseInt(item.getFirstChild().getNodeValue());
                    } else if (item.getNodeName().equalsIgnoreCase("gender")) {
                        config.gender = item.getFirstChild().getNodeValue();
                    } else if (item.getNodeName().equalsIgnoreCase("lang")) {
                        config.lang = item.getFirstChild().getNodeValue();
                    } else if (item.getNodeName().equalsIgnoreCase("radius")) {
                        if (item.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) item;
                            Radius radius = Radius.parse(eElement);
                            if (radius != null && radius.type.equalsIgnoreCase("in"))
                                config.inRadius = radius;
                            else if (radius != null && radius.type.equalsIgnoreCase("out"))
                                config.outRadius = radius;
                        }
                    } else if (item.getNodeName().equalsIgnoreCase("audio")) {
                        Element eElement = (Element) item;

                        String type = eElement.getAttribute("type");
                        if (type.equalsIgnoreCase("go")) {
                            config.welcomeVoice = eElement.getAttribute("mp3");
                            config.speakWelcome = eElement.getAttribute("use").equalsIgnoreCase("1");
                        } else if (type.equalsIgnoreCase("stop")) {
                            config.stopVoice = eElement.getAttribute("mp3");
                            config.speakStop = eElement.getAttribute("use").equalsIgnoreCase("1");
                        }
                    } else if (item.getNodeName().equalsIgnoreCase("version")) {
                        if (item.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) item;
                            Version version = Version.parse(eElement);
                            if (version.type.equalsIgnoreCase("advert"))
                                config.advertVersion = version;
                            else if (version.type.equalsIgnoreCase("road")) {
                                config.roadVersion = version;
                            } else if (version.type.equalsIgnoreCase("welcome")) {
                                config.welcomeVersion = version;
                            } else if (version.type.equalsIgnoreCase("radius")) {
                                config.radiusVersion = version;
                            }
                        }
                    } else if (item.getNodeName().equalsIgnoreCase("rpm")) {
                        config.RPM = Integer.parseInt(item.getFirstChild().getNodeValue());
                    } else if (item.getNodeName().equalsIgnoreCase("accelerate")) {
                        config.accelerate = Integer.parseInt(item.getFirstChild().getNodeValue());
                    } else if (item.getNodeName().equalsIgnoreCase("decelerate")) {
                        config.decelerate = Integer.parseInt(item.getFirstChild().getNodeValue());
                    } else if (item.getNodeName().equalsIgnoreCase("halt")) {
                        config.halt = Integer.parseInt(item.getFirstChild().getNodeValue());
                    } else if (item.getNodeName().equalsIgnoreCase("movement")) {
                        config.movement = Integer.parseInt(item.getFirstChild().getNodeValue());
                    }
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(config.toString());
        return config;
    }
}
