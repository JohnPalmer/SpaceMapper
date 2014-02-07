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
 * This file incorporates code from Funf, which is subject to the following terms: 
 * 
 * 		Funf: Open Sensing Framework
 *		Copyright (C) 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland. 
 * 		Acknowledgments: Alan Gardner
 * 		Contact: nadav@media.mit.edu
 * 
 * 		Funf is free software: you can redistribute it and/or modify
 * 		it under the terms of the GNU Lesser General Public License as 
 * 		published by the Free Software Foundation, either version 3 of 
 * 		the License, or (at your option) any later version. 
 * 
 * 		Funf is distributed in the hope that it will be useful, but 
 * 		WITHOUT ANY WARRANTY; without even the implied warranty of 
 * 		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * 		See the GNU Lesser General Public License for more details.
 * 
 */

package edu.princeton.jrpalmer.asmlibrary;

import java.util.Calendar;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import edu.princeton.jrpalmer.asmlibrary.Fix.Fixes;
import edu.princeton.jrpalmer.asmlibrary.Upload.Uploads;

/**
 * Uploads files to the server. The tryUploads() method is mostly taken from the
 * Funf code.
 * 
 * @author John R.B. Palmer
 * 
 */
public class FileUploader extends Service {
	// private static final String TAG = "FileUploader";
	private static boolean isFix;
	private boolean uploading = false;

	Context context;

	@Override
	public void onStart(Intent intent, int startId) {

		
		// Delete old data
		ContentResolver dcr = getContentResolver();
		Calendar cal = Calendar.getInstance();
		int currentDay = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH,
				currentDay - PropertyHolder.getStorageDays());
		dcr.delete(
				Util.getFixesUri(context),
				Fixes.KEY_STATION_DEPARTURE_TIMELONG + " < "
						+ cal.getTimeInMillis(), null);

		
		if (!uploading) {
			uploading = true;

			isFix = false;

			Thread uploadThread = new Thread(null, doFileUploading,
					"uploadBackground");
			uploadThread.start();

		}
	};

	private Runnable doFileUploading = new Runnable() {
		public void run() {
			tryUploads();
		}
	};

	@Override
	public void onCreate() {

		// Log.e(TAG, "FileUploader onCreate.");

		context = getApplicationContext();
		if (PropertyHolder.isInit() == false)
			PropertyHolder.init(context);
	}

	@Override
	public void onDestroy() {

	}

	private void tryUploads() {

		if (Util.isOnline(context)) {
			// Log.e(TAG, "FileUploader online.");

			ContentResolver cr = getContentResolver();

			Cursor c = cr.query(Util.getUploadQueueUri(context),
					Uploads.KEYS_ALL, null, null, null);

			if (!c.moveToFirst()) {
				c.close();
				return;
			}

			int idIndex = c.getColumnIndexOrThrow(Uploads.KEY_ROWID);
			int prefixIndex = c.getColumnIndexOrThrow(Uploads.KEY_PREFIX);
			int dataIndex = c.getColumnIndexOrThrow(Uploads.KEY_DATA);

			while (!c.isAfterLast()) {

				String prefix = c.getString(prefixIndex);
				isFix = prefix.equals("FIX");


				int thisId = c.getInt(idIndex);


				if (Util.uploadEncryptedString(context, prefix,
						c.getString(dataIndex), Util.SERVER)) {


					cr.delete(Util.getUploadQueueUri(context),
							Uploads.KEY_ROWID + " = " + String.valueOf(thisId),
							null);

					if (isFix) {

						// increment nUploads
						if (PropertyHolder.isInit() == false)
							PropertyHolder.init(context);
						int nUploads = PropertyHolder.incrementUploads();
						announceUpload(nUploads);

					}
				}

				c.moveToNext();

			}

			c.close();
		}

		if (!PropertyHolder.getProVersion()
				&& PropertyHolder.getNUploads() >= Util.UPLOADS_TO_PRO
				&& PropertyHolder.ptCheck() >= Util.TIME_TO_PRO) {
			PropertyHolder.setProVersion(true);
			PropertyHolder.setNeedsDebriefingSurvey(true);
			Util.createProNotification(context);

			ContentResolver ucr = getContentResolver();
			ucr.insert(Util.getUploadQueueUri(context),
					UploadContentValues.createUpload("PRO", "pro upgrade"));
			tryUploads();

		}

		uploading = false;

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void announceUpload(int nUploads) {
		Intent intent = new Intent(getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_FIX_UPLOADED);
		intent.putExtra("nUploads", nUploads);
		sendBroadcast(intent);
	}

}
