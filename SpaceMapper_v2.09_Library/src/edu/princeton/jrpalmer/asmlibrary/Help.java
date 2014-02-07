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

import edu.princeton.jrpalmer.asmlibrary.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Provides the help menu.
 * 
 * @author John R.B. Palmer
 * 
 */
public class Help extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		Context context = getApplicationContext();
		PropertyHolder.init(context);

		final TextView mFAQs = (TextView) findViewById(R.id.FAQSelector);
		mFAQs.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// create an intent object and tell it where to go
				Intent i = new Intent(Help.this, FAQs.class);
				// start the intent
				startActivity(i);
			}
		});

		final TextView mContact = (TextView) findViewById(R.id.contactSelector);
		mContact.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// create an intent object and tell it where to go
				Intent i = new Intent(Help.this, Contact.class);
				// start the intent
				startActivity(i);
			}
		});

		final TextView mReviewConsent = (TextView) findViewById(R.id.reviewConsentSelector);
		mReviewConsent.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// create an intent object and tell it where to go
				Intent i = new Intent(Help.this, ReviewConsent.class);
				// start the intent
				startActivity(i);
			}
		});

		final TextView mWithdraw = (TextView) findViewById(R.id.withdrawSelector);
		mWithdraw.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// create an intent object and tell it where to go
				Intent i = new Intent(Help.this, Withdraw.class);
				// start the intent
				startActivity(i);
			}
		});

		String userID = PropertyHolder.getUserId();

		final TextView mUserID = (TextView) findViewById(R.id.userIDText);
		mUserID.setText(userID);

	}

	@Override
	protected void onResume() {

		if (Util.trafficCop(this))
			finish();
		super.onResume();

	}

}
