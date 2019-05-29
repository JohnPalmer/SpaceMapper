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

import net.movelab.cmlibrary.R;
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
