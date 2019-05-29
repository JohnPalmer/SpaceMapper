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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import net.movelab.cmlibrary.R;
import net.movelab.cmlibrary.Fix.Fixes;

/**
 * Activity and custom surfaceview for displaing lifeline graphics using OpenGL
 * ES 1.0.
 * 
 * @author John R.B. Palmer
 * 
 */

public class Lifeline extends Activity {

	ArrayList<Float> pathCoords;
	float[] pcs;
	ProgressBar progressbar;

	public static int SPEED_CHECK_MAX = 500;

	public static float spaceScale = 1;
	public static float timeScale = 1;

	public static String lastFixTime = null;
	public static String firstFixTime = null;

	public static double lastFixX = 0;
	public static double lastFixY = 0;

	public static double lastLat = 0;
	public static double lastLon = 0;
	public static double lastTim = 0;

	public static float XsMid = 0;

	public static float YsMid = 0;

	public static long maxStationDuration = 0;

	public static float timsMid = 0;

	public static float[] lineCoordsImp;
	public static ArrayList<Float> lineList;
	private LifelineGLSurfaceView mGLView;
	private LifelineRenderer mRenderer;
	private ZoomControls mZoomControls;
	Context context;
	public int outcome;
	Handler toastHandler;
	Runnable toastRunnable;
	String toastText;
	boolean zaxToggle;
	boolean xyaxToggle;
	private static final long ZOOM_SPEED = 5;
	TextView mFirstFixTime;

	TextView mLastFixTime;

	boolean dataloaded = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		zaxToggle = false;
		xyaxToggle = false;

		context = getApplicationContext();

		setContentView(R.layout.progress_bar);

		progressbar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);
		progressbar.setProgress(0);

		toastHandler = new Handler();
		toastRunnable = new Runnable() {
			public void run() {
				if (outcome == 1 && mRenderer.mFilename != null) {
					toastText = getResources().getString(R.string.data_saved)
							+ " " + mRenderer.mFilename;
				}
				if (outcome == 3) {
					toastText = getResources().getString(
							R.string.data_SD_unavailable);
				}

				if (outcome == 2) {
					toastText = getResources()
							.getString(R.string.data_SD_error);
				}

				Toast.makeText(Lifeline.this, toastText, Toast.LENGTH_SHORT)
						.show();
			}
		};

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (dataloaded) {
			mGLView.onPause();
		}
	}

	@Override
	protected void onResume() {

		if (Util.trafficCop(this))
			finish();

		super.onResume();
		new LifelineDataGrabberTask().execute(context);
	}

	public static float[] convertFloats(List<Float> floats) {
		float[] ret = new float[floats.size()];
		Iterator<Float> iterator = floats.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().floatValue();
		}
		return ret;
	}

	static final private int TOGGLE_ZAX = Menu.FIRST;
	static final private int TOGGLE_XYAX = Menu.FIRST + 1;
	static final private int SAVE_LL = Menu.FIRST + 2;
	static final private int SHARE_LL = Menu.FIRST + 3;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, SAVE_LL, Menu.NONE, R.string.menu_map_save);
		menu.add(0, SHARE_LL, Menu.NONE, R.string.menu_map_share);
		menu.add(0, TOGGLE_ZAX, Menu.NONE, R.string.toggle_zax);
		menu.add(0, TOGGLE_XYAX, Menu.NONE, R.string.toggle_xyax);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (SAVE_LL): {
			outcome = 0;

			mRenderer.startScreenShot(new Handler() {

				public void handleMessage(Message msg) {

					if (msg.what == LifelineRenderer.SCREENSHOT_DONE) {
						outcome = 1;

						toastHandler.post(toastRunnable);

					}
					if (msg.what == LifelineRenderer.SCREENSHOT_SD_ERROR) {
						outcome = 2;
						toastHandler.post(toastRunnable);

					}
					if (msg.what == LifelineRenderer.SCREENSHOT_SD_UNAVAIL) {
						outcome = 3;
						toastHandler.post(toastRunnable);

					}

				}

			});

			return true;
		}
		case (SHARE_LL): {
			outcome = 0;

			mRenderer.startScreenShot(new Handler() {

				public void handleMessage(Message msg) {

					if (msg.what == LifelineRenderer.SCREENSHOT_DONE) {
						outcome = 1;
						Context context = getApplicationContext();

						shareLL(context);

					}
					if (msg.what == LifelineRenderer.SCREENSHOT_SD_ERROR) {
						outcome = 2;
						toastHandler.post(toastRunnable);

					}

				}

			});

			return true;
		}

		case (TOGGLE_ZAX): {

			zaxToggle = !zaxToggle;

			mRenderer.showZax = zaxToggle;

			return true;
		}

		case (TOGGLE_XYAX): {

			xyaxToggle = !xyaxToggle;

			mRenderer.showXYax = xyaxToggle;

			return true;
		}

		}
		return false;
	}

	private void shareLL(Context context) {

		File root = Environment.getExternalStorageDirectory();
		File directory = new File(root, "SpaceMapper");
		if (mRenderer.mFilename != null) {
			File f = new File(directory, mRenderer.mFilename);
			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("image/jpeg");
			share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f)); // add the
																	// message
			share.putExtra(android.content.Intent.EXTRA_TEXT, getResources()
					.getText(R.string.made_with_SM)
					+ ": https://play.google.com/store/apps/details?id="
					+ getResources().getString(R.string.package_name));
			startActivity(Intent.createChooser(share,
					getResources().getText(R.string.share_with)));
		} else {
			outcome = 2;
			toastHandler.post(toastRunnable);
		}
	}

	public class LifelineDataGrabberTask extends
			AsyncTask<Context, Integer, Boolean> {

		int myProgress;

		@Override
		protected void onPreExecute() {

			myProgress = 0;
		}

		protected Boolean doInBackground(Context... context) {

			PropertyHolder.init(context[0]);

			ContentResolver cr = getContentResolver();
			Cursor c = cr.query(Util.getFixesUri(context[0]), Fixes.KEYS_ALL,
					null, null, null);

			int totalRecords = c.getCount();
			int currentRecord = 1;

			if (!c.moveToFirst()) {
				c.close();
				return false;
			}

			pathCoords = new ArrayList<Float>();

			long timsMin = 0;
			long timsMax = 0;

			while (!c.isAfterLast()) {
				myProgress = (int) ((currentRecord / (float) totalRecords) * 100);
				publishProgress(myProgress);

				// Escape early if cancel() is called
				if (isCancelled())
					break;

				// First grabbing double values of lat lon and time
				double geoLat = c.getDouble(c
						.getColumnIndexOrThrow(Fixes.KEY_LATITUDE));
				double geoLng = c.getDouble(c
						.getColumnIndexOrThrow(Fixes.KEY_LONGITUDE));
				long geoTim = c.getLong(c
						.getColumnIndexOrThrow(Fixes.KEY_TIMELONG));

				long geoDTim = c
						.getLong(c
								.getColumnIndexOrThrow(Fixes.KEY_STATION_DEPARTURE_TIMELONG));

				// This is a quick and dirty transformation of lat lon to x y,
				// assuming earth is a sphere. Remember to change this if and
				// when
				// we need more precision. ALSO NOTE: I am reversing the sign of
				// the x coordinates because the camera position ends up facing
				// the rear side of the x-y plane, so east and west are
				// backwards.
				double geoX = -geoLng * 60 * 1852 * Math.cos(geoLat);
				double geoY = 60 * 1852 * geoLat;

				if (c.isFirst()) {

					timsMin = geoTim;

					firstFixTime = Util.userDate(new Date(geoTim));

					XsMid = (float) geoX;
					YsMid = (float) geoY;

				}

				if (c.isLast()) {
					lastFixX = geoX;
					lastFixY = geoY;

					lastFixTime = Util.userDate(new Date(geoDTim));
					timsMax = geoDTim;

				}

				pathCoords.add((float) geoX);
				pathCoords.add((float) geoY);
				pathCoords.add((float) geoTim);

				if ((geoDTim - geoTim) > 0) {

					pathCoords.add((float) geoX);
					pathCoords.add((float) geoY);
					pathCoords.add((float) geoDTim);

					if ((geoDTim - geoTim) > maxStationDuration) {
						maxStationDuration = (geoDTim - geoTim);
						XsMid = (float) geoX;
						YsMid = (float) geoY;
					}

				}

				c.moveToNext();
				currentRecord++;
			}

			c.close();

			pcs = new float[pathCoords.size()];
			float[] Xs = new float[(pcs.length / 3)];
			float[] Ys = new float[Xs.length];
			Iterator<Float> iterator = pathCoords.iterator();
			for (int i = 0; i < pcs.length; i++) {
				pcs[i] = iterator.next().floatValue();
				if (i % 3 == 0) {
					Xs[i / 3] = pcs[i];
				}
				if ((i - 1) % 3 == 0) {
					Ys[(i - 1) / 3] = pcs[i];
				}
			}

			Arrays.sort(Xs);
			float XsMin = Xs[0];
			float XsMax = Xs[Xs.length - 1];

			Arrays.sort(Ys);
			float YsMin = Ys[0];
			float YsMax = Ys[Ys.length - 1];

			timsMid = timsMin + ((timsMax - timsMin) / 2);

			spaceScale = Math.max((YsMax - YsMin), (XsMax - XsMin));
			timeScale = (timsMax - timsMin) / 2.8f;

			for (int i = 0; i < pcs.length; i = i + 3) {
				if (spaceScale > 0) {
					pcs[i] = (pcs[i] - XsMid) / spaceScale;
				} else {
					pcs[i] = (pcs[i] - XsMid);
				}
				if (spaceScale > 0) {
					pcs[i + 1] = (pcs[i + 1] - YsMid) / spaceScale;
				} else {
					pcs[i + 1] = (pcs[i + 1] - YsMid);
				}

				if (timeScale > 0) {
					pcs[i + 2] = (pcs[i + 2] - timsMid) / timeScale;
				} else {
					pcs[i + 2] = (pcs[i + 2] - timsMid);
				}

			}

			return true;

		}

		protected void onProgressUpdate(Integer... progress) {
			progressbar.setProgress(progress[0]);
		}

		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (result) {

				progressbar.setVisibility(View.INVISIBLE);

				lineCoordsImp = pcs;
				lineList = pathCoords;

				final DisplayMetrics displayMetrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(
						displayMetrics);

				setContentView(R.layout.lifeline_overlay);

				mRenderer = new LifelineRenderer(pcs);

				mGLView = (LifelineGLSurfaceView) findViewById(R.id.glsurfaceview);

				mGLView.setRenderer(mRenderer, displayMetrics.density);

				mZoomControls = (ZoomControls) findViewById(R.id.zoomControls);

				mZoomControls.setIsZoomInEnabled(true);
				mZoomControls.setIsZoomOutEnabled(true);

				mZoomControls.setZoomSpeed(ZOOM_SPEED);

				mZoomControls.setOnZoomInClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (mRenderer.currentZoom < LifelineRenderer.MAXZOOM) {
							mRenderer.currentZoom = mRenderer.currentZoom
									* LifelineRenderer.zoomInc;
						}
					}

				});

				mZoomControls.setOnZoomOutClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (mRenderer.currentZoom > LifelineRenderer.MINZOOM) {
							mRenderer.currentZoom = mRenderer.currentZoom
									/ LifelineRenderer.zoomInc;

						}
					}
				});

				// setContentView(mGLView);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

					mRenderer.mWidth = ScreenSizeMethodsNew.getWidth(context);
					mRenderer.mHeight = ScreenSizeMethodsNew.getHeight(context);

				} else {
					mRenderer.mWidth = ScreenSizeMethodsOld.getWidth(context);
					mRenderer.mHeight = ScreenSizeMethodsOld.getHeight(context);

				}

				mLastFixTime = (TextView) findViewById(R.id.lastFixTimeText);
				if (lastFixTime != null) {
					mLastFixTime.setText(lastFixTime);
				}

				mFirstFixTime = (TextView) findViewById(R.id.firstFixTimeText);
				if (firstFixTime != null) {
					mFirstFixTime.setText(firstFixTime);
				}

				dataloaded = true;

			} else if (PropertyHolder.getStoreMyData() == true) {
				buildNoDataMessage();
			} else {
				buildStoreDataMessage();
			}
		}

	}

	private void buildNoDataMessage() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.message_no_data))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								dialog.cancel();
								finish();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildStoreDataMessage() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.message_store_data))
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								Intent i = new Intent(Lifeline.this,
										Settings.class);
								startActivity(i);

								dialog.cancel();

							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

}

