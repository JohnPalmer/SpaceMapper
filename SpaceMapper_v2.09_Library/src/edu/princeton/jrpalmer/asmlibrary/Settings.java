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
 * This file incorporates code written by Chang Y. Chung, Necati E. Ozgencil, 
 * and Kathleen Li for the Human Mobility Project, which is subject to the 
 * following terms: 
 * 
 * 		Copyright (C) 2010, 2011 Human Mobility Project
 *
 *		Permission is hereby granted, free of charge, to any person obtaining 
 *		a copy of this software and associated documentation files (the
 *		"Software"), to deal in the Software without restriction, including
 *		without limitation the rights to use, copy, modify, merge, publish, 
 *		distribute, sublicense, and/or sell copies of the Software, and to
 *		permit persons to whom the Software is furnished to do so, subject to
 *		the following conditions:
 *
 *		The above copyright notice and this permission notice shall be included
 *		in all copies or substantial portions of the Software.
 *
 *		THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *		EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *		MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *		IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *		CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *		TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *		SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. * 
 */

package edu.princeton.jrpalmer.asmlibrary;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import edu.princeton.jrpalmer.asmlibrary.Upload.Uploads;

/**
 * Allows user to change the app settings.
 * 
 * @author John R.B. Palmer
 * 
 */
public class Settings extends FragmentActivity {

	// private static final String TAG = "Settings";

	private Chronometer participationTimeText;
	private TextView nUploadsText;

	ColorStateList oldColors;
	private TextView mServiceMessage;
	private ToggleButton mServiceButton;
	private Spinner mIntervalSpinner;
	private Spinner mStorageSpinner;
	private RadioGroup mShareDataRadioGroup;
	private RadioGroup mToggleSatRadioGroup;
	private RadioGroup mToggleIconsRadioGroup;
	private RadioGroup mToggleAccRadioGroup;
	private RadioGroup mLimitStartDateRadioGroup;
	private RadioGroup mLimitEndDateRadioGroup;

	private static Button mStartDateButton;
	private static Button mEndDateButton;

	private int MIN_STORAGE = 0;
	private int MAX_STORAGE = 365;

	private TextView mStorageHeading;
	private TextView mStorageText;

	private TextView mIntervalHeading;
	private TextView mIntervalText;

	private TextView mStorageSizePendingUploadsText;
	private TextView mStorageSizeUserDbText;
	private ImageButton deletePendingUploadsButton;
	private ImageButton deleteUserDbButton;
	private ImageButton uploadButton;

	TextView participationLabel;
	TextView pendingUploadsLabel;

	private boolean storeMyData;
	private boolean shareMyData;

	static int storageDays;
	static Date savedStartDate;
	static Date savedEndDate;
	static Context context;

	UploadReceiver uploadReceiver;
	FixReceiver fixReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		PropertyHolder.init(context);

		storeMyData = PropertyHolder.getStoreMyData();

		setContentView(R.layout.settings);

		// views
		mServiceButton = (ToggleButton) findViewById(R.id.service_button);
		mServiceMessage = (TextView) findViewById(R.id.service_message);
		mIntervalSpinner = (Spinner) findViewById(R.id.spinner_interval);

		mStorageHeading = (TextView) findViewById(R.id.storageHeading);
		mStorageText = (TextView) findViewById(R.id.storageText);
		mIntervalHeading = (TextView) findViewById(R.id.intervalHeading);
		mIntervalText = (TextView) findViewById(R.id.intervalText);

		participationLabel = (TextView) findViewById(R.id.participationHeading);
		pendingUploadsLabel = (TextView) findViewById(R.id.pendingUploadsHeading);

		mStorageSizePendingUploadsText = (TextView) findViewById(R.id.storageSizePendingUploadsText);
		mStorageSizeUserDbText = (TextView) findViewById(R.id.storageSizeUserDbText);

		participationTimeText = (Chronometer) findViewById(R.id.participationTimeText);
		nUploadsText = (TextView) findViewById(R.id.nUploadsText);

		mStorageSpinner = (Spinner) findViewById(R.id.spinner_mydata);
		ArrayAdapter<CharSequence> sAdapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = MIN_STORAGE; i <= MAX_STORAGE; i++) {
			sAdapter.add(" " + Integer.toString(i));
		}

		mStorageSpinner.setAdapter(sAdapter);

		mShareDataRadioGroup = (RadioGroup) findViewById(R.id.sharedataRadioGroup);

		deletePendingUploadsButton = (ImageButton) findViewById(R.id.deletePendingUploadsButton);

		uploadButton = (ImageButton) findViewById(R.id.uploadButton);

		oldColors = mStorageText.getTextColors();

	}

	@Override
	protected void onResume() {

		if (Util.trafficCop(this))
			finish();
		IntentFilter uploadFilter;
		uploadFilter = new IntentFilter(getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_FIX_UPLOADED);
		uploadReceiver = new UploadReceiver();
		registerReceiver(uploadReceiver, uploadFilter);

		IntentFilter fixFilter;
		fixFilter = new IntentFilter(getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_FIX_RECORDED);
		fixReceiver = new FixReceiver();
		registerReceiver(fixReceiver, fixFilter);

		shareMyData = PropertyHolder.getShareData();

		toggleParticipationViews(shareMyData);

		int nUploads = PropertyHolder.getNUploads();

		final long participationTime = PropertyHolder.ptCheck();
		participationTimeText.setBase(SystemClock.elapsedRealtime()
				- participationTime);

		// service button
		boolean isServiceOn = PropertyHolder.isServiceOn();

		mServiceButton.setChecked(isServiceOn);
		mServiceButton.setOnClickListener(new ToggleButton.OnClickListener() {
			public void onClick(View view) {
				if (view.getId() != R.id.service_button)
					return;
				Context context = view.getContext();
				boolean on = ((ToggleButton) view).isChecked();
				String schedule = on ? Util.MESSAGE_SCHEDULE
						: Util.MESSAGE_UNSCHEDULE;
				// Log.e(TAG, schedule + on);

				// now schedule or unschedule
				Intent intent = new Intent(
						getString(R.string.internal_message_id) + schedule);
				context.sendBroadcast(intent);
				showSpinner(on, storeMyData);

				if (on && shareMyData) {
					final long ptNow = PropertyHolder.ptStart();
					participationTimeText.setBase(SystemClock.elapsedRealtime()
							- ptNow);
					participationTimeText.start();

					ContentResolver ucr = getContentResolver();

					ucr.insert(
							Util.getUploadQueueUri(context),
							UploadContentValues.createUpload("ONF", "on,"
									+ Util.iso8601(System.currentTimeMillis())
									+ "," + ptNow));
				} else {

					final long ptNow = PropertyHolder.ptStop();
					participationTimeText.setBase(SystemClock.elapsedRealtime()
							- ptNow);
					participationTimeText.stop();
					// stop uploader
					Intent stopUploaderIntent = new Intent(Settings.this,
							FileUploader.class);
					// Stop service if it is currently running
					stopService(stopUploaderIntent);

					if (shareMyData) {

						ContentResolver ucr = getContentResolver();

						ucr.insert(Util.getUploadQueueUri(context),
								UploadContentValues.createUpload(
										"ONF",
										"off,"
												+ Util.iso8601(System
														.currentTimeMillis())
												+ "," + ptNow));

					}

				}
				// If user turns CountdownDisplay on but GPS is not on, remind
				// user to turn
				// GPS on
				if (on) {
					final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

					if (!manager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						if (!manager
								.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
							buildAlertMessageNoGpsNoNet();
						} else
							buildAlertMessageNoGps();
					}
				}

				return;
			}
		});

		// interval spinner
		int intspinner_item = android.R.layout.simple_spinner_item;
		int dropdown_item = android.R.layout.simple_spinner_dropdown_item;
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.interval_array, intspinner_item);
		adapter.setDropDownViewResource(dropdown_item);
		mIntervalSpinner.setAdapter(adapter);
		mIntervalSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View v,
							int pos, long id) {
						int parentId = parent.getId();
						if (parentId != R.id.spinner_interval)
							return;
						if (pos > mInterval.length)
							return;
						if (!PropertyHolder.isServiceOn()) {
							PropertyHolder.setAlarmInterval(mInterval[pos]);
							return;
						}
						PropertyHolder.setAlarmInterval(mInterval[pos]);
						mServiceButton.setChecked(true);

						Intent intent = new Intent(
								getString(R.string.internal_message_id)
										+ Util.MESSAGE_SCHEDULE);
						Context context = getApplicationContext();
						context.sendBroadcast(intent);
						showSpinner(true, storeMyData);

						if (shareMyData) {
							ContentResolver ucr = getContentResolver();

							ucr.insert(Util.getUploadQueueUri(context),
									UploadContentValues.createUpload(
											"INT",
											Util.iso8601(System
													.currentTimeMillis())
													+ ","
													+ mInterval[pos]));
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						// do nothing
					}
				});
		int pos = ai2pos(PropertyHolder.getAlarmInterval());
		mIntervalSpinner.setSelection(pos);
		showSpinner(isServiceOn, storeMyData);

		// mydata buttons

		// storage spinner

		storageDays = PropertyHolder.getStorageDays();

		mStorageSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View v,
							int pos, long id) {
						int parentId = parent.getId();
						if (parentId != R.id.spinner_mydata)
							return;
						if (pos > MAX_STORAGE - MIN_STORAGE)
							return;
						PropertyHolder.setStorageDays(pos + MIN_STORAGE);
						PropertyHolder.setStoreMyData((pos + MIN_STORAGE) > 0);
					}

					public void onNothingSelected(AdapterView<?> parent) {
						// do nothing
					}
				});
		int storagepos = storageDays - MIN_STORAGE;
		mStorageSpinner.setSelection(storagepos);

		// NEW STUFF
		mToggleSatRadioGroup = (RadioGroup) findViewById(R.id.toggleSatRadioGroup);
		mToggleIconsRadioGroup = (RadioGroup) findViewById(R.id.toggleIconsRadioGroup);
		mToggleAccRadioGroup = (RadioGroup) findViewById(R.id.toggleAccRadioGroup);
		mLimitStartDateRadioGroup = (RadioGroup) findViewById(R.id.limitStartDateRadioGroup);
		mLimitEndDateRadioGroup = (RadioGroup) findViewById(R.id.limitEndDateRadioGroup);

		Intent i = getIntent();
		if (i.getBooleanExtra(MapMyData.DATES_BUTTON_MESSAGE, false)) {

			RelativeLayout dateSettingsArea = (RelativeLayout) findViewById(R.id.dateSettingsArea);
			dateSettingsArea.setFocusable(true);
			dateSettingsArea.setFocusableInTouchMode(true);
			dateSettingsArea.requestFocus();
		}

		if (shareMyData && isServiceOn) {
			participationTimeText.setBase(SystemClock.elapsedRealtime()
					- PropertyHolder.ptStart());
			participationTimeText.start();
		}

		nUploadsText.setText(String.valueOf(nUploads));

		if (nUploads >= Util.UPLOADS_TO_PRO && !PropertyHolder.getProVersion()
				&& participationTime >= Util.TIME_TO_PRO) {
			Util.createProNotification(context);
			PropertyHolder.setProVersion(true);
			PropertyHolder.setNeedsDebriefingSurvey(true);
		}

		boolean proV = PropertyHolder.getProVersion();

		// 19 December 2013: end of research changes
		mShareDataRadioGroup.check(R.id.sharedataNo);

		mShareDataRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.sharedataYes) {
							buildSharingOverAnnouncement();
						}
						mShareDataRadioGroup.check(R.id.sharedataNo);
					}
				});

		/*
		 * if (proV) {
		 * 
		 * if (shareMyData) { mShareDataRadioGroup.check(R.id.sharedataYes); if
		 * (PropertyHolder.isRegistered() == false ||
		 * PropertyHolder.hasConsented() == false) { send2Intro(context); } }
		 * else { mShareDataRadioGroup.check(R.id.sharedataNo); }
		 * 
		 * mShareDataRadioGroup .setOnCheckedChangeListener(new
		 * OnCheckedChangeListener() {
		 * 
		 * @Override public void onCheckedChanged(RadioGroup group, int
		 * checkedId) { shareMyData = (checkedId == R.id.sharedataYes);
		 * PropertyHolder.setShareData(shareMyData);
		 * toggleParticipationViews(shareMyData); final boolean on =
		 * PropertyHolder.isServiceOn(); if (shareMyData) { if
		 * (PropertyHolder.isRegistered() == false ||
		 * PropertyHolder.hasConsented() == false) { send2Intro(context);
		 * 
		 * } if (on) {
		 * 
		 * final long ptNow = PropertyHolder.ptStart();
		 * participationTimeText.setBase(SystemClock .elapsedRealtime() -
		 * ptNow);
		 * 
		 * participationTimeText.start();
		 * 
		 * ContentResolver ucr = getContentResolver();
		 * 
		 * ucr.insert( Util.getUploadQueueUri(context),
		 * UploadContentValues.createUpload( "ONF", "on," + Util.iso8601(System
		 * .currentTimeMillis()) + "," + ptNow));
		 * 
		 * } } else {
		 * 
		 * final long ptNow = PropertyHolder.ptStop();
		 * participationTimeText.setBase(SystemClock .elapsedRealtime() -
		 * ptNow); participationTimeText.stop(); // stop uploader Intent i = new
		 * Intent(Settings.this, FileUploader.class); // Stop service if it is
		 * currently running stopService(i);
		 * 
		 * if (on) { ContentResolver ucr = getContentResolver();
		 * 
		 * ucr.insert( Util.getUploadQueueUri(context),
		 * UploadContentValues.createUpload( "ONF", "off," + Util.iso8601(System
		 * .currentTimeMillis()) + "," + ptNow));
		 * 
		 * }
		 * 
		 * }
		 * 
		 * } }); } else { mShareDataRadioGroup.check(R.id.sharedataYes);
		 * mShareDataRadioGroup .setOnCheckedChangeListener(new
		 * OnCheckedChangeListener() {
		 * 
		 * @Override public void onCheckedChanged(RadioGroup group, int
		 * checkedId) { if (checkedId == R.id.sharedataNo) {
		 * mShareDataRadioGroup.check(R.id.sharedataYes);
		 * showCurrentlySharingDialog(); } } });
		 * 
		 * }
		 */
		new CheckPendingUploadsSizeTask().execute(context);
		new CheckUserDbSizeTask().execute(context);

		deletePendingUploadsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ContentResolver cr = getContentResolver();
				final int nDeleted = cr.delete(Util.getUploadQueueUri(context),
						"1", null);

				// Log.i("Settings", "number of rows deleted=" + nDeleted);
				Util.toast(context, String.valueOf(nDeleted) + " "
						+ getResources().getString(R.string.locations_deleted)
						+ ".");

				updateStorageSizes();

			}

		});

		deleteUserDbButton = (ImageButton) findViewById(R.id.deleteMyDbButton);

		deleteUserDbButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ContentResolver cr = getContentResolver();
				final int nDeleted = cr.delete(Util.getFixesUri(context), "1",
						null);

				Util.toast(context, String.valueOf(nDeleted) + " "
						+ getResources().getString(R.string.locations_deleted)
						+ ".");
				updateStorageSizes();

			}

		});

		uploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (Util.isOnline(context)) {
					Intent i = new Intent(Settings.this, FileUploader.class);
					startService(i);

					new UploadMessageTask().execute(context);
				} else {
					Util.toast(context,
							getResources().getString(R.string.offline_warning));
				}

			}

		});

		mToggleSatRadioGroup
				.check(PropertyHolder.getMapSat() ? R.id.toggleSatYes
						: R.id.toggleSatNo);

		mToggleSatRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						PropertyHolder
								.setMapSat(checkedId == R.id.toggleSatYes);
					}
				});

		mToggleIconsRadioGroup
				.check(PropertyHolder.getMapIcons() ? R.id.toggleIconsYes
						: R.id.toggleIconsNo);

		mToggleIconsRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						PropertyHolder
								.setMapIcons(checkedId == R.id.toggleIconsYes);
					}
				});

		mToggleAccRadioGroup
				.check(PropertyHolder.getMapAcc() ? R.id.toggleAccYes
						: R.id.toggleAccNo);

		mToggleAccRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						PropertyHolder
								.setMapAcc(checkedId == R.id.toggleAccYes);
					}
				});

		mStartDateButton = (Button) findViewById(R.id.startDateButton);
		mEndDateButton = (Button) findViewById(R.id.endDateButton);

		boolean limitStartDate = PropertyHolder.getLimitStartDate();
		boolean limitEndDate = PropertyHolder.getLimitEndDate();

		if (!limitStartDate)
			mStartDateButton.setVisibility(View.GONE);
		else {
			mStartDateButton.setVisibility(View.VISIBLE);

		}
		if (!limitEndDate)
			mEndDateButton.setVisibility(View.GONE);
		else {
			mEndDateButton.setVisibility(View.VISIBLE);
		}

		mLimitStartDateRadioGroup.check(limitStartDate ? R.id.limitStartDateYes
				: R.id.limitStartDateNo);

		mLimitStartDateRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						PropertyHolder
								.setLimitStartDate(checkedId == R.id.limitStartDateYes);

						if (checkedId != R.id.limitStartDateYes)
							mStartDateButton.setVisibility(View.GONE);
						else {
							mStartDateButton.setVisibility(View.VISIBLE);
						}
					}
				});

		mLimitEndDateRadioGroup.check(limitEndDate ? R.id.limitEndDateYes
				: R.id.limitEndDateNo);

		mLimitEndDateRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						PropertyHolder
								.setLimitEndDate(checkedId == R.id.limitEndDateYes);

						if (checkedId != R.id.limitEndDateYes)
							mEndDateButton.setVisibility(View.GONE);
						else {
							mEndDateButton.setVisibility(View.VISIBLE);
						}
					}
				});

		mStartDateButton.setText(Util.userDateNoTime(PropertyHolder
				.getMapStartDate()));
		mStartDateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				DialogFragment newFragment = new StartDatePickerFragment();
				newFragment.show(getSupportFragmentManager(), "datePicker");

			}

		});

		mEndDateButton.setText(Util.userDateNoTime(PropertyHolder
				.getMapEndDate()));
		mEndDateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				DialogFragment newFragment = new EndDatePickerFragment();
				newFragment.show(getSupportFragmentManager(), "datePicker");

			}

		});

		if (PropertyHolder.getNeedsDebriefingSurvey()) {
			buildProAnnouncement();
			PropertyHolder.setNeedsDebriefingSurvey(false);
		}

		super.onResume();

	}

	@Override
	protected void onPause() {
		unregisterReceiver(fixReceiver);
		unregisterReceiver(uploadReceiver);
		participationTimeText.stop();

		super.onPause();
	}

	// for interval spinner
	private int[] mInterval = { 15000, 30000, 60000, 120000, 300000, 600000,
			1800000, 3600000 };

	private int ai2pos(Long interval) {
		interval = interval == -1 ? Util.ALARM_INTERVAL : interval;
		for (int i = 0; i < mInterval.length; i++) {
			if (mInterval[i] == interval)
				return i;
		}
		return -1;
	}

	private void showSpinner(boolean _show, boolean store) {
		mIntervalSpinner.setEnabled(_show);
		if (_show) {
			mServiceMessage.setText(R.string.service_is_on);
			mIntervalSpinner.setVisibility(View.VISIBLE);
			mIntervalHeading.setTextColor(Color.WHITE);
			mIntervalText.setTextColor(oldColors);

		} else {
			mServiceMessage.setText(R.string.service_is_off);
			mIntervalSpinner.setVisibility(View.INVISIBLE);
			mIntervalHeading.setTextColor(Color.GRAY);
			mIntervalText.setTextColor(Color.GRAY);

		}
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.noGPSAlert))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton(getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildAlertMessageNoGpsNoNet() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.noGPSnoNetAlert))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton(getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	// TODO limit possible date selections by setting end date to current date
	// whenever user tries to set it over, and start date to first date in date
	// whenever user tries to set it under. Also stop user from setting start
	// date higher than end date.
	public static class StartDatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			Calendar c = PropertyHolder.getMapStartCalendar();

			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {

			PropertyHolder.setMapStartDate(year, month, day);

			GregorianCalendar gc = new GregorianCalendar(year, month, day);
			mStartDateButton.setText(Util.userDateNoTime(gc.getTime()));

			MapMyData.reloadData = true;

			if (gc.after(PropertyHolder.getMapEndCalendar()))
				Util.toast(
						context,
						getResources().getString(
								R.string.warning_calendar_start_date));

		}
	}

	public static class EndDatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			Calendar c = PropertyHolder.getMapEndCalendar();

			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {

			PropertyHolder.setMapEndDate(year, month, day);

			GregorianCalendar gc = new GregorianCalendar(year, month, day);
			mEndDateButton.setText(Util.userDateNoTime(gc.getTime()));

			MapMyData.reloadData = true;

			if (gc.before(PropertyHolder.getMapStartCalendar()))
				Util.toast(
						context,
						getResources().getString(
								R.string.warning_calendar_end_date));

		}
	}

	private void showCurrentlySharingDialog() {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.currently_sharing))
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						})
				.setNeutralButton(getResources().getString(R.string.more_info),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								Intent i = new Intent(Settings.this, Help.class);
								// start the intent
								startActivity(i);
								finish();
							}
						})

		;
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void send2Intro(Context context) {
		Intent intent = new Intent(context, Intro.class);
		startActivity(intent);
		return;
	}

	public class UploadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateStorageSizes();
			int nUploads = intent.getIntExtra("nUploads", 0);
			nUploadsText.setText(String.valueOf(nUploads));
			// Log.i(TAG, "nUploads=" + nUploads);

		}

	}

	public class FixReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateStorageSizes();
		}

	}

	private void updateStorageSizes() {

		new CheckPendingUploadsSizeTask().execute(context);
		new CheckUserDbSizeTask().execute(context);

	}

	public class CheckPendingUploadsSizeTask extends
			AsyncTask<Context, Integer, Boolean> {

		String resultUsed = "0 bytes";
		String resultAvailable = "0 bytes";
		boolean dummy = false;

		@Override
		protected void onPreExecute() {

			mStorageSizePendingUploadsText.setText(getResources().getString(
					R.string.calculating)
					+ " . . .");
		}

		protected Boolean doInBackground(Context... context) {

			resultUsed = Util.getPendingUploadsMB(context[0]);

			resultAvailable = Util.getAvailableMB();

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

			if (dummy)
				mStorageSizePendingUploadsText.setText(getResources()
						.getString(R.string.calculating) + " . . .");
			else
				mStorageSizePendingUploadsText.setText(getResources()
						.getString(R.string.calculating) + ". . .");

			dummy = !dummy;
		}

		protected void onPostExecute(Boolean result) {

			mStorageSizePendingUploadsText.setText(resultUsed + " "
					+ getResources().getString(R.string.used) + " ("
					+ resultAvailable + " "
					+ getResources().getString(R.string.available) + ")");

		}
	}

	public class CheckUserDbSizeTask extends
			AsyncTask<Context, Integer, Boolean> {

		String resultUsed = "0 bytes";
		String resultAvailable = "0 bytes";
		boolean dummy = false;

		@Override
		protected void onPreExecute() {

			mStorageSizeUserDbText.setText(getResources().getString(
					R.string.calculating)
					+ " . . .");
		}

		protected Boolean doInBackground(Context... context) {

			resultUsed = Util.getUserDbMB(context[0]);

			resultAvailable = Util.getAvailableMB();

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

			if (dummy)
				mStorageSizeUserDbText.setText(getResources().getString(
						R.string.calculating)
						+ " . . .");
			else
				mStorageSizeUserDbText.setText(getResources().getString(
						R.string.calculating)
						+ ". . .");

			dummy = !dummy;
		}

		protected void onPostExecute(Boolean result) {

			mStorageSizeUserDbText.setText(resultUsed + " "
					+ getResources().getString(R.string.used) + " ("
					+ resultAvailable + " "
					+ getResources().getString(R.string.available) + ")");

		}
	}

	public class UploadMessageTask extends AsyncTask<Context, Integer, Boolean> {
		int nRecords = 0;

		@Override
		protected void onPreExecute() {

		}

		protected Boolean doInBackground(Context... context) {

			ContentResolver cr = getContentResolver();
			String[] proj = { Uploads.KEY_PREFIX };
			String selectionString = Uploads.KEY_PREFIX + " = " + "'FIX'";

			Cursor c = cr.query(Util.getUploadQueueUri(context[0]), proj,
					selectionString, null, null);
			if (c.moveToFirst())
				nRecords = c.getCount();
			c.close();

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Boolean result) {

			final String msg;

			if (nRecords == 1)
				msg = getResources().getString(R.string.uploading)
						+ " "
						+ nRecords
						+ " "
						+ getResources().getString(R.string.location)
						+ ". "
						+ getResources().getString(
								R.string.thank_you_for_participating);
			else
				msg = getResources().getString(R.string.uploading)
						+ " "
						+ nRecords
						+ " "
						+ getResources().getString(R.string.locations)
						+ ". "
						+ getResources().getString(
								R.string.thank_you_for_participating);

			Util.toast(context, msg);

		}
	}

	private void toggleParticipationViews(boolean isActive) {
		if (isActive) {
			participationLabel.setTextColor(Color.WHITE);
			pendingUploadsLabel.setTextColor(Color.WHITE);
			participationTimeText.setTextColor(Color.WHITE);
			nUploadsText.setTextColor(Color.WHITE);
			deletePendingUploadsButton.setVisibility(View.VISIBLE);
			uploadButton.setVisibility(View.VISIBLE);
			mStorageSizePendingUploadsText.setVisibility(View.VISIBLE);

		} else {
			participationLabel.setTextColor(Color.GRAY);
			pendingUploadsLabel.setTextColor(Color.GRAY);
			participationTimeText.setTextColor(Color.GRAY);
			nUploadsText.setTextColor(Color.GRAY);
			deletePendingUploadsButton.setVisibility(View.INVISIBLE);
			uploadButton.setVisibility(View.INVISIBLE);
			mStorageSizePendingUploadsText.setVisibility(View.INVISIBLE);

		}

	}

	// 19 December 2013: end of research changes
	private void buildSharingOverAnnouncement() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.no_sharing_msg))
				.setCancelable(true)
				.setNegativeButton(getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.dismiss();
							}
						})
				.setPositiveButton(getResources().getString(R.string.visit_website),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								String url = "http://www.princeton.edu/~jrpalmer/spacemapper";
								Intent i = new Intent(Intent.ACTION_VIEW);
								i.setData(Uri.parse(url));
								startActivity(i);

							}
						});
		final AlertDialog alert = builder.create();
		alert.show();

	}

	private void buildProAnnouncement() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.user_id) + "\n"
						+ PropertyHolder.getUserId() + "\n\n"
						+ getResources().getString(R.string.pro_welcome))
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								cancelNotification(context);
								Intent i = new Intent(Settings.this,
										DebriefingSurvey.class);
								startActivity(i);
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();

	}

	public void cancelNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Util.PRO_CONVERSION_NOTIFICATION);

	}

}
