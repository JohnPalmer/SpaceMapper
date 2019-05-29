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

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import net.movelab.cmlibrary.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Space Mapper's service for recording NMEA sentences.
 * <p/>
 * Dependencies: SpaceMapperBroadcastReceiver.java, FixGet.java, Settings.java,
 * Withdraw.java.
 * <p/>
 *
 * @author John R.B. Palmer
 */

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class NmeaGet extends Service {
    private LocationManager locationManager;
    GpsNmeaListener mGpsNmeaListener;
    StopReceiver stopReceiver;
    IntentFilter stopFilter;
    boolean NmeaInProgress = false;
    String TAG = "NmeaGet";
    String bestNmeaLocation = null;
    String bestNmeaLocationTime = null;
    float bestNmeaLocationHdop = -1f;
    Context context;

    @Override
    public void onCreate() {

        context = getApplicationContext();
        if (PropertyHolder.isInit() == false)
            PropertyHolder.init(context);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mGpsNmeaListener = null;

    }

    public void onStart(Intent intent, int startId) {

        if (NmeaInProgress == false) {
            NmeaInProgress = true;
            bestNmeaLocation = null;
            bestNmeaLocationTime = null;
            bestNmeaLocationHdop = -1;

            stopFilter = new IntentFilter(getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_STOP_FIXGET);
            stopReceiver = new StopReceiver();
            registerReceiver(stopReceiver, stopFilter);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && PropertyHolder.getShareData()) {

                mGpsNmeaListener = new GpsNmeaListener();
                locationManager.addNmeaListener(mGpsNmeaListener);
            }

        }

    }

    @Override
    public void onDestroy() {

        removeNmeaUpdates();

        NmeaInProgress = false;
        try {
            unregisterReceiver(stopReceiver);
        } catch (Exception e) {
// Log.e(TAG, "exception" + e);
        }

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

    private class GpsNmeaListener implements GpsStatus.NmeaListener {

        @Override
        public void onNmeaReceived(long time, String nmea_sentence) {

            if (PropertyHolder.getShareData() && nmea_sentence.contains("GGA")) {

                float thisHdop = -1;
                try {
                    thisHdop = Float.valueOf(nmea_sentence.split(",")[8]);

                } catch (Exception e) {

                } finally {

                    if (thisHdop >= 0) {

                        if (bestNmeaLocation == null
                                || bestNmeaLocationHdop == -1f) {
                            bestNmeaLocation = nmea_sentence;
                            bestNmeaLocationTime = Util.iso8601(time);
                            bestNmeaLocationHdop = thisHdop;

                        } else {

                            if (thisHdop < bestNmeaLocationHdop) {

                                bestNmeaLocation = nmea_sentence;
                                bestNmeaLocationTime = Util.iso8601(time);
                                bestNmeaLocationHdop = thisHdop;

                            }
                        }
                    }
                }
            }
        }

    }

    private void removeNmeaUpdates() {
        if (mGpsNmeaListener != null)
            locationManager.removeNmeaListener(mGpsNmeaListener);

    }

    public class StopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (bestNmeaLocationHdop >= 0 && bestNmeaLocation != null) {

                JSONObject nme_json_data = new JSONObject();
                String nme_json_data_string;

                try {

                    nme_json_data.put(DataCodeBook.NMEA_KEY_TIME, bestNmeaLocationTime);
                    nme_json_data.put(DataCodeBook.NMEA_KEY_LOCATION_MESSAGE, bestNmeaLocation);
                    nme_json_data.put(DataCodeBook.NMEA_KEY_HDOP, bestNmeaLocationHdop);
                    nme_json_data_string = nme_json_data.toString();
                    ContentResolver ucr = getContentResolver();
                    ucr.insert(Util.getUploadQueueUri(context), UploadContentValues
                            .createUpload(DataCodeBook.NMEA_PREFIX, nme_json_data_string));
                } catch (JSONException e) {
                    //todo
                }

            }

            removeNmeaUpdates();
            NmeaInProgress = false;
            stopSelf();

        }

    }

}