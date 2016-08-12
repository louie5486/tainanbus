package com.hantek.ttia.module;

import org.w3c.dom.Element;

public class Version {
    static final String SPLIT_SIGN = "$%#";
    public String no;
    public String type;
    public String lastUpdateTime;

    public static Version parse(Element eElement) {
        Version version = new Version();
        version.type = eElement.getAttribute("type");
        version.no = eElement.getAttribute("no");
        version.lastUpdateTime = eElement.getTextContent();
        return version;
    }

    public String getSharedPrefFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(SPLIT_SIGN);
        sb.append(no);
        sb.append(SPLIT_SIGN);
        sb.append(lastUpdateTime);
        return sb.toString();
    }

    public static Version parse(String data) {
        try {
            Version version = new Version();
            String[] tmp = data.split("");
            version.type = tmp[0];
            version.no = tmp[1];
            version.lastUpdateTime = tmp[2];
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Version{" +
                "lastUpdateTime='" + lastUpdateTime + '\'' +
                ", no='" + no + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
