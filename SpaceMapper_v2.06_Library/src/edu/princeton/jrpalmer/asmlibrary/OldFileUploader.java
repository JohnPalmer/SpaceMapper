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

import java.io.File;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Uploads files to the server. The tryUploads() method is mostly taken from the
 * Funf code.
 * 
 * @author John R.B. Palmer
 * 
 */
public class OldFileUploader extends Service {
	// private static final String TAG = "FileUploader";
	public static final String NEW_FIX_UPLOADED = "New_Fix_Uploaded";

	Context context;

	@Override
	public void onStart(Intent intent, int startId) {
		// Log.e(TAG, "FileUploader onStart.");

		Thread uploadThread = new Thread(null, doFileUploading,
				"uploadBackground");
		uploadThread.start();

		PropertyHolder.setUploadOldFiles(false);

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

		File[] files = null;

		if (Util.isOnline(context)) {

			files = Util.listFiles(context);

			if (files != null && files.length > 0) {
				for (File f : files) {
					if (f.getName().endsWith(Util.EXTENSION)) {
						ContentResolver ucr = getContentResolver();
						if (f.getName().startsWith("CON"))
							ucr.insert(Util.getUploadQueueUri(context),
									UploadContentValues.createUpload("CON",
											PropertyHolder.getConsentTime()));

						if (f.getName().startsWith("WIT"))
							ucr.insert(Util.getUploadQueueUri(context),
									UploadContentValues.createUpload("WIT",
											"withdraw"));

						f.delete();
					}

				}

			}
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
