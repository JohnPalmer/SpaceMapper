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
 */

package edu.princeton.jrpalmer.asmlibrary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.princeton.jrpalmer.asmlibrary.Fix.Fixes;
import edu.princeton.jrpalmer.asmlibrary.RangeSeekBar.OnRangeSeekBarChangeListener;
import edu.princeton.jrpalmer.asmlibrary.RangeSeekBarDonut.OnRangeSeekBarDonutChangeListener;

/**
 * Allows user to view their own data in a map (from the database on their phone
 * -- not from the server).
 * 
 * @author John R.B. Palmer
 * 
 * 
 */
public class MapMyData extends MapActivity {
	String TAG = "MapMyData";
	private MapView mapView;
	private MapController myMapController;
	public List<Overlay> mapOverlays;
	private TextView dateRangeText;

	boolean getIntro = false;
	boolean isPro = false;

	boolean loadingData;
	boolean updatingDatabase;
	boolean savingCsv;

	private long startTime;
	private long endTime;

	private RelativeLayout dateArea;

	public static String DATES_BUTTON_MESSAGE = "datesButtonMessage";

	GeoPoint point;
	boolean satToggle;
	String stringLat;
	String stringLng;
	String stringAlt;
	String stringAcc;
	String stringProvider;
	String stringTime;

	boolean shareData;

	Double lastLat = null;
	Double lastLon = null;
	Double lastGeoLat = null;
	Double lastGeoLon = null;

	int startDay;
	int startMonth;
	int startYear;

	int endDay;
	int endMonth;
	int endYear;

	private int lastRecId = 0;

	public static boolean reloadData = false;

	boolean drawConfidenceCircles;
	boolean drawIcons;
	
	// TODO setting this totrue for testing until I build the on/off button
	boolean selectNewPrivacyZone = true;
	ArrayList<GeoPoint> privacyZones;

	boolean isServiceOn;

	GeoPoint currentCenter;

	MapOverlay mainOverlay;

	int BORDER_COLOR_MAP = 0xee4D2EFF;
	int FILL_COLOR_MAP = 0x554D2EFF;

	int BORDER_COLOR_SAT = 0xeeD9FCFF;
	int FILL_COLOR_SAT = 0xbbD9FCFF;

	int PZ_BORDER_COLOR = 0xee00ff00;
	int PZ_FILL_COLOR = 0x5500ff00;
	
	
	ProgressBar progressbar;

	ArrayList<MapPoint> mPoints;
	MapPoint[] mPointsArray;

	TextView progressbarText;

	LinearLayout progressNotificationArea;

	LinearLayout receiverNotificationArea;

	private TextView mReceiversOffWarning;

	boolean _mustDraw = true;

	long thumbMin = -1;
	long thumbMax = -1;

	private int minDist = 0;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final int screenSize = getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		if (screenSize != Configuration.SCREENLAYOUT_SIZE_LARGE
				&& screenSize != Configuration.SCREENLAYOUT_SIZE_XLARGE)
			requestWindowFeature(Window.FEATURE_NO_TITLE);

		// TODO add all of the alerts from countdown activity regarding sensors
		// off, sm off etc.

		Context context = getApplicationContext();
		PropertyHolder.init(context);
		PowerSensor.init(context);

		if (Util.trafficCop(this))
			finish();

		if (!PropertyHolder.getInitialStartDateSet()) {
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(System.currentTimeMillis());
			PropertyHolder.setMapStartDate(now);
			PropertyHolder.setInitialStartDateSet(true);
		}

		minDist = Util.getMinDist();
		// Log.e("FixGet", "minDist=" + minDist);

		setContentView(R.layout.map_layout);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		// pauseToggle = !PropertyHolder.isServiceOn();

		mPoints = new ArrayList<MapPoint>();

		myMapController = mapView.getController();
		myMapController.setZoom(15);

		progressbar = (ProgressBar) findViewById(R.id.mapProgressbar);
		progressbar.setProgress(0);

		progressbarText = (TextView) findViewById(R.id.progressBarLabel);

		progressNotificationArea = (LinearLayout) findViewById(R.id.mapProgressNotificationArea);

		receiverNotificationArea = (LinearLayout) findViewById(R.id.mapReceiverNotificationArea);

		mReceiversOffWarning = (TextView) findViewById(R.id.mapReceiversOffWarning);

		dateArea = (RelativeLayout) findViewById(R.id.dateArea);

		dateRangeText = (TextView) findViewById(R.id.dateSelectionText);

		dateRangeText.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent e) {

				if (e.getAction() == MotionEvent.ACTION_DOWN) {

					dateRangeText
							.setBackgroundResource(R.drawable.red_border_pressed);
				}
				if (e.getAction() == MotionEvent.ACTION_UP) {

					dateRangeText
							.setBackgroundResource(R.drawable.red_border);
				}
				
				
				return false;
			}

		});

		
		dateRangeText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(MapMyData.this, Settings.class);
				i.putExtra(DATES_BUTTON_MESSAGE, true);
				startActivity(i);

			}

		});

		getIntro = PropertyHolder.getIntro();
		isPro = PropertyHolder.getProVersion();

		AppRater.app_launched(this);
		
		privacyZones = new ArrayList<GeoPoint>();

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public class FixReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getBooleanExtra(FixGet.NEW_RECORD, false) == true) {
				updateMap(intent);

			}

		}

	}

	public void updateMap(Intent intent) {

		Context context = getApplicationContext();

		if (loadingData == false) {
			loadingData = true;
			new DataGrabberTask().execute(context);
		}

	}

	FixReceiver fixReceiver;

	@Override
	protected void onResume() {

	
		Context context = getApplicationContext();

		Log.e("TAPMAP", "selectPZ=" + selectNewPrivacyZone);
		
		if (Util.trafficCop(this))
			finish();
		dateRangeText.setBackgroundResource(R.drawable.red_border);

		if (reloadData) {
			mPoints.clear();
			lastRecId = 0;
		}

		isServiceOn = PropertyHolder.isServiceOn();
		shareData = PropertyHolder.getShareData();
		drawConfidenceCircles = PropertyHolder.getMapAcc();
		drawIcons = PropertyHolder.getMapIcons();
		satToggle = PropertyHolder.getMapSat();
		mapView.setSatellite(satToggle);

		if (PropertyHolder.getLimitEndDate())
			dateRangeText.setText(Util.userDate(PropertyHolder
					.getMapStartDate())
					+ " - "
					+ Util.userDate(PropertyHolder.getMapEndDate()));
		else
			dateRangeText.setText(Util.userDate(PropertyHolder
					.getMapStartDate())
					+ " - "
					+ getResources().getString(R.string.present));

		if (Util.needDatabaseUpdate) {

			progressbarText.setText(getResources().getString(
					R.string.database_updating_text));

			if (updatingDatabase == false) {
				updatingDatabase = true;
				new DatabaseUpdateTask().execute(context);
			}
		} else {
			progressNotificationArea.setVisibility(View.VISIBLE);
			progressbarText.setText(getResources().getString(
					R.string.mapdata_loading_text));

			if (loadingData == false) {
				loadingData = true;
				new DataGrabberTask().execute(context);
			}

		}

		receiverNotificationArea.setVisibility(View.INVISIBLE);

		if (isServiceOn) {
			final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				if (!manager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					receiverNotificationArea.setVisibility(View.VISIBLE);

					mReceiversOffWarning.setText(getResources().getString(
							R.string.noGPSnoNet));
				} else {
					receiverNotificationArea.setVisibility(View.VISIBLE);

					mReceiversOffWarning.setText(getResources().getString(
							R.string.noGPS));
				}

				mReceiversOffWarning
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

							}
						});

			}

		} else {

			receiverNotificationArea.setVisibility(View.VISIBLE);

			mReceiversOffWarning.setText(getResources().getString(
					R.string.main_text_off));

			mReceiversOffWarning.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent e) {

					if (e.getAction() == MotionEvent.ACTION_DOWN) {
					receiverNotificationArea.setBackgroundColor(getResources()
							.getColor(R.color.push_button_color));
					}
					if (e.getAction() == MotionEvent.ACTION_UP) {
					receiverNotificationArea.setBackgroundColor(getResources()
							.getColor(R.color.dark_grey));
					}
					
					
					return false;
				}

			});

			mReceiversOffWarning.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {


					Intent intent = new Intent(
							getString(R.string.internal_message_id)
									+ Util.MESSAGE_SCHEDULE);
					sendBroadcast(intent);
					receiverNotificationArea.setVisibility(View.INVISIBLE);

				}
			});

		}

		IntentFilter fixFilter;
		fixFilter = new IntentFilter(getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_FIX_RECORDED);
		fixReceiver = new FixReceiver();
		registerReceiver(fixReceiver, fixFilter);

		super.onResume();

	}

	@Override
	protected void onPause() {
		unregisterReceiver(fixReceiver);

		super.onPause();
	}

	static final private int LOCATE_NOW = Menu.FIRST;
	static final private int LIST_DATA = Menu.FIRST + 1;
	static final private int SAVE_MAP = Menu.FIRST + 2;
	static final private int SHARE_MAP = Menu.FIRST + 3;
	static final private int LIFELINE = Menu.FIRST + 4;
	static final private int SETTINGS = Menu.FIRST + 5;
	static final private int SAVE_CSV = Menu.FIRST + 6;
	static final private int ABOUT = Menu.FIRST + 7;
	static final private int RATE = Menu.FIRST + 8;
	static final private int SHARE = Menu.FIRST + 9;
	static final private int FLUSH_GPS = Menu.FIRST + 10;
	static final private int ASP = Menu.FIRST + 11;
	static final private int CENTER = Menu.FIRST + 13;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.clear();

		menu.add(0, LOCATE_NOW, Menu.NONE, R.string.menu_locate_now);
		menu.add(0, SETTINGS, Menu.NONE, R.string.menu_settings);
		menu.add(0, CENTER, Menu.NONE, R.string.menu_center_map);
		menu.add(0, SAVE_MAP, Menu.NONE, R.string.menu_map_save);
		menu.add(0, SHARE_MAP, Menu.NONE, R.string.menu_map_share);

		menu.add(0, LIFELINE, Menu.NONE, R.string.lifeline_button);
		menu.add(0, LIST_DATA, Menu.NONE, R.string.list_data_button);
		menu.add(0, SAVE_CSV, Menu.NONE, R.string.save_data_button);
		menu.add(0, FLUSH_GPS, Menu.NONE, R.string.menu_flush_gps);
		menu.add(0, RATE, Menu.NONE, R.string.menu_rate);
		menu.add(0, SHARE, Menu.NONE, R.string.menu_share);

		if (shareData)
			menu.add(0, ASP, Menu.NONE, R.string.menu_asp);
		menu.add(0, ABOUT, Menu.NONE, R.string.about);

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		Context context = getApplicationContext();
		switch (item.getItemId()) {

		case (ASP): {

			Intent i = new Intent(this, Help.class);
			// start the intent
			startActivity(i);

			return true;

		}

		case (SAVE_CSV): {

			if (savingCsv == false) {
				savingCsv = true;
				new SaveCsvTask().execute(context);
			}
			return true;

		}

		case (LIST_DATA): {
			Intent i = new Intent(this, ListMyDataCursorLoader.class);
			startActivity(i);
			return true;
		}

		case (LIFELINE): {
			// create an intent object and tell it where to go
			Intent i = new Intent(this, Lifeline.class);
			// start the intent
			startActivity(i);
			return true;
		}

		case (LOCATE_NOW): {
			if (Util.locatingNow == false) {
				Intent i = new Intent(MapMyData.this, FixGet.class);
				startService(i);
			}
			return true;
		}
		case (FLUSH_GPS): {

			buildFlushGPSAlert();
			return true;

		}
		case (CENTER): {
			if (currentCenter != null) {
				myMapController.animateTo(currentCenter);
			}
			return true;
		}
		case (SAVE_MAP): {
			saveMapImage(context);
			return true;
		}
		case (SHARE_MAP): {
			shareMap(context);
			return true;
		}
		case (SETTINGS): {
			Intent i = new Intent(this, Settings.class);
			startActivity(i);
			return true;
		}

		case (ABOUT): {
			Intent i = new Intent(this, About.class);
			// start the intent
			startActivity(i);
			return true;
		}
		case (RATE): {
			AppRater.showRateDialog(this, null);
			return true;
		}
		case (SHARE): {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);

			// set the type
			shareIntent.setType("text/plain");

			// add a subject
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Space Mapper");

			// build the body of the message to be shared
			String shareMessage = "https://play.google.com/store/apps/details?id="
					+ getResources().getString(R.string.package_name);

			// add the message
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					shareMessage);

			// start the chooser for sharing
			startActivity(Intent.createChooser(shareIntent, getResources()
					.getText(R.string.share_with)));

			return true;
		}

		}
		return false;
	}

	/*
	 * Draw selected locations on map. Returns true if drawn; false otherwise.
	 */
	private boolean drawFixes(ArrayList<MapPoint> data, long minTimeSelection,
			long maxTimeSelection, boolean sat, boolean clearMapOverlays,
			boolean recenter) {

		int nData = data.size();

		// quick return if the maximum time selected is less than the minimum
		// time selected or if there is no data loaded
		if (maxTimeSelection - minTimeSelection < 0 || nData < 1) {
			// get and clear mapview overlays
			mapOverlays = mapView.getOverlays();
			if (clearMapOverlays)
				mapOverlays.clear();
			mapView.postInvalidate();
			return false;
		}
		// turn data into array and grab long arrays of entry and exit times
		// from data
		MapPoint[] dataArray = new MapPoint[nData];
		long[] entryTimes = new long[nData];
		long[] exitTimes = new long[nData];
		int i = 0;
		for (MapPoint p : data) {
			dataArray[i] = p.copy();
			entryTimes[i] = p.entryTime;
			exitTimes[i] = p.exitTime;
			i++;
		}

		// calculate indexes for min and max times; if -1 is passed for min time
		// selection, then min index is simply 0; if -1 passed for max time
		// selection, then max index is the last element in the data array.
		int maxTimeIndex = maxTimeSelection == -1 ? (nData - 1) : Util
				.maxElementLessThanOrEqualToKey(entryTimes, maxTimeSelection,
						0, (nData - 1));
		int minTimeIndex = minTimeSelection == -1 ? 0 : Util
				.minElementGreaterThanOrEqualToKey(exitTimes, minTimeSelection,
						0, (nData - 1));

		// calculate the number of elements between these two index values
		int nSelected = 1 + maxTimeIndex - minTimeIndex;

		// return if there are no elements selected (indicated either by
		// nSelected less than 1 or by the min or max time index functions
		// returning -1
		if (nSelected < 1 || minTimeIndex < 0 || maxTimeIndex < 0) {
			// get and clear mapview overlays
			mapOverlays = mapView.getOverlays();
			if (clearMapOverlays)
				mapOverlays.clear();
			mapView.postInvalidate();
			return false;
		}
		// create new array of the selected points (which will be actually
		// drawn) and copy the selected points into this array
		MapPoint[] mPointsSelected = new MapPoint[nSelected];
		System.arraycopy(dataArray, minTimeIndex, mPointsSelected, 0, nSelected);

		// flag most recent fix for special icon
		mPointsSelected[nSelected - 1]
				.setIconFlag(MapPoint.ICON_CURRENT_LOCATION);

		// get mapview overlays
		mapOverlays = mapView.getOverlays();

		// Clear any existing overlays if cleraMapOverlays set to true
		if (clearMapOverlays)
			mapOverlays.clear();

		// set colors depending on satellite background
		int BC = sat ? BORDER_COLOR_SAT : BORDER_COLOR_MAP;
		int FC = sat ? FILL_COLOR_SAT : FILL_COLOR_MAP;

		mainOverlay = new MapOverlay(BC, FC);

		mainOverlay.setPointsToDraw(mPointsSelected);
		mapOverlays.add(mainOverlay);
		mapView.postInvalidate();

		currentCenter = new GeoPoint(mPointsSelected[nSelected - 1].lat,
				mPointsSelected[nSelected - 1].lon);

		if (recenter)
			myMapController.animateTo(currentCenter);

		return true;
	}

	private Bitmap getMapImage() {
		mapView.setDrawingCacheEnabled(true);
		Bitmap bmp = Bitmap.createBitmap(mapView.getDrawingCache());
		mapView.setDrawingCacheEnabled(false);
		return bmp;
	}

	private void saveMapImage(Context context) {

		try {
			File root = Environment.getExternalStorageDirectory();
			File directory = new File(root, "SpaceMapper");
			directory.mkdirs();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd_HH-mm-ss");
			Date date = new Date();
			String stringDate = dateFormat.format(date);
			String filename = getResources().getString(
					R.string.filenameprefix_map)
					+ stringDate + ".jpg";
			if (directory.canWrite()) {
				File f = new File(directory, filename);
				FileOutputStream out = new FileOutputStream(f);
				Bitmap bmp = getMapImage();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();

				Util.toast(context,
						getResources().getString(R.string.data_saved) + " " + f);
			} else {

				Util.toast(context,
						getResources().getString(R.string.data_SD_unavailable));
			}

		} catch (IOException e) {

			Util.toast(context, getResources()
					.getString(R.string.data_SD_error));
		}

	}

	private void shareMap(Context context) {

		try {
			File root = Environment.getExternalStorageDirectory();
			File directory = new File(root, "SpaceMapper");
			directory.mkdirs();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd_HH-mm-ss");
			Date date = new Date();
			String stringDate = dateFormat.format(date);
			String filename = getResources().getString(
					R.string.filenameprefix_map)
					+ stringDate + ".jpg";
			if (directory.canWrite()) {
				File f = new File(directory, filename);
				FileOutputStream out = new FileOutputStream(f);
				Bitmap bmp = getMapImage();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();

				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("image/jpeg");

				share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));

				// add the message
				share.putExtra(
						android.content.Intent.EXTRA_TEXT,
						getResources().getText(R.string.made_with_SM)
								+ ": https://play.google.com/store/apps/details?id="
								+ getResources().getString(
										R.string.package_name));

				startActivity(Intent.createChooser(share, getResources()
						.getText(R.string.share_with)));

			} else {

				Util.toast(context,
						getResources().getString(R.string.data_SD_unavailable));
			}

		} catch (IOException e) {

			Util.toast(context, getResources()
					.getString(R.string.data_SD_error));
		}

	}

	class MapOverlay extends Overlay {

		private Paint mPaintBorder;
		private Paint mPaintFill;
		
		private Paint mPaintPzBorder;
		private Paint mPaintPzFill;
		
		private Bitmap normalfixPin;
		private Bitmap currentfixPin;

		private MapPoint[] pointsToDraw;

		MapOverlay(int border, int fill) {
			mPaintBorder = new Paint();
			mPaintBorder.setStyle(Paint.Style.STROKE);
			mPaintBorder.setAntiAlias(true);
			mPaintBorder.setColor(border);
			mPaintFill = new Paint();
			mPaintFill.setStyle(Paint.Style.FILL);
			mPaintFill.setColor(fill);

			
			mPaintPzBorder = new Paint();
			mPaintPzBorder.setStyle(Paint.Style.STROKE);
			mPaintPzBorder.setAntiAlias(true);
			mPaintPzBorder.setColor(PZ_BORDER_COLOR);
			mPaintPzFill = new Paint();
			mPaintPzFill.setStyle(Paint.Style.FILL);
			mPaintPzFill.setColor(PZ_FILL_COLOR);

			
			normalfixPin = BitmapFactory.decodeResource(getResources(),
					R.drawable.old_fix_pin);

			currentfixPin = BitmapFactory.decodeResource(getResources(),
					R.drawable.new_fix_pin);

		}

		public void setPointsToDraw(MapPoint[] points) {
			pointsToDraw = points;
		}

		public MapPoint[] getPointsToDraw() {
			return pointsToDraw;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			if (shadow)
				return;

			// First draw confidence circles if toggled
			if (drawConfidenceCircles) {
				for (MapPoint p : pointsToDraw) {
					// convert point to pixels
					Point screenPts = new Point();
					GeoPoint pointToDraw = new GeoPoint(p.lat, p.lon);
					mapView.getProjection().toPixels(pointToDraw, screenPts);
					int radius = (int) mapView.getProjection()
							.metersToEquatorPixels(p.acc);
					canvas.drawCircle(screenPts.x, screenPts.y, radius,
							mPaintBorder);
					canvas.drawCircle(screenPts.x, screenPts.y, radius,
							mPaintFill);
				}
			}

			// Now draw icons
			if (drawIcons) {
				for (MapPoint p : pointsToDraw) {
					// convert point to pixels
					Point screenPts = new Point();
					GeoPoint pointToDraw = new GeoPoint(p.lat, p.lon);
					mapView.getProjection().toPixels(pointToDraw, screenPts);
					if (p.iconFlag == MapPoint.ICON_NORMAL)
						canvas.drawBitmap(normalfixPin, screenPts.x
								- normalfixPin.getWidth() / 2, screenPts.y
								- normalfixPin.getHeight(), null);
					else
						canvas.drawBitmap(currentfixPin, screenPts.x
								- currentfixPin.getWidth() / 2, screenPts.y
								- currentfixPin.getHeight(), null);
				}
			}
			
			Log.e("TAPMAP", "pzs.size=" + privacyZones.size());
			
			if (privacyZones != null && privacyZones.size() >0){
				for(GeoPoint p : privacyZones){
				Point screenPts = new Point();
				mapView.getProjection().toPixels(p, screenPts);
				int radius = (int) mapView.getProjection()
						.metersToEquatorPixels(Util.PRIVACY_ZONE_RADIUS);
				canvas.drawCircle(screenPts.x, screenPts.y, radius,
						mPaintPzBorder);
				canvas.drawCircle(screenPts.x, screenPts.y, radius,
						mPaintPzFill);
				
				Log.i("TAPMAP", "just drew pz");
				}
			}

			return;
		}
		
		@Override
		public boolean onTap(GeoPoint p, MapView mapview) {

			if(selectNewPrivacyZone){			
			privacyZones.add(new GeoPoint(p.getLatitudeE6(), p.getLongitudeE6()));
			
			Log.i("TAPMAP", "tapped: " + p.getLatitudeE6());
			return true;
			} else{
				return false;
			}

		}


	}

	public class DataGrabberTask extends AsyncTask<Context, Integer, Boolean> {

		int myProgress;

		int nFixes;

		ArrayList<MapPoint> results = new ArrayList<MapPoint>();

		GeoPoint center;

		@Override
		protected void onPreExecute() {

			myProgress = 0;
		}

		protected Boolean doInBackground(Context... context) {

			results.clear();

			startTime = PropertyHolder.getMapStartDate().getTime();
			endTime = PropertyHolder.getMapEndDate().getTime();

			final String selectionString;
			final boolean limitStart = PropertyHolder.getLimitStartDate();
			final boolean limitEnd = PropertyHolder.getLimitEndDate();

			if (limitStart && !limitEnd)
				selectionString = Fixes.KEY_ROWID + " > " + lastRecId + " AND "
						+ Fixes.KEY_STATION_DEPARTURE_TIMELONG + " >= "
						+ startTime;
			else if (!limitStart && limitEnd)
				selectionString = Fixes.KEY_ROWID + " > " + lastRecId + " AND "
						+ Fixes.KEY_TIMELONG + " <= " + endTime;
			else if (limitStart && limitEnd)
				selectionString = Fixes.KEY_ROWID + " > " + lastRecId + " AND "
						+ Fixes.KEY_STATION_DEPARTURE_TIMELONG + " >= "
						+ startTime + " AND " + Fixes.KEY_TIMELONG + " <= "
						+ endTime;
			else
				selectionString = Fixes.KEY_ROWID + " > " + lastRecId;

			ContentResolver cr = getContentResolver();
			Cursor c = cr.query(Util.getFixesUri(context[0]),
					Fixes.KEYS_LATLONACCTIMES, selectionString, null, null);

			if (!c.moveToFirst()) {
				c.close();

				return false;
			}

			int latCol = c.getColumnIndexOrThrow("latitude");
			int lonCol = c.getColumnIndexOrThrow("longitude");
			int accCol = c.getColumnIndexOrThrow("accuracy");
			int idCol = c.getColumnIndexOrThrow("_id");
			int timeCol = c.getColumnIndexOrThrow(Fixes.KEY_TIMELONG);
			int sdtimeCol = c
					.getColumnIndexOrThrow(Fixes.KEY_STATION_DEPARTURE_TIMELONG);

			nFixes = c.getCount();

			// float lastAcc = 0;

			int currentRecord = 0;

			while (!c.isAfterLast()) {
				myProgress = (int) (((currentRecord + 1) / (float) nFixes) * 100);
				publishProgress(myProgress);

				// Escape early if cancel() is called
				if (isCancelled())
					break;

				lastRecId = c.getInt(idCol);

				// First grabbing double values of lat lon and time
				Double lat = c.getDouble(latCol);
				Double lon = c.getDouble(lonCol);
				float acc = c.getFloat(accCol);
				long entryTime = c.getLong(timeCol);
				long exitTime = c.getLong(sdtimeCol);

				Double geoLat = lat * 1E6;
				Double geoLon = lon * 1E6;

				results.add(new MapPoint(geoLat.intValue(), geoLon.intValue(),
						acc, entryTime, exitTime, MapPoint.ICON_NORMAL));

				c.moveToNext();
				currentRecord++;
			}
			c.close();
			return true;

		}

		protected void onProgressUpdate(Integer... progress) {
			progressbar.setProgress(progress[0]);
		}

		protected void onPostExecute(Boolean result) {

			Context context = getApplicationContext();
			if (result) {

				if (results != null && results.size() > 0) {

					for (MapPoint p : results) {
						mPoints.add(p);
					}
					final int newlastFixIndex = mPoints.size() - 1;

					final long selectorEndTime;

					if (PropertyHolder.getLimitEndDate()) {
						selectorEndTime = Long.valueOf(endTime);
					} else {
						selectorEndTime = Long.valueOf(mPoints
								.get(newlastFixIndex).exitTime);
					}

					drawDateSelector(startTime, selectorEndTime, context);

				}
			} else
				drawDateSelector(startTime, endTime, context);

			progressNotificationArea.setVisibility(View.INVISIBLE);

			dateArea.setVisibility(View.VISIBLE);

			loadingData = false;

			if (getIntro && !isPro) {
				PropertyHolder.setIntro(false);
				getIntro = false;
				buildAlertMessageIntro();
			}
		}

	}

	public class DatabaseUpdateTask extends
			AsyncTask<Context, Integer, Boolean> {

		int myProgress;

		@Override
		protected void onPreExecute() {
			myProgress = 0;
			progressNotificationArea.setVisibility(View.VISIBLE);
		}

		protected Boolean doInBackground(Context... context) {

			Double lastLat = null;
			Double lastLon = null;

			ContentResolver cr = getContentResolver();
			Cursor c = cr.query(Util.getFixesUri(context[0]),
					Fixes.KEYS_LATLONTIME, null, null, null);

			if (!c.moveToFirst()) {
				c.close();

				return false;
			}

			int latCol = c.getColumnIndexOrThrow(Fixes.KEY_LATITUDE);
			int lonCol = c.getColumnIndexOrThrow(Fixes.KEY_LONGITUDE);
			int idCol = c.getColumnIndexOrThrow(Fixes.KEY_ROWID);
			int timelongCol = c.getColumnIndexOrThrow(Fixes.KEY_TIMELONG);

			int nFixes = c.getCount();

			int currentRecord = 0;

			while (!c.isAfterLast()) {

				myProgress = (int) (((currentRecord + 1) / (float) nFixes) * 100);
				publishProgress(myProgress);

				// Escape early if cancel() is called
				if (isCancelled())
					break;

				// First grabbing double values of lat lon and time
				Double lat = c.getDouble(latCol);
				Double lon = c.getDouble(lonCol);
				int id = c.getInt(idCol);

				if (lastLat == null && lastLon == null) {

					lastLat = lat;
					lastLon = lon;

				} else if (lat != null && lon != null && lastLat != null
						&& lastLon != null) {

					float[] distResult = new float[1];

					Location.distanceBetween(lastLat, lastLon, lat, lon,
							distResult);

					float dist = distResult[0];

					if (dist < minDist) {

						if (id > 1) {
							ContentValues cv = new ContentValues();
							String sc = Fixes.KEY_ROWID + " = " + (id - 1);
							cv.put(Fixes.KEY_STATION_DEPARTURE_TIMELONG,
									c.getLong(timelongCol));
							cr.update(Util.getFixesUri(context[0]), cv, sc,
									null);
						}

						cr.delete(Util.getFixesUri(context[0]), Fixes.KEY_ROWID
								+ " = " + id, null);
					} else {
						lastLat = lat;
						lastLon = lon;

						ContentValues cv = new ContentValues();
						String sc = Fixes.KEY_ROWID + " = " + id;
						cv.put(Fixes.KEY_STATION_DEPARTURE_TIMELONG,
								c.getLong(timelongCol));
						cr.update(Util.getFixesUri(context[0]), cv, sc, null);

					}
				}
				c.moveToNext();
				currentRecord++;

			}
			c.close();
			return true;
		}

		protected void onProgressUpdate(Integer... progress) {
			progressbar.setProgress(progress[0]);
		}

		protected void onPostExecute(Boolean result) {

			Util.needDatabaseUpdate = false;
			PropertyHolder.setNeedDatabaseValueUpdate(false);

			progressbarText.setText(getResources().getString(
					R.string.mapdata_loading_text));
			Context context = getApplicationContext();

			updatingDatabase = false;
			if (loadingData == false) {
				loadingData = true;
				new DataGrabberTask().execute(context);
			}
		}
	}

	public class SaveCsvTask extends AsyncTask<Context, Integer, Boolean> {

		int myProgress;

		String saveMe;

		File file;

		private int FLAG;
		private final static int SAVED = 1;
		private final static int SD_UNAVAILABLE = 2;
		private final static int SD_ERROR = 3;

		@Override
		protected void onPreExecute() {

			progressbarText.setText(getResources().getString(
					R.string.saving_data));
			progressNotificationArea.setVisibility(View.VISIBLE);
			myProgress = 0;
		}

		protected Boolean doInBackground(Context... context) {

			ContentResolver cr = getContentResolver();
			Cursor c = cr.query(Util.getFixesUri(context[0]),
					Fixes.KEYS_SAVECSV, null, null, null);

			int accuracy = c.getColumnIndexOrThrow(Fixes.KEY_ACCURACY);
			int altitude = c.getColumnIndexOrThrow(Fixes.KEY_ALTITUDE);
			int latitude = c.getColumnIndexOrThrow(Fixes.KEY_LATITUDE);
			int longitude = c.getColumnIndexOrThrow(Fixes.KEY_LONGITUDE);
			int provider = c.getColumnIndexOrThrow(Fixes.KEY_PROVIDER);
			int timelong = c.getColumnIndexOrThrow(Fixes.KEY_TIMELONG);
			StringBuilder sb = new StringBuilder("");
			sb.append("accuracy").append(",");
			sb.append("altitude").append(",");
			sb.append("latitude").append(",");
			sb.append("longitude").append(",");
			sb.append("provider").append(",");
			sb.append("time");

			c.moveToFirst();

			int nFixes = c.getCount();

			int currentRecord = 0;

			while (!c.isAfterLast()) {

				myProgress = (int) (((currentRecord + 1) / (float) nFixes) * 100);
				publishProgress(myProgress);

				// Escape early if cancel() is called
				if (isCancelled())
					break;

				sb.append("\n");
				sb.append(Util.doubleFieldVal(c, accuracy)).append(",");
				sb.append(Util.doubleFieldVal(c, altitude)).append(",");
				sb.append(Util.doubleFieldVal(c, latitude)).append(",");
				sb.append(Util.doubleFieldVal(c, longitude)).append(",");
				sb.append(Util.enquote(c.getString(provider))).append(",");
				sb.append(Util.enquote(Util.userDate(
						new Date(c.getLong(timelong))).trim()));
				c.moveToNext();
				currentRecord++;
			}

			saveMe = sb.toString();
			c.close();

			BufferedWriter out;
			try {
				File root = Environment.getExternalStorageDirectory();
				File directory = new File(root, "SpaceMapper");
				directory.mkdirs();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd_HH-mm-ss");
				Date date = new Date();
				String stringDate = dateFormat.format(date);
				String filename = getResources().getString(
						R.string.filenameprefix_csv)
						+ stringDate + ".csv";
				if (directory.canWrite()) {
					file = new File(directory, filename);
					FileWriter filewriter = new FileWriter(file);
					out = new BufferedWriter(filewriter);
					out.write(saveMe);
					out.close();

					FLAG = SAVED;

				} else {

					FLAG = SD_UNAVAILABLE;
				}

			} catch (IOException e) {
				FLAG = SD_ERROR;
			}

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {
			progressbar.setProgress(progress[0]);
		}

		protected void onPostExecute(Boolean result) {

			Context context = getApplicationContext();

			switch (FLAG) {

			case (SAVED): {
				Util.toast(context,
						getResources().getString(R.string.data_saved) + " "
								+ file);
				break;
			}
			case (SD_UNAVAILABLE): {
				Util.toast(context,
						getResources().getString(R.string.data_SD_unavailable));
				break;
			}
			case (SD_ERROR): {
				Util.toast(context,
						getResources().getString(R.string.data_SD_error));
				break;
			}
			}
			progressNotificationArea.setVisibility(View.INVISIBLE);

			savingCsv = false;

		}
	}

	private void buildFlushGPSAlert() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.renew_gps_alert))
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								Util.flushGPSFlag = true;

								dialog.cancel();
							}
						})

				.setNegativeButton(getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});

		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildAlertMessageIntro() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getText(R.string.main_text))
				.setCancelable(true)
				.setNeutralButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.dismiss();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void drawDateSelector(long min, long max, Context context) {

		final long barMax = max;
		final long barMin = min;
		final long selectedMax;
		final long selectedMin;

		if (thumbMin == -1 || thumbMin < min)
			selectedMin = Long.valueOf(min);
		else
			selectedMin = Long.valueOf(thumbMin);
		if (thumbMax == -1 || thumbMax > max)
			selectedMax = Long.valueOf(max);
		else
			selectedMax = Long.valueOf(thumbMax);

		drawFixes(mPoints, selectedMin, selectedMax, satToggle, true, true);

		if (PropertyHolder.getLimitEndDate())
			dateRangeText.setText(Util.userDate(new Date(selectedMin)) + " - "
					+ Util.userDate(new Date(selectedMax)));
		else
			dateRangeText.setText(Util.userDate(new Date(selectedMin)) + " - "
					+ getResources().getString(R.string.present));

		if (Build.VERSION.SDK_INT >= 5) {
			RangeSeekBar<Long> seekBar = new RangeSeekBar<Long>(min, max,
					context);

			seekBar.setSelectedMinValue(selectedMin);
			seekBar.setSelectedMaxValue(selectedMax);

			seekBar.setNotifyWhileDragging(true);

			seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Long>() {
				@Override
				public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
						Long minValue, Long maxValue) {

					drawFixes(mPoints, minValue, maxValue, satToggle, true,
							false);

					if (maxValue < barMax) {
						thumbMax = Long.valueOf(maxValue);
						dateRangeText.setText(Util.userDate(new Date(minValue))
								+ " - " + Util.userDate(new Date(maxValue)));
					} else {
						thumbMax = -1;
						if (PropertyHolder.getLimitEndDate())
							dateRangeText.setText(Util.userDate(new Date(
									minValue))
									+ " - "
									+ Util.userDate(new Date(maxValue)));
						else
							dateRangeText.setText(Util.userDate(new Date(
									minValue))
									+ " - "
									+ getResources()
											.getString(R.string.present));
					}
				}
			});

			// add RangeSeekBar to pre-defined layout
			LinearLayout dateSelectorArea = (LinearLayout) findViewById(R.id.dateSelectorArea);
			dateSelectorArea.removeAllViews();
			dateSelectorArea.addView(seekBar);

		} else {
			RangeSeekBarDonut<Long> seekBar = new RangeSeekBarDonut<Long>(min,
					max, context);

			seekBar.setSelectedMinValue(selectedMin);
			seekBar.setSelectedMaxValue(selectedMax);

			seekBar.setNotifyWhileDragging(true);

			seekBar.setOnRangeSeekBarDonutChangeListener(new OnRangeSeekBarDonutChangeListener<Long>() {
				@Override
				public void onRangeSeekBarDonutValuesChanged(
						RangeSeekBarDonut<?> bar, Long minValue, Long maxValue) {

					drawFixes(mPoints, minValue, maxValue, satToggle, true,
							false);

					if (maxValue < barMax) {
						thumbMax = Long.valueOf(maxValue);
						dateRangeText.setText(Util.userDate(new Date(minValue))
								+ " - " + Util.userDate(new Date(maxValue)));
					} else {
						thumbMax = -1;
						if (PropertyHolder.getLimitEndDate())
							dateRangeText.setText(Util.userDate(new Date(
									minValue))
									+ " - "
									+ Util.userDate(new Date(maxValue)));
						else
							dateRangeText.setText(Util.userDate(new Date(
									minValue))
									+ " - "
									+ getResources()
											.getString(R.string.present));
					}
				}
			});

			// add RangeSeekBar to pre-defined layout
			LinearLayout dateSelectorArea = (LinearLayout) findViewById(R.id.dateSelectorArea);
			dateSelectorArea.removeAllViews();
			dateSelectorArea.addView(seekBar);

		}
	}

}