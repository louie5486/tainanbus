package com.hantek.ttia.module.polygonutils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.util.Log;

/**
 * N個多邊形判斷
 *
 * @author wsh
 */
public class Polygon {
    private static final String TAG = Polygon.class.getName();

    private static Polygon region = new Polygon();
    private HashMap<Integer, Region> regionHashMap = new LinkedHashMap<Integer, Region>();

    /* 前一次位置 -1:未知 */
    private int prevRegionID = -1;

    /* 判斷進入位置 */
    private int lastRegion = -1;

    private int checkCounter = 0;

    private PolygonInterface polygonInterface;
    // private Region prevRegion = null;

    private int noSatelliteCounter = 0;

    public Polygon() {

    }

    public static Polygon getInstance() {
        return region;
    }

    public void setInterface(PolygonInterface polygon) {
        this.polygonInterface = polygon;
    }

    public void setRegionData(HashMap<Integer, Region> regionHashMap) {
        this.regionHashMap = regionHashMap;
    }

    public void check(double longitude, double lateitude, boolean fixed) {
        Calendar startCheck = Calendar.getInstance();

        if (this.polygonInterface == null || this.regionHashMap == null) {
            Log.w(TAG, "polygon null interface");
            return;
        }

        if (!fixed) {
            this.noSatelliteCounter += 1;
            // 沒訊號 不知道位置 歸零
            if (this.noSatelliteCounter > 30) {
                this.noSatelliteCounter = 0;
                this.checkCounter = 0;
                Log.w(TAG, "Out of order, Reset.");
            }
            return;
        } else {
            this.noSatelliteCounter = 0;
        }

        Point checkPoint = new Point();
        checkPoint.x = longitude;
        checkPoint.y = lateitude;

        // 找出目前位置
        Region currentRegion = new Region();
        currentRegion.ID = -1;
        for (Region region : this.regionHashMap.values()) {
            if (isInSidePolygon(region.pointList, checkPoint)) {
                currentRegion = region;
                Log.d(TAG, "Current location id: " + currentRegion.ID + " region size:" + currentRegion.pointList.size());
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Polygon check counter=" + this.checkCounter);
        sb.append(" curr=" + currentRegion.ID);
        sb.append(" last=" + this.lastRegion);
        sb.append(" prev=" + this.prevRegionID);

        Log.d(TAG, sb.toString());

        // 不在任何區域
        if (currentRegion.ID == -1) {
            if (this.prevRegionID != -1) {
                this.prevRegionID = -1;
                this.checkCounter = 0;
            } else {
                this.checkCounter++;
            }

            if (this.checkCounter > 15) {
                this.checkCounter = 0;

                // 沒有上一次記錄
                if (this.lastRegion != -1) {
                    Log.d(TAG, "===== leave ====");
                    this.polygonInterface.leaveRegion(this.regionHashMap.get(this.lastRegion));
                } else {
                    Log.d(TAG, "===== not real leave ====");
                }

                this.lastRegion = -1;
            }
        } else {
            if (this.prevRegionID == -1) {
                this.prevRegionID = currentRegion.ID;
                this.checkCounter = 0;
            } else if (this.prevRegionID == currentRegion.ID) {
                this.checkCounter++;
            } else {
                this.checkCounter = 0;
            }

            if (this.checkCounter > 15) {
                this.checkCounter = 0;

                // 沒有上一次記錄, 可能為重開機 , 不處理
                if (this.lastRegion != -1) {
                    Log.d(TAG, "===== enter ====");
                    this.polygonInterface.enterRegion(currentRegion);
                } else {
                    Log.d(TAG, "===== not real enter ====");
                }

                this.lastRegion = currentRegion.ID;
            }
        }

//		Log.d(TAG, String.format("Check EOT:%s", Utility.dateDiffNow(startCheck)));
    }

    private boolean isInSidePolygon(List<Point> pointList, Point currentPoint) {
        if (pointList.size() == 0) {
            return false;
        }

        int counter = 0;
        double xinters;
        Point p1;
        Point p2;

        p1 = pointList.get(0);
        for (int i = 1; i < pointList.size(); i++) {
            p2 = pointList.get(i);
            if (currentPoint.y > Math.min(p1.y, p2.y)) {
                if (currentPoint.y <= Math.max(p1.y, p2.y)) {
                    if (currentPoint.x <= Math.max(p1.x, p2.x)) {
                        if (p1.y != p2.y) {
                            xinters = (currentPoint.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x;
                            if ((p1.x == p2.x) || currentPoint.x <= xinters) {
                                counter += 1;
                            }
                        }
                    }
                }
            }
        }

        if (counter % 2 == 0) {
            return false;
        } else {
            return true;
        }
    }
}
