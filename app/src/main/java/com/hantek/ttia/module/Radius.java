package com.hantek.ttia.module;

import org.w3c.dom.Element;

public class Radius {
    static final String SPLIT_SIGN = "!@#";
    public String type;
    public String mp3;
    public int distance;
    public int delay;
    public int mp3first;

    public static Radius parse(Element eElement) {
        try {
            Radius radius = new Radius();
            radius.type = eElement.getAttribute("type");
            radius.distance = Integer.parseInt(eElement.getAttribute("distance"));
            radius.mp3 = eElement.getAttribute("mp3");
            radius.delay = Integer.parseInt(eElement.getAttribute("delay"));
            radius.mp3first = Integer.parseInt(eElement.getAttribute("mp3first"));
            return radius;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Radius parse(String data) {
        try {
            Radius radius = new Radius();
            String[] tmp = data.split(SPLIT_SIGN);
            radius.type = tmp[0];
            radius.distance = Integer.parseInt(tmp[1]);
            radius.mp3 = tmp[2];
            radius.mp3first = Integer.parseInt(tmp[3]);
            radius.delay = Integer.parseInt(tmp[4]);
            return radius;
        } catch (Exception e) {
            System.out.println("Radius parse: " + data);
            e.printStackTrace();
            return null;
        }
    }

    public String getSharedPrefFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(SPLIT_SIGN);
        sb.append(distance);
        sb.append(SPLIT_SIGN);
        sb.append(mp3);
        sb.append(SPLIT_SIGN);
        sb.append(mp3first);
        sb.append(SPLIT_SIGN);
        sb.append(delay);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Radius{" +
                "delay=" + delay +
                ", type='" + type + '\'' +
                ", mp3='" + mp3 + '\'' +
                ", distance=" + distance +
                ", mp3first=" + mp3first +
                '}';
    }
}
