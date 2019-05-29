
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

import net.movelab.cmlibrary.R;
import net.movelab.cmlibrary.Fix.Fixes;
import net.movelab.cmlibrary.Upload.Uploads;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

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

            JSONObject pro_json_data = new JSONObject();
            String pro_json_data_string;
            try{
                pro_json_data.put(DataCodeBook.PRO_UPGRADE_PRO_MESSAGE, "pro upgrade");
                pro_json_data_string = pro_json_data.toString();
			ContentResolver ucr = getContentResolver();
			ucr.insert(Util.getUploadQueueUri(context),
					UploadContentValues.createUpload(DataCodeBook.PRO_UPGRADE_PREFIX, pro_json_data_string));
            } catch(JSONException e){
                //todo
            }

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
