package com.hantek.ttia.module.advertutils;


import org.w3c.dom.Element;

import java.util.Arrays;

public class Advert {
    public String folderName;
    public String[] fileItem;
    public String version;

    public Advert() {
        folderName = "";
        version = "";
    }

    public static Advert parse(Element eElement) {
        Advert advert = new Advert();
        advert.folderName = eElement.getAttribute("name");
        advert.fileItem = eElement.getElementsByTagName("file").item(0).getTextContent().split("\\|");
        return advert;
    }

    @Override
    public String toString() {
        return "Advert{" +
                "fileItem=" + Arrays.toString(fileItem) +
                ", folderName='" + folderName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
