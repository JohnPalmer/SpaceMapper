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

import edu.princeton.jrpalmer.asmlibrary.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Message to be displayed if user indicates that he or she is under 18 years
 * old (and so not eligible to participate as a research volunteer.
 * 
 * @author John R.B. Palmer
 * 
 */
public class UnderAgeMessage extends Activity {
	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();

		setContentView(R.layout.underage_view);

		final TextView mWeb = (TextView) findViewById(R.id.webLink);
		Linkify.addLinks(mWeb, Linkify.ALL);
		mWeb.setLinkTextColor(getResources().getColor(R.color.light_yellow));
		mWeb.setTextSize(getResources().getDimension(R.dimen.textsize_url));

		final TextView mEmail = (TextView) findViewById(R.id.emailLink);
		Linkify.addLinks(mEmail, Linkify.ALL);
		mEmail.setLinkTextColor(getResources().getColor(R.color.light_yellow));
		mEmail.setTextSize(getResources().getDimension(R.dimen.textsize_url));

	}

}
