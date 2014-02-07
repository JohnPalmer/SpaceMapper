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

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Allows user to provide answers to survey before starting using the app. All
 * responses are encrypted and uploaded to the server.
 * 
 * @author John R.B. Palmer
 * 
 */
public class Registration extends Activity {
	// private static final String TAG = "Registration";

	String userId = UUID.randomUUID().toString();

	private Spinner mAgeSpinner;
	private RadioGroup mSexRadioGroup;
	private LinearLayout mRaceView;
	private CheckBox mWhiteBox;
	private CheckBox mBlackBox;
	private CheckBox mLatinoBox;
	private CheckBox mNativeAmericanBox;
	private CheckBox mAsianBox;
	private CheckBox mOtherBox;
	private RadioGroup mMarriedRadioGroup;
	private Spinner mKidsSpinner;
	private RadioGroup mBornResRadioGroup;
	private TextView mPobPrompt;
	private Spinner mPobSpinner;
	private Spinner mPorSpinner;
	private TextView mEntryYearPrompt;
	private Spinner mEntryyearSpinner;
	private RadioGroup mCitRadioGroup;
	private Spinner mLangSpinner;
	private Spinner mEmpSpinner;

	private String countrySelected = "";
	private String DR1 = "";
	private String DR2 = "United States";
	private String DR3 = "Estats Units (EUA)";
	private String DR4 = "Estados Unidos de América";

	private static final int MIN_AGE = 18;
	private static final int MAX_AGE = 150;

	private static final int MIN_KIDS = 0;
	private static final int MAX_KIDS = 30;

	private static final int MIN_YEAR = 1900;

	// The maximum year used in the entry year spinner will be the current year,
	// set on create.
	private int MAX_YEAR;

	private static final int MIN_HH = 1;
	private static final int MAX_HH = 50;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);

		MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR);

		// Set up raceview, Pob and entryyear so that they can change
		mRaceView = (LinearLayout) findViewById(R.id.raceView);
		mPobPrompt = (TextView) findViewById(R.id.pob_prompt);
		mEntryYearPrompt = (TextView) findViewById(R.id.entryyearPrompt);
		mEntryyearSpinner = (Spinner) findViewById(R.id.entryyearSpinner);
		ArrayAdapter<CharSequence> entryAdapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		entryAdapter
				.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
		entryAdapter.add("  ");
		for (int i = MAX_YEAR; i >= MIN_YEAR; i--) {
			entryAdapter.add(Integer.toString(i));
		}
		mEntryyearSpinner.setAdapter(entryAdapter);

		// RadioGroups

		mSexRadioGroup = (RadioGroup) findViewById(R.id.sexRadioGroup);
		mMarriedRadioGroup = (RadioGroup) findViewById(R.id.marriedRadioGroup);
		mCitRadioGroup = (RadioGroup) findViewById(R.id.citRadioGroup);
		mBornResRadioGroup = (RadioGroup) findViewById(R.id.bornResRadioGroup);

		mBornResRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.bornResYes) {
							mPobSpinner.setEnabled(false);
							mPobSpinner.setVisibility(View.INVISIBLE);
							mPobPrompt.setTextColor(Color.GRAY);
							mEntryyearSpinner.setEnabled(false);
							mEntryyearSpinner.setVisibility(View.INVISIBLE);
							mEntryYearPrompt.setTextColor(Color.GRAY);
						} else {
							mPobSpinner.setEnabled(true);
							mPobSpinner.setVisibility(View.VISIBLE);
							mPobPrompt.setTextColor(Color.WHITE);
							mEntryyearSpinner.setEnabled(true);
							mEntryyearSpinner.setVisibility(View.VISIBLE);
							mEntryYearPrompt.setTextColor(Color.WHITE);
						}
					}
				});

		// Spinners

		mAgeSpinner = (Spinner) findViewById(R.id.ageSpinner);
		ArrayAdapter<CharSequence> ageAdapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		ageAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ageAdapter.add("  ");
		for (int i = MIN_AGE; i <= MAX_AGE; i++) {
			ageAdapter.add(Integer.toString(i));
		}
		mAgeSpinner.setAdapter(ageAdapter);

		mKidsSpinner = (Spinner) findViewById(R.id.kidsSpinner);
		ArrayAdapter<CharSequence> kidsAdapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		kidsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		kidsAdapter.add("  ");
		for (int i = MIN_KIDS; i <= MAX_KIDS; i++) {
			kidsAdapter.add(Integer.toString(i));
		}
		mKidsSpinner.setAdapter(kidsAdapter);

		mPobSpinner = (Spinner) findViewById(R.id.pobSpinner);
		ArrayAdapter<CharSequence> pobAdapter = ArrayAdapter
				.createFromResource(this, R.array.countries_array,
						android.R.layout.simple_spinner_item);
		pobAdapter
				.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
		mPobSpinner.setAdapter(pobAdapter);

		final String[] countries = getResources().getStringArray(
				R.array.countries_array);

		mPorSpinner = (Spinner) findViewById(R.id.porSpinner);
		ArrayAdapter<CharSequence> porAdapter = ArrayAdapter
				.createFromResource(this, R.array.countries_array,
						android.R.layout.simple_spinner_item);
		porAdapter
				.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
		mPorSpinner.setAdapter(porAdapter);

		/*
		 * mPorSpinner .setOnItemSelectedListener(new
		 * Spinner.OnItemSelectedListener() { public void
		 * onItemSelected(AdapterView<?> parent, View v, int pos, long id) { int
		 * parentId = parent.getId(); if (parentId != R.id.porSpinner) return;
		 * if (pos > countries.length) return; String countrySelected =
		 * String.valueOf(mPorSpinner .getSelectedItem()); if (countrySelected
		 * == "United States" || countrySelected == " " || countrySelected ==
		 * "Estats Units (EUA)" || countrySelected ==
		 * "Estados Unidos de América") { for (int i = 0; i <
		 * mRaceView.getChildCount(); i++) { View view =
		 * mRaceView.getChildAt(i); view.setVisibility(View.VISIBLE); }
		 * mRaceView.setVisibility(View.VISIBLE);
		 * 
		 * } else { for (int i = 0; i < mRaceView.getChildCount(); i++) { View
		 * view = mRaceView.getChildAt(i); view.setVisibility(View.GONE); }
		 * 
		 * mRaceView.setVisibility(View.GONE); }
		 * 
		 * }
		 * 
		 * public void onNothingSelected(AdapterView<?> parent) { // do nothing
		 * } });
		 */
		mLangSpinner = (Spinner) findViewById(R.id.langSpinner);
		ArrayAdapter<CharSequence> langAdapter = ArrayAdapter
				.createFromResource(this, R.array.langs_array,
						android.R.layout.simple_spinner_item);
		langAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLangSpinner.setAdapter(langAdapter);

		

		mEmpSpinner = (Spinner) findViewById(R.id.empSpinner);
		ArrayAdapter<CharSequence> empAdapter = ArrayAdapter
				.createFromResource(this, R.array.emp_array,
						android.R.layout.simple_spinner_item);
		empAdapter
				.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
		mEmpSpinner.setAdapter(empAdapter);


		
		// Checkboxes

		mWhiteBox = (CheckBox) findViewById(R.id.whiteBox);
		mBlackBox = (CheckBox) findViewById(R.id.blackBox);
		mLatinoBox = (CheckBox) findViewById(R.id.latinoBox);
		mNativeAmericanBox = (CheckBox) findViewById(R.id.nativeamericanBox);
		mAsianBox = (CheckBox) findViewById(R.id.asianBox);
		mOtherBox = (CheckBox) findViewById(R.id.otherBox);

		mPorSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View v,
							int pos, long id) {
						int parentId = parent.getId();
						if (parentId != R.id.porSpinner)
							return;
						if (pos > countries.length)
							return;

						countrySelected = String.valueOf(mPorSpinner
								.getSelectedItem());

						// Log.i(TAG, countrySelected);

						if (countrySelected.equals(DR1)
								|| countrySelected.equals(DR2)
								|| countrySelected.equals(DR3)
								|| countrySelected.equals(DR4)) {

							for (int i = 0; i < mRaceView.getChildCount(); i++) {
								View view = mRaceView.getChildAt(i);
								view.setVisibility(View.VISIBLE);
							}
							mRaceView.setVisibility(View.VISIBLE);

						} else {
							for (int i = 0; i < mRaceView.getChildCount(); i++) {
								View view = mRaceView.getChildAt(i);
								view.setVisibility(View.GONE);
							}

							mRaceView.setVisibility(View.GONE);
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						// do nothing
					}
				});

		Context context = getApplicationContext();
		PropertyHolder.init(context);

		final Button saveRegistrationButton = (Button) findViewById(R.id.save_reg_button);
		saveRegistrationButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = getBaseContext();

				PropertyHolder.setUserId(userId);
				PropertyHolder.setRegistered(true);
				PropertyHolder.setServiceOn(true);
				PropertyHolder.setWithdrawn(false);
				PropertyHolder.setStoreMyData(true);
				
				// 19 December 2013: end research change
//				PropertyHolder.setShareData(true);
				PropertyHolder.setShareData(false);

				Intent schedulingIntent = new Intent(
						getResources().getString(R.string.internal_message_id)
								+ Util.MESSAGE_SCHEDULE);
				context.sendBroadcast(schedulingIntent);

				String phoneLanguage = Locale.getDefault().getDisplayLanguage();

				PackageInfo pInfo = null;
				try {
					pInfo = getPackageManager().getPackageInfo(
							getPackageName(), 0);
				} catch (NameNotFoundException e) {
				}
				String version = pInfo.versionName;

				String thisSDK = Integer.toString(Build.VERSION.SDK_INT);

				String sex = "x";
				String married = "x";
				String cit = "x";
				String bornRes = "x";
				String white = "x";
				String black = "x";
				String latino = "x";
				String nativeamerican = "x";
				String asian = "x";
				String other = "x";

				int sexId = mSexRadioGroup.getCheckedRadioButtonId();
				if (sexId == R.id.sexMale)
					sex = "m";
				if (sexId == R.id.sexFemale)
					sex = "f";

				int marriedId = mMarriedRadioGroup.getCheckedRadioButtonId();
				if (marriedId == R.id.marriedYes)
					married = "y";
				if (marriedId == R.id.marriedNo)
					married = "n";

				int citId = mCitRadioGroup.getCheckedRadioButtonId();
				if (citId == R.id.citYes)
					cit = "y";
				if (citId == R.id.citNo)
					cit = "n";

				int bornResId = mBornResRadioGroup.getCheckedRadioButtonId();
				if (bornResId == R.id.bornResYes)
					bornRes = "y";
				if (bornResId == R.id.bornResNo)
					bornRes = "n";

				String age = String.valueOf(mAgeSpinner.getSelectedItem());
				String kids = String.valueOf(mKidsSpinner.getSelectedItem());
				String pob = String.valueOf(mPobSpinner.getSelectedItem());
				String por = String.valueOf(mPorSpinner.getSelectedItem());
				String entryyear = String.valueOf(mEntryyearSpinner
						.getSelectedItem());
				String lang = String.valueOf(mLangSpinner.getSelectedItem());
				String ed = "";
				String inc = "";
				String hh = "";
				String emp = String.valueOf(mEmpSpinner
						.getSelectedItemPosition());

				if (mWhiteBox.isChecked())
					white = "y";
				if (mBlackBox.isChecked())
					black = "y";
				if (mLatinoBox.isChecked())
					latino = "y";
				if (mNativeAmericanBox.isChecked())
					nativeamerican = "y";
				if (mAsianBox.isChecked())
					asian = "y";
				if (mOtherBox.isChecked())
					other = "y";

				String responses = age + "," + sex + "," + por + "," + white
						+ "," + black + "," + latino + "," + nativeamerican
						+ "," + asian + "," + other + "," + married + ","
						+ kids + "," + bornRes + "," + pob + "," + entryyear
						+ "," + cit + "," + lang + "," + ed + "," + inc + ","
						+ hh + "," + emp + "," + phoneLanguage + "," + version
						+ "," + thisSDK;

				ContentResolver ucr = getContentResolver();

				ucr.insert(Util.getUploadQueueUri(context),
						UploadContentValues.createUpload("REG", responses));

				ucr.insert(
						Util.getUploadQueueUri(context),
						UploadContentValues.createUpload("CON",
								PropertyHolder.getConsentTime()));

				// try to upload them
				Intent i = new Intent(Registration.this, FileUploader.class);
				startService(i);

				// Util.toast(context,
				// "Activity-Space Mapper is now activated");

				// create an intent object and tell it where to go
				Intent intent2ASM = new Intent(Registration.this,
						MapMyData.class);
				// start the intent
				startActivity(intent2ASM);
				finish();
				return;
			}
		});

	}

}