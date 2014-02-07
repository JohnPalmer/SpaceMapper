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
 */

package edu.princeton.jrpalmer.asmlibrary;

import edu.princeton.jrpalmer.asmlibrary.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Shows the introductory text when user first installs app.
 * 
 * @author John R.B. Palmer
 * 
 */
public class Intro extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intro);

		PropertyHolder.init(getApplicationContext());

		TextView intro = (TextView) findViewById(R.id.introText);
		intro.setText(Html.fromHtml(getString(R.string.intro_text)));
		intro.setTextColor(getResources().getColor(R.color.light_yellow));
		intro.setTextSize(getResources().getDimension(R.dimen.textsize_normal));

		final TextView mWeb = (TextView) findViewById(R.id.webLink);
		Linkify.addLinks(mWeb, Linkify.ALL);
		mWeb.setLinkTextColor(getResources().getColor(R.color.light_yellow));
		mWeb.setTextSize(getResources().getDimension(R.dimen.textsize_url));

		final TextView mEmail = (TextView) findViewById(R.id.emailLink);
		Linkify.addLinks(mEmail, Linkify.ALL);
		mEmail.setLinkTextColor(getResources().getColor(R.color.light_yellow));
		mEmail.setTextSize(getResources().getDimension(R.dimen.textsize_url));

		final Button mIntroButtonOver = (Button) findViewById(R.id.introButtonOver18);
		mIntroButtonOver.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// create an intent object and tell it where to go
				Intent i = new Intent(Intro.this, Consent.class);
				// start the intent
				startActivity(i);
				finish();

			}
		});

		final Button mIntroButtonUnder = (Button) findViewById(R.id.introButtonUnder18);
		mIntroButtonUnder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (PropertyHolder.getProVersion()) {

					PropertyHolder.setShareData(false);

					Intent i = new Intent(Intro.this, MapMyData.class);
					startActivity(i);

					finish();

				} else {
					Intent i = new Intent(Intro.this, UnderAgeMessage.class);
					// start the intent
					startActivity(i);
					finish();
				}

			}
		});

	}

	@Override
	public void onResume() {

		if (PropertyHolder.isRegistered())
			finish();

		super.onResume();

	}

}
