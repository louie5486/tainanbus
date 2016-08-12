package com.hantek.ttia.module.polygonutils;

import java.util.ArrayList;
import java.util.List;

public class Region {
	public int ID;

	public List<Point> pointList;

	public Region() {
		this.pointList = new ArrayList<Point>();
	}

	public static Region Parse(String data) {
		Region region = new Region();
		String[] tmp = data.split("\\^");
		region.ID = Integer.parseInt(tmp[0]);

		String[] loc = tmp[1].trim().split("/");
		Point p1 = new Point();
		for (int i = 0; i < loc.length; i += 2) {
			Point point = new Point();
			point.x = Double.parseDouble(loc[i].trim());
			point.y = Double.parseDouble(loc[i + 1].trim());

			if (i == 0) {
				p1.x = point.x;
				p1.y = point.y;
			}
			region.pointList.add(point);
		}

		region.pointList.add(p1);
		return region;
	}
}
