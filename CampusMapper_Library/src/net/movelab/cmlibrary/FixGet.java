/*
 * Campus Mobility is a mobile phone app for studying activity spaces on campuses. It is based in part on code from the Human Mobility Project.
 *
 * Copyright (c) 2015 John R.B. Palmer.
 *
 * Campus Mobility is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Campus Mobility is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses.
 *
 *
 * The code incorporated from the Human Mobility Project is subject to the following terms:
 *
 * Copyright 2010, 2011 Human Mobility Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.movelab.cmlibrary;

import java.util.Calendar;
import java.util.Date;

import net.movelab.cmlibrary.R;
import net.movelab.cmlibrary.Fix.Fixes;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Space Mapper's location recording service.
 * <p/>
 * Dependencies: TripInProgress.java, SpaceMapperBroadcastReceiver.java,
 * FixGet.java, TripInProgress.java, Settings.java, Withdraw.java.
 * <p/>
 * This class is a modified version of the one used in the Human Mobility
 * Project.
 *
 * @author Chang Y. Chung
 * @author Necati E. Ozgencil
 * @author Kathleen Li
 * @author John R.B. Palmer
 */

public class FixGet extends Service {
    // private static final String TAG = "FixGet";
    private LocationManager locationManager;
    private LocationListener locationListener1; // gps
    private LocationListener locationListener2; // network

    public static String NEW_RECORD = "newRecord";

    boolean gotLocation = false;
    Location bestLocation;
    Location bestGpsLocation;

    WifiLock wifiLock;
    WakeLock wakeLock;

    StopReceiver stopReceiver;
    IntentFilter stopFilter;

    boolean fixInProgress = false;

    boolean updating = false;

    public int extraRuns = Util.EXTRARUNS;

    private int minDist = 0;

    /**
     * Creates a new FixGet service instance.<br>
     * Begins location recording process. Creates a location manager and two
     * location listeners. Begins requesting updates from both the GPS and
     * network services, with one location listener receiving updates from one
     * provider.
     * <p/>
     * If either provider is unavailable, no updates will ever be returned to
     * the corresponding location listener.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {

        // Log.e("FixGet", "onCreate");

        Context context = getApplicationContext();

        if (PropertyHolder.isInit() == false)
            PropertyHolder.init(context);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        wifiLock = ((WifiManager) context
                .getSystemService(Context.WIFI_SERVICE)).createWifiLock(
                WifiManager.WIFI_MODE_SCAN_ONLY, "SpaceMapperWifiLock");

        wakeLock = ((PowerManager) context
                .getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "SpaceMapperScreenDimWakeLock");

        minDist = Util.getMinDist();

        // Log.e("FixGet", "minDist=" + minDist);
    }

    public void onStart(Intent intent, int startId) {

        Context context = getApplicationContext();

        saveCellData(context);

        if (Build.VERSION.SDK_INT >= 5 && PropertyHolder.getShareData()) {
            // intent to upload
            Intent i = new Intent(FixGet.this, NmeaGet.class);
            startService(i);
        }

        if (fixInProgress == false) {
            fixInProgress = true;
            stopFilter = new IntentFilter(getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_STOP_FIXGET);
            stopReceiver = new StopReceiver();
            registerReceiver(stopReceiver, stopFilter);

            // stopListening = null;
            bestLocation = null;

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


                locationListener1 = new mLocationListener();
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0, 0, locationListener1);

            }

            if (locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationListener2 = new mLocationListener();
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 0,
                        locationListener2);

            }

        }
    }

    ;

    /**
     * Destroy this FixGet service instance. Nothing else done.
     */
    @Override
    public void onDestroy() {
        removeLocationUpdates();


        try {
            unregisterReceiver(stopReceiver);
        } catch (Exception e) {
// Log.e(TAG, "exception" + e);
        }
        unWakeLock();

        // locationListener1 = null;
        // locationListener2 = null;
        // locationManager = null;
        fixInProgress = false;

    }

    /**
     * Returns Object that receives client interactions.
     *
     * @return The Object that receives interactions from clients.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    /**
     * Inner class to listen to LocationManager. <br>
     * Defines LocationListener behavior.
     */
    private class mLocationListener implements LocationListener {

        /**
         * Defines LocationListener behavior upon reception of a location fix
         * update from the LocationManager.
         */
        public void onLocationChanged(Location location) {

            Context context = getApplicationContext();

            // Quick return if given location is null or has an invalid time
            if ((location == null) || (location.getTime() < 0)
                    || ((location.getProvider() == "gps"))) {

                // Log.e(TAG, "garbage fix");
                return;
            } else {
                // Log.e(TAG, "not garbage");
                // Log.e(TAG, "accuracy=" + location.getAccuracy());

                // if the location is within the optimum accuracy (for either
                // normal or long runs),
                // then use it and stop.
                if ((location.getAccuracy() <= Util.OPT_ACCURACY || (Util.missedFixes > 0 && location
                        .getAccuracy() < Util.OPT_ACCURACY_LONGRUNS))) {

                    // Log.e(TAG, "locloop1");

                    useFix(context, location);

                    removeLocationUpdates();

                    // try {
                    // unregisterReceiver(stopReceiver);
                    // } catch (Exception e) {
                    // Log.e(TAG, "exception" + e);
                    // }

                    fixInProgress = false;

                    stopSelf();
                } else {

                    // Log.e(TAG, "locloop2");

                    // if no best location set yet, current location is best
                    if (bestLocation == null) {
                        // Log.e(TAG, "locloop3");

                        bestLocation = location;

                        return;
                        // current and best location are gps, use for new bets
                        // whichever is better
                    } else if (location.getProvider() == "gps"
                            && bestLocation.getProvider() == "gps"
                            && location.getAccuracy() < bestLocation
                            .getAccuracy()) {
                        // Log.e(TAG, "locloop4");

                        bestLocation = location;

                        return;

                        // if current location is gps and best location is
                        // network,
                        // use gps for new best if it is below the minimum gps
                        // accuracy or better than current
                    } else if (location.getProvider() == "gps"
                            && bestLocation.getProvider() == "network"
                            && (location.getAccuracy() <= Util.MIN_GPS_ACCURACY || location
                            .getAccuracy() < bestLocation.getAccuracy())) {
                        // Log.e(TAG, "locloop5");
                        bestLocation = location;

                        return;

                        // if current location is network and best is network,
                        // use
                        // for new best whichever is better
                    } else if (location.getProvider() == "network"
                            && bestLocation.getProvider() == "network"
                            && location.getAccuracy() < location.getAccuracy()) {
                        // Log.e(TAG, "locloop6");

                        bestLocation = location;

                        return;

                        // if current location is network and best is gps, use
                        // current as new best if gps accuracy is above the
                        // minimum threshhold or is better than network
                    } else if (location.getProvider() == "network"
                            && bestLocation.getProvider() == "gps"
                            && (bestLocation.getAccuracy() > Util.MIN_GPS_ACCURACY || location
                            .getAccuracy() < bestLocation.getAccuracy())) {
                        // Log.e(TAG, "locloop7");

                        bestLocation = location;

                        return;
                    } else
                        // if none of these conditions are met, then return and
                        // keep trying
                        // Log.e(TAG, "locloop8");

                        return;
                }
            }
        }

        /**
         * Defines behavior when the given provider is disabled.
         *
         * @param provider The provider to be disabled
         */
        public void onProviderDisabled(String provider) {
            removeLocationUpdate(provider);
            if (locationListener1 == null && locationListener2 == null) {
                removeLocationUpdates();
                // try {
                // unregisterReceiver(stopReceiver);
                // } catch (Exception e) {
                // Log.e(TAG, "exception" + e);
                // }

                fixInProgress = false;
                stopSelf();
            }
        }

        /**
         * Defines behavior when the given provider is re-enabled. Currently no
         * behavior is defined.
         *
         * @param provider The provider to be re-enabled
         */
        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            /*
             * If provider service is no longer available, stop trying to get
			 * updates from both providers and quit.
			 */
            if (status == LocationProvider.OUT_OF_SERVICE) {
                removeLocationUpdate(provider);
                if (locationListener1 == null && locationListener2 == null) {

                    removeLocationUpdates();

                    fixInProgress = false;

                    stopSelf();
                }
            }
        }

    }

    // utilities
    private void removeLocationUpdate(String provider) {
        if (locationManager != null) {
            LocationListener listener = provider == "gps" ? locationListener1
                    : locationListener2;
            if (listener != null)
                locationManager.removeUpdates(listener);
        }
    }

    private void removeLocationUpdates() {
        if (locationManager != null) {
            if (locationListener1 != null)
                locationManager.removeUpdates(locationListener1);
            if (locationListener2 != null)
                locationManager.removeUpdates(locationListener2);
        }
    }

    private void announceFix(Location location, boolean newRecord) {

        // reset missed fixes counter to zero
        Util.missedFixes = 0;
        Date usertime = new Date(location.getTime());
        Util.lastFixTimeStamp = Util.userDate(usertime);
        Util.lastFixTime = location.getTime();
        Util.lastFixLat = location.getLatitude();
        Util.lastFixLon = location.getLongitude();

        // inform the main display
        Context context = getApplicationContext();
        if (PropertyHolder.isInit() == false)
            PropertyHolder.init(context);
        Intent intent = new Intent(getResources().getString(
                R.string.internal_message_id)
                + Util.MESSAGE_FIX_RECORDED);
        Bundle bundle = new Bundle();
        bundle.putString("nFixes", String.valueOf(PropertyHolder.getNFixes()));
        bundle.putBoolean(NEW_RECORD, newRecord);
        bundle.putDouble("lat", location.getLatitude());
        bundle.putDouble("lon", location.getLongitude());
        bundle.putDouble("acc", location.getAccuracy());
        intent.putExtras(bundle);

        sendBroadcast(intent);
    }

    private void useFix(Context context, Location location) {

        // Log.e("FixGet", "useFix started");

        if (PropertyHolder.isInit() == false)
            PropertyHolder.init(context);

        if (PropertyHolder.getStoreMyData() == true) {

            ContentResolver cr = getContentResolver();

            float dist = -1;

            Cursor c = cr.query(Util.getFixesUri(context), Fixes.KEYS_LATLON,
                    null, null, null);

            int id = -1;

            if (c.moveToLast()) {

                int latCol = c.getColumnIndexOrThrow(Fixes.KEY_LATITUDE);
                int lonCol = c.getColumnIndexOrThrow(Fixes.KEY_LONGITUDE);
                int idCol = c.getColumnIndexOrThrow(Fixes.KEY_ROWID);

                float[] distResult = new float[1];

                id = c.getInt(idCol);

                Location.distanceBetween(c.getDouble(latCol),
                        c.getDouble(lonCol), location.getLatitude(),
                        location.getLongitude(), distResult);

                dist = distResult[0];

            }
            c.close();

            if (dist > minDist || dist == -1) {

                // create new entry with time and sdtime both set to the
                // location time. (We will update sdtime in next chunk of code
                // if the person
                // stays in same 50 m radius.)
                cr.insert(
                        Util.getFixesUri(context),
                        LocationContentValues.createFix(location,
                                location.getTime()));

                announceFix(location, true);

            } else if (id > 0) {

                ContentValues cv = new ContentValues();
                String sc = Fixes.KEY_ROWID + " = " + id;
                cv.put(Fixes.KEY_STATION_DEPARTURE_TIMELONG, location.getTime());
                cv.put(Fixes.KEY_ACCURACY, location.getAccuracy());
                cv.put(Fixes.KEY_PROVIDER, location.getProvider());

                cr.update(Util.getFixesUri(context), cv, sc, null);

                announceFix(location, false);

            }


        }

        if (PropertyHolder.getShareData()) {

            boolean ml = (!Secure.getString(getContentResolver(),
                    Secure.ALLOW_MOCK_LOCATION).equals("0"));

            // Log.e(TAG, "mock locations=" + ml);

            String prefix = ml ? DataCodeBook.FIX_PREFIX_POSSIBLE_MOCK_LOCATION : DataCodeBook.FIX_PREFIX_NORMAL;

            JSONObject fix_json_data = new JSONObject();
            String fix_json_data_string;
            try {
                fix_json_data.put(DataCodeBook.FIX_KEY_LAT, location.getLatitude());
                fix_json_data.put(DataCodeBook.FIX_KEY_LON, location.getLongitude());
                fix_json_data.put(DataCodeBook.FIX_KEY_ACCURACY, location.getAccuracy());
                fix_json_data.put(DataCodeBook.FIX_KEY_PROVIDER, location.getProvider());
                fix_json_data.put(DataCodeBook.FIX_KEY_TIME, Util.iso8601(location.getTime()));
                fix_json_data.put(DataCodeBook.FIX_KEY_POWER, PowerSensor.PowerLevel);
                fix_json_data_string = fix_json_data.toString();
                ContentResolver ucr = getContentResolver();
                ucr.insert(Util.getUploadQueueUri(context),
                        UploadContentValues.createUpload(prefix, fix_json_data_string));
            } catch (JSONException e) {
                //todo
            }


        }

        unWakeLock();

    }

    public void wakeUpAndWakeLock() {

        if (!wifiLock.isHeld()) {

            try {
                wifiLock.acquire();
                // Log.e(TAG, "wifilock set");

            } catch (Exception e) {
            }

        }
        if (!wakeLock.isHeld()) {

            try {
                wakeLock.acquire();
                // Log.e(TAG, "wakelock set");

            } catch (Exception e) {
            }

        }
    }

    public void unWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            // Log.e(TAG, "power wakelock released");

        }
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
            // Log.e(TAG, "wifi wakelock released");

        }
    }

    public void injectNewXTRA() {
        if (locationManager != null) {
            Bundle bundle = new Bundle();
            locationManager.sendExtraCommand("gps", "force_xtra_injection",
                    bundle);
            locationManager.sendExtraCommand("gps", "force_time_injection",
                    bundle);
        }
    }

    public void clearGPS() {
        if (locationManager != null)
            locationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,
                    "delete_aiding_data", null);
    }

    public class StopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Log.e(TAG, "stopReceiver triggered. ExtraRuns=" + extraRuns
            // + ", missedFixes=" + Util.missedFixes);

            if (extraRuns > 0
                    && Util.missedFixes > 0
                    && Util.missedFixes % 10 == 1
                    && locationManager != null
                    && locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                // Log.e("FixGet", "long run timer");

                // turn on screen to get network location woken up
                wakeUpAndWakeLock();

                // Check if online and if so try to inject new XTRA into GPS for
                // first extra run
                if (extraRuns == Util.EXTRARUNS) {

                    NetworkInfo netInfo = ((ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getActiveNetworkInfo();

                    if (netInfo != null && netInfo.isConnected()) {
                        injectNewXTRA();
                        // Log.e("FixGet", "XTRAinjection");
                    }
                }

                extraRuns = extraRuns - 1;
            } else {

                extraRuns = Util.EXTRARUNS;

                // stop both listeners if running
                if (locationManager != null && locationListener1 != null) {
                    locationManager.removeUpdates(locationListener1);
                    // Log.e(TAG, "gps listener stopped by timer");
                }
                if (locationManager != null && locationListener2 != null) {
                    locationManager.removeUpdates(locationListener2);
                    // Log.e(TAG, "network listener stopped by timer");
                }
                // use best location if one exists and if it is below minimum
                // threshhold
                if (bestLocation != null
                        && bestLocation.getAccuracy() < Util.MIN_ACCURACY) {

                    useFix(context, bestLocation);
                    // Log.e(TAG, "best location used with accuracy of "
                    // + bestLocation.getAccuracy());

                } else {

                    if (locationManager != null
                            && locationManager
                            .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Util.missedFixes = Util.missedFixes + 1;
                    }
                }

                // Log.e("FixGet", "short run stop");

                removeLocationUpdates();

                fixInProgress = false;
                stopSelf();

            }

        }
    }

    @SuppressLint("NewApi")
    private void saveCellData(Context context) {

        if (PropertyHolder.getShareData()) {

            String phoneTime = Util.iso8601(System.currentTimeMillis());
            String cid = "n";
            String lac = "n";
            String countryISO = "n";
            String sid = "n";
            String bid = "n";
            String nid = "n";
            String bsLat = "n";
            String bsLon = "n";

            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();
                if (loc != null) {
                    cid = Integer.toHexString(loc.getCid());
                    lac = Integer.toHexString(loc.getLac());
                }
                countryISO = tm.getNetworkCountryIso();
            }
            if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {

                if (Build.VERSION.SDK_INT >= 5) {
                    CdmaCellLocation loc = (CdmaCellLocation) tm
                            .getCellLocation();
                    if (loc != null) {
                        sid = Integer.toHexString(loc.getSystemId());
                        bid = Integer.toHexString(loc.getBaseStationId());
                        nid = Integer.toHexString(loc.getNetworkId());
                        bsLat = Integer.toHexString(loc.getBaseStationLatitude());
                        bsLon = Integer.toHexString(loc.getBaseStationLongitude());
                    }
                }
            }

            if (cid.equals("n") && lac.equals("n") && countryISO.equals("n")
                    && bid.equals("n") && nid.equals("n") && sid.equals("n")
                    && bsLat.equals("n") && bsLon.equals("n")) {
                // do nothing
            } else {

                JSONObject cell_json_data = new JSONObject();
                String cell_json_data_string;
                try {
                    cell_json_data.put(DataCodeBook.CELL_KEY_PHONE_TIME, phoneTime);
                    cell_json_data.put(DataCodeBook.CELL_KEY_CID, cid);
                    cell_json_data.put(DataCodeBook.CELL_KEY_LAC, lac);
                    cell_json_data.put(DataCodeBook.CELL_KEY_COUNTRY_ISO, countryISO);
                    cell_json_data.put(DataCodeBook.CELL_KEY_BID, bid);
                    cell_json_data.put(DataCodeBook.CELL_KEY_NID, nid);
                    cell_json_data.put(DataCodeBook.CELL_KEY_SID, sid);
                    cell_json_data.put(DataCodeBook.CELL_KEY_BS_LAT, bsLat);
                    cell_json_data.put(DataCodeBook.CELL_KEY_BS_LON, bsLon);

                    cell_json_data_string = cell_json_data.toString();

                    ContentResolver ucr = getContentResolver();
                    ucr.insert(Util.getUploadQueueUri(context),
                            UploadContentValues.createUpload(DataCodeBook.CELL_PREFIX, cell_json_data_string));
                } catch (JSONException e) {
                    //todo
                }

            }
        }

    }
}