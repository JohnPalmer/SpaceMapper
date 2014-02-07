/*
 * Space Mapper
 * Copyright (C) 2012, 2013 John R.B. Palmer
 * Contact: jrpalmer@princeton.edu
 * 
 * This file is part of Space Mapper.
 * 
 * Space Mapper is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or (at 
 * your option) any later version.
 * 
 * Space Mapper is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with Space Mapper.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package edu.princeton.jrpalmer.asmlibrary;

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

/**
 * Space Mapper's service for recording NMEA sentences.
 * <p>
 * Dependencies: SpaceMapperBroadcastReceiver.java, FixGet.java, Settings.java,
 * Withdraw.java.
 * <p>
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

		stopReceiver = null;
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

		if (stopReceiver != null) {
			unregisterReceiver(stopReceiver);
		}
		// mGpsNmeaListener = null;
		// locationManager = null;
		NmeaInProgress = false;

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

				ContentResolver ucr = getContentResolver();
				ucr.insert(Util.getUploadQueueUri(context), UploadContentValues
						.createUpload("NME", bestNmeaLocationTime + ","
								+ bestNmeaLocation));

			}

			removeNmeaUpdates();
			NmeaInProgress = false;
			stopSelf();

		}

	}

}