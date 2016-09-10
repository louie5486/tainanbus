package com.hantek.ttia.module.gpsutils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.hantek.ttia.module.Utility;

import component.LogManager;

public class GpsReceiver implements NmeaListener, LocationListener, Listener {
    private static final String TAG = GpsReceiver.class.getName();

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static GpsReceiver gps = new GpsReceiver();

    private LocationManager locationManager;

    private int satelliteNumber = 0;
    private String status = "V";
    private String nmeaLat = "0.0";
    private String latitudeQuadrant = " ";
    private String nmeaLon = "0.0";
    private String longitudeQuadrant = " ";
    private double speed = 0; // knot
    private double angle = 0;
    private Date gpsTime = new Date(); // utc time
    private Calendar lastReceiveNmeaGpsTime = Calendar.getInstance();

    private double latitude = 0.0d;
    private double longitude = 0.0d;

    private Location currentBestLocation = null;
    private boolean gpsEnable = false;

    private boolean logGPS = false;

    public static GpsReceiver getInstance() {
        return gps;
    }

    /**
     * @return the currentBestLocation
     */
    public Location getCurrentBestLocation() {
        return currentBestLocation;
    }

    /**
     * @return the Satellite number
     */
    public int getSatelliteNumber() {
        return satelliteNumber;
    }

    /**
     * @return the status 'A','V',''
     */
    public String getRawStatus() {
        if (Utility.dateDiffNow(GpsReceiver.getInstance().getLastReceiveTime()) > 60000) {
            return "V";
        }

        return status;
    }

    /**
     * @return the nmea latitude
     */
    public String getNmeaLatitude() {
        return nmeaLat;
    }

    /**
     * @return the latitude, 23.45678
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * @return the latitude 'N','S'
     */
    public String getLatitudeQuadrant() {
        return latitudeQuadrant;
    }

    /**
     * @return the nmea longitude
     */
    public String getNmeaLongitude() {
        return nmeaLon;
    }

    /**
     * @return the longitude, 123,456789
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * @return the longitude 'E','W
     */
    public String getLongitudeQuadrant() {
        return longitudeQuadrant;
    }

    /**
     * @return the speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * @return the angle
     */
    public double getAngle() {
        return angle;
    }

    public Date getUTCTime() {
        return gpsTime;
    }

    public String getTime() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isoFormatter.setTimeZone(utc);
        return isoFormatter.format(gpsTime); // String.format("%tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", gpsTime);
    }

    public boolean isFixed() {
        // return (this.status.equalsIgnoreCase("A")) && this.satelliteNumber >= 3;
        return getRawStatus().equalsIgnoreCase("A") && this.satelliteNumber >= 3;
    }

    public boolean isEnable() {
        return gpsEnable;
    }

    public void setLogGps(boolean logGPS) {
        this.logGPS = logGPS;
    }

    public void startListener(Context context) {
        Object lm = context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null)
            return;

        // Acquire a reference to the system Location Manager
        this.locationManager = (LocationManager) lm;

        // for (String s : locationManager.getAllProviders()) {
        // Log.i(TAG, "getAllProviders:" + s);
        // }

        // Get Network location, set to current location
        Location lastLocationNetwork = this.getLastLocation(LocationManager.NETWORK_PROVIDER);
        if (lastLocationNetwork != null) {
            Log.i(TAG, "lastLocationNetwork:" + printLocation(lastLocationNetwork));
            if (this.isBetterLocation(lastLocationNetwork, this.currentBestLocation)) {
                this.updateLocation(lastLocationNetwork);
            }
        } else {
            Log.w(TAG, "lastLocationNetwork == null");
        }

        // Get GPS location, set to current location
        Location lastLocationGPS = this.getLastLocation(LocationManager.GPS_PROVIDER);
        if (lastLocationGPS != null) {
            Log.i(TAG, "lastLocationGPS:" + printLocation(lastLocationGPS));
            if (this.isBetterLocation(lastLocationGPS, this.currentBestLocation)) {
                this.updateLocation(lastLocationGPS);
            }
        } else {
            Log.w(TAG, "lastLocationGPS == null");
        }

        // Register the listener with the Location Manager to receive location updates
        String provider = this.locationManager.getBestProvider(new Criteria(), true);
//        if (provider == null) {
//            Log.w(TAG, "provider == null");
//        } else {
//            locationManager.requestLocationUpdates(provider, 1000, 0, this);
//        }

        locationManager.addNmeaListener(this);
        locationManager.addGpsStatusListener(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i(TAG, "start listen - Best:" + provider + " GPS:" + gpsEnable);
    }

    public void stopListener() {
        if (this.locationManager != null) {
            locationManager.removeNmeaListener(this);
            locationManager.removeGpsStatusListener(this);
            locationManager.removeUpdates(this);
            Log.i(TAG, "stop lister");
        } else {
            Log.w(TAG, "locationManager == null");
        }

        locationManager = null;
        gpsEnable = false;
    }

    /**
     * 判斷GPS是否開啟
     */
    public boolean isOpen() {
        if (this.locationManager != null) {
            boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps || network) {
                return true;
            }
        }

        return false;
    }

    public Calendar getLastReceiveTime() {
        return this.lastReceiveNmeaGpsTime;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param newerLocation       The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location newerLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            Log.d(TAG, "First Location Lat:" + newerLocation.getLatitude());
            Log.d(TAG, "First Location Lon:" + newerLocation.getLongitude());
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newerLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newerLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newerLocation.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void updateLocation(Location location) {
        this.currentBestLocation = location;

//         this.speed = this.currentBestLocation.getSpeed();
//        this.latitude = this.currentBestLocation.getLatitude();
//        this.longitude = this.currentBestLocation.getLongitude();
//         this.angle = this.currentBestLocation.getBearing();
//        this.gpsTime = this.utcTimeToDate(this.currentBestLocation.getTime());

        // NMEAGPRMCTime(this.gpsTime);
        // NMEAGPRMCDate(this.gpsTime);
        // Log.d(TAG, "Update: " + printLocString(this.currentBestLocation));
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        try {
            if (this.logGPS)
                LogManager.write("nmea", nmea, null);
            if (Utility.dateDiffNow(GpsReceiver.getInstance().getLastReceiveTime()) > 2000) {
                LogManager.write("debug", "delay," + nmea, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] aList;
        try {
            aList = nmea.split(",");
            if (aList[0].equalsIgnoreCase("$GNGGA") || aList[0].equalsIgnoreCase("$GPGGA")) {
                // MS5668 USE "GNGGA", Normal device USE "GPGGA".
            } else if (aList[0].equalsIgnoreCase("$GNRMC") || aList[0].equalsIgnoreCase("$GPRMC")) {
                // MS5668 USE "GNRMC", Normal device USE "GPRMC".
                this.lastReceiveNmeaGpsTime = Calendar.getInstance();
//                System.out.println("GGPRMC: " + nmea);
                GPRMC tmp = GPRMC.parse(nmea);
                if (tmp != null) {
                    doRMC(tmp);
                } else {
                    LogManager.write("nmeaerr", nmea, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRMC(GPRMC rmc) {
        this.status = rmc.getStatus();
        this.nmeaLat = rmc.getNmeaLat();
        this.latitude = rmc.getLatitude();
        this.latitudeQuadrant = rmc.getLatitudeQuadrant();
        this.nmeaLon = rmc.getNmeaLon();
        this.longitude = rmc.getLongitude();
        this.longitudeQuadrant = rmc.getLongitudeQuadrant();
        this.speed = rmc.getSpeed();
        this.angle = rmc.getAngle();
        this.gpsTime = rmc.getGpsTime();
    }

    @Override
    public void onLocationChanged(Location newerLocation) {
        try {
            if (this.isBetterLocation(newerLocation, this.currentBestLocation)) {
                this.updateLocation(newerLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
//                Log.w(TAG, " Status Changed provider: " + provider + ", status: OUT_OF_SERVICE.");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                Log.w(TAG, " Status Changed provider: " + provider + ", status: TEMPORARILY_UNAVAILABLE.");
                break;
            case LocationProvider.AVAILABLE:
//                Log.w(TAG, " Status Changed provider: " + provider + ", status: AVAILABLE.");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.w(TAG, " Enabled provider: " + provider);
        gpsEnable = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.w(TAG, " Disabled provider: " + provider);
        gpsEnable = false;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                try {
                    if (locationManager == null)
                        return;
                    GpsStatus currentGpsStatus = locationManager.getGpsStatus(null);
//                     int maxSatellite = currentGpsStatus.getMaxSatellites();
                    Iterator<GpsSatellite> iterator = currentGpsStatus.getSatellites().iterator();
                    int tmpCount = 0;
                    int total = 0;
                    while (iterator.hasNext()) {
                        GpsSatellite s = iterator.next();
                        boolean used = s.usedInFix();

                        if (used) {
                            tmpCount++;
                        }
                        total++;
                        // String str = printSatellite(s);
                        // Log.v(TAG, String.format("Count: %s, %s", tmpCount, str));
                    }

                    //DEBUG
                    if (tmpCount != this.satelliteNumber)
                        Log.d(TAG, String.format("Max:%02d, Curr:%02d, Num:%02d.", total, tmpCount, satelliteNumber));

                    this.satelliteNumber = tmpCount;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * debug
     *
     * @param lon 120.123456
     */
    public void setLon(double lon) {
        this.nmeaLon = String.valueOf(du2Nmea(lon));
        this.longitude = lon;
        this.longitudeQuadrant = "E";
    }

    /**
     * debug
     *
     * @param lat 23.123456
     */
    public void setLat(double lat) {
        this.nmeaLat = String.valueOf(du2Nmea(lat));
        this.latitude = lat;
        this.latitudeQuadrant = "N";
    }

    /**
     * debug
     *
     * @param speed 0.0
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * debug
     *
     * @param status A,V
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * debug
     *
     * @param direct 0.0
     */
    public void setAngle(double direct) {
        this.angle = direct;
    }

    /**
     * debug
     *
     * @param time date
     */
    public void setTime(Date time) {
//        Log.d(TAG, "setTime1:" + time.toString());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(time);
//        Log.d(TAG, "setTime2:" + calendar.getTime().toString());

        this.gpsTime = calendar.getTime();
//        Log.d(TAG, "setTime2:" + this.gpsTime.toString());
    }

    public void setSatelliteNumber(int satelliteNumber) {
        this.satelliteNumber = satelliteNumber;
        this.lastReceiveNmeaGpsTime = Calendar.getInstance();
    }

    private double DM2DD(String astr) {
        try {
            String[] aList;
            aList = astr.split("\\.");
            double d1 = Double.parseDouble(aList[0].substring(0, aList[0].length() - 2)) + Double.parseDouble(aList[0].substring(aList[0].length() - 2) + "." + aList[1]) / 60;

            DecimalFormat df = new DecimalFormat("###.0000000");
            d1 = Double.parseDouble(df.format(d1));
            return d1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static double du2Nmea(double du) {
        try {
            int integer = (int) du;
            double value = du - integer;
            return integer * 100 + value * 60;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private Location getLastLocation(String provider) {
        Location lastLocation = null;
        if (locationManager.getAllProviders().contains(provider)) {
            if (locationManager.isProviderEnabled(provider)) {
                lastLocation = locationManager.getLastKnownLocation(provider);
            } else {
                Log.w(TAG, String.format("LocationManager NOT Enabled %s.", provider));
            }
        } else {
            Log.w(TAG, String.format("LocationManager NOT supprot %s.", provider));
        }

        return lastLocation;
    }

    private String printLocation(Location loc) {
        String s = String.format("Provider=%s Time(ms)=%s, Time(UTC)=%s, Lat=%s, Lon=%s, Accuracy=%s, Altitude=%s, Angle=%s, Speed=%s.", loc.getProvider(), loc.getTime(), utcTimeToDate(loc.getTime()),
                loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), loc.getAltitude(), loc.getBearing(), loc.getSpeed());
        return s;
    }

    private String printSatellite(GpsSatellite satellite) {
        String s = "GpsSatellite: ";
        s = String.format("hasEphemeris=%s, hasAlmanac=%s, getAzimuth=%s, getElevation=%s, getPrn=%s, getSnr=%s.", satellite.hasEphemeris(), satellite.hasAlmanac(), satellite.getAzimuth(),
                satellite.getElevation(), satellite.getPrn(), satellite.getSnr());
        return s;
    }

    private Date utcTimeToDate(long milliseconds) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(milliseconds);
        return c.getTime();
    }

    private String NMEAGPRMCTime(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String result = sdf.format(d);
        Log.d(TAG, result);
        return result;
    }

    private String NMEAGPRMCDate(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String result = sdf.format(d);
        Log.d(TAG, result);

        return result;
    }
}
