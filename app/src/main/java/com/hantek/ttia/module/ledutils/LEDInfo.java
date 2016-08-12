package com.hantek.ttia.module.ledutils;

import org.w3c.dom.Element;

public class LEDInfo {
    static final String SPLIT_SIGN = "!@#";
    public int no;
    public int animation;
    public int stay;
    public int second;
    public int light;
    public int wait;
    public int type;
    public String lang;
    public String content;

    public LEDInfo() {
        lang = "zh";
        content = "";
    }

    public String getStorageFormat() {
        StringBuffer sb = new StringBuffer();
        sb.append(no);
        sb.append(SPLIT_SIGN);

        sb.append(animation);
        sb.append(SPLIT_SIGN);

        sb.append(stay);
        sb.append(SPLIT_SIGN);

        sb.append(second);
        sb.append(SPLIT_SIGN);

        sb.append(light);
        sb.append(SPLIT_SIGN);

        sb.append(wait);
        sb.append(SPLIT_SIGN);

        sb.append(type);
        sb.append(SPLIT_SIGN);

        sb.append(content);
        sb.append(SPLIT_SIGN);

        sb.append(lang);
        return sb.toString();
    }

    public static LEDInfo parse(String data) {
        String[] tmp = data.split(SPLIT_SIGN);
        LEDInfo info = new LEDInfo();
        info.no = Integer.parseInt(tmp[0]);
        info.animation = Integer.parseInt(tmp[1]);
        info.stay = Integer.parseInt(tmp[2]);
        info.second = Integer.parseInt(tmp[3]);
        info.light = Integer.parseInt(tmp[4]);
        info.wait = Integer.parseInt(tmp[5]);
        info.type = Integer.parseInt(tmp[6]);
        if (tmp.length > 7)
            info.content = tmp[7].trim();
        if (tmp.length > 8)
            info.lang = tmp[8].trim();
        return info;
    }

    public static LEDInfo parse(Element eElement) {
        LEDInfo info = new LEDInfo();
        info.no = Integer.parseInt(eElement.getAttribute("no"));
        info.animation = Integer.parseInt(eElement.getAttribute("animation"));
        info.stay = Integer.parseInt(eElement.getAttribute("stay"));
        info.second = Integer.parseInt(eElement.getAttribute("second"));
        info.light = Integer.parseInt(eElement.getAttribute("light"));
        info.wait = Integer.parseInt(eElement.getAttribute("wait"));
        info.type = Integer.parseInt(eElement.getAttribute("type"));
        if (eElement.hasAttribute("lang"))
            info.lang = eElement.getAttribute("lang");
        info.content = eElement.getTextContent();

        return info;
    }

    @Override
    public String toString() {
        return "LEDInfo{" +
                "animation=" + animation +
                ", no=" + no +
                ", stay=" + stay +
                ", second=" + second +
                ", light=" + light +
                ", wait=" + wait +
                ", type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
