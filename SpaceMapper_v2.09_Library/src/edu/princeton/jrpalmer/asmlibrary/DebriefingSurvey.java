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
 **/

package edu.princeton.jrpalmer.asmlibrary;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;

/**
 * Displays the Debriefing Survey.
 * 
 * @author John R.B. Palmer
 * 
 */
public class DebriefingSurvey extends Activity {

	private CheckBox dbqHowFoundFriendBox;
	private CheckBox dbqHowFoundAdBox;
	private CheckBox dbqHowFoundMarketBox;
	private CheckBox dbqHowFoundOtherBox;

	private RadioGroup dbqFriendsRadioGroup;
	private RadioGroup dbqBatteryRadioGroup;
	private RadioGroup dbqBehavior1RadioGroup;
	private RadioGroup dbqBehavior2RadioGroup;

	private Button dbSubmitButton;
	private Button dbCancelButton;

	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.debriefing_survey);

		context = getApplicationContext();

		dbqHowFoundFriendBox = (CheckBox) findViewById(R.id.dbHowFoundFriendBox);
		dbqHowFoundAdBox = (CheckBox) findViewById(R.id.dbHowFoundAdBox);
		dbqHowFoundMarketBox = (CheckBox) findViewById(R.id.dbHowFoundMarketBox);
		dbqHowFoundOtherBox = (CheckBox) findViewById(R.id.dbHowFoundOtherBox);

		dbqFriendsRadioGroup = (RadioGroup) findViewById(R.id.dbqFriendsRadioGroup);
		dbqBatteryRadioGroup = (RadioGroup) findViewById(R.id.dbqBatteryRadioGroup);
		dbqBehavior1RadioGroup = (RadioGroup) findViewById(R.id.dbqBehavior1RadioGroup);
		dbqBehavior2RadioGroup = (RadioGroup) findViewById(R.id.dbqBehavior2RadioGroup);

		dbSubmitButton = (Button) findViewById(R.id.dbSaveButton);
		dbCancelButton = (Button) findViewById(R.id.dbCancelButton);

	}

	@Override
	protected void onResume() {

		dbSubmitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String responses = (dbqHowFoundFriendBox.isChecked() ? "y"
						: "n")
						+ (dbqHowFoundAdBox.isChecked() ? "y" : "n")
						+ (dbqHowFoundMarketBox.isChecked() ? "y" : "n")
						+ (dbqHowFoundOtherBox.isChecked() ? "y" : "n")
						+ (dbqFriendsRadioGroup.getCheckedRadioButtonId() == R.id.dbqFriendsYes ? "y"
								: "n")
						+ (dbqBatteryRadioGroup.getCheckedRadioButtonId() == R.id.dbqFriendsYes ? "y"
								: "n")
						+ (dbqBehavior1RadioGroup.getCheckedRadioButtonId() == R.id.dbqFriendsYes ? "y"
								: "n")
						+ (dbqBehavior2RadioGroup.getCheckedRadioButtonId() == R.id.dbqFriendsYes ? "y"
								: "n");

				ContentResolver ucr = getContentResolver();

				ucr.insert(Util.getUploadQueueUri(context),
						UploadContentValues.createUpload("DEB", responses));

				Intent i = new Intent(DebriefingSurvey.this, FileUploader.class);
				startService(i);

				finish();

			}

		});

		dbCancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}

		});

		super.onResume();

	}
}