package com.hantek.ttia.module.polygonutils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class RegionDataFactory {
	static final String TAG = RegionDataFactory.class.getName();

	public static HashMap<Integer, Region> loadRegionData(Context context) {

		HashMap<Integer, Region> regionHashMap = new LinkedHashMap<Integer, Region>();
		Calendar tmpStartTime = Calendar.getInstance();
		int tmpRoadCount = 0;
		try {
			AssetManager am = context.getAssets();

			String[] tmpList = new String[] { "region.txt" };

			for (String path : tmpList) {
				InputStreamReader inputStreamReader = null;
				try {
					InputStream inputStream = am.open(path);
					inputStreamReader = new InputStreamReader(inputStream, "UTF-16");
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(inputStreamReader, inputStream.available());
						String readline = new String("");

						while ((readline = reader.readLine()) != null) {
							try {
								Region region = Region.Parse(readline);
								regionHashMap.put(region.ID, region);
							} catch (Exception e) {
								Log.e(TAG, String.format("Format:%s %s", path, e.getMessage()));
							}
						}

						tmpRoadCount++;
					} catch (Exception e) {
						Log.e(TAG, String.format("Format:%s %s", path, e.getMessage()));
					} finally {
						try {
							reader.close();
						} catch (Exception e) {

						}
					}
				} catch (Exception e) {
					Log.e(TAG, String.format("Load:%s %s", path, e.getMessage()));
					e.printStackTrace();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Calendar tmpEndTime = Calendar.getInstance();
		Log.d(TAG, String.format("Load Region Count:%s EOT:%04d", tmpRoadCount, tmpEndTime.getTimeInMillis() - tmpStartTime.getTimeInMillis()));

		return regionHashMap;
	}

	public static HashMap<Integer, Region> LoadUnserviceRegionData(Context context) {
		HashMap<Integer, Region> regionHashMap = new LinkedHashMap<Integer, Region>();
		Calendar tmpStartTime = Calendar.getInstance();
		int tmpRoadCount = 0;

		try {
			AssetManager am = context.getAssets();

			String[] tmpList = new String[] { "region_not_service.txt" };

			for (String path : tmpList) {
				InputStreamReader inputStreamReader = null;
				try {
					InputStream inputStream = am.open(path);
					inputStreamReader = new InputStreamReader(inputStream, "UTF-16");
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(inputStreamReader, inputStream.available());
						String readline = new String("");

						while ((readline = reader.readLine()) != null) {
							try {
								Region region = Region.Parse(readline);
								regionHashMap.put(region.ID, region);
							} catch (Exception e) {
								Log.e(TAG, String.format("Format:%s %s", path, e.getMessage()));
							}
						}

						tmpRoadCount++;
					} catch (Exception e) {
						Log.e(TAG, String.format("Format:%s %s", path, e.getMessage()));
					} finally {
						try {
							reader.close();
						} catch (Exception e) {

						}
					}
				} catch (Exception e) {
					Log.e(TAG, String.format("Load:%s %s", path, e.getMessage()));
					e.printStackTrace();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Calendar tmpEndTime = Calendar.getInstance();
		Log.d(TAG, String.format("Load Road Count:%s EOT:%04d", tmpRoadCount, tmpEndTime.getTimeInMillis() - tmpStartTime.getTimeInMillis()));

		return regionHashMap;
	}
}
