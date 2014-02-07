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
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import edu.princeton.jrpalmer.asmlibrary.R;

/**
 * Displays the About screen.
 * 
 * @author John R.B. Palmer
 * 
 */
public class About extends Activity {
	// TODO add credits here and in all copyright info for the authors of the
	// double thumb slider and the apprater. (Anything else?)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		TextView t = (TextView) findViewById(R.id.aboutText);
		t.setText(Html.fromHtml(getString(R.string.about_html)));
		t.setTextColor(getResources().getColor(R.color.light_yellow));
		t.setTextSize(15);

		final TextView mWeb = (TextView) findViewById(R.id.webLink);
		Linkify.addLinks(mWeb, Linkify.ALL);
		mWeb.setLinkTextColor(getResources().getColor(R.color.light_yellow));
		mWeb.setTextSize(getResources().getDimension(R.dimen.textsize_url));

		final TextView mEmail = (TextView) findViewById(R.id.emailLink);
		Linkify.addLinks(mEmail, Linkify.ALL);
		mEmail.setLinkTextColor(getResources().getColor(R.color.light_yellow));
		mEmail.setTextSize(getResources().getDimension(R.dimen.textsize_url));

	}

	@Override
	protected void onResume() {

		if(Util.trafficCop(this))
			finish();
		
		super.onResume();

	}

	static final private int GPL = Menu.FIRST;
	static final private int LGPL = Menu.FIRST + 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, GPL, Menu.NONE, R.string.GPL);
		menu.add(0, LGPL, Menu.NONE, R.string.LGPL);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (GPL): {
			Intent i = new Intent(this, GPLView.class);
			startActivity(i);
			return true;
		}
		case (LGPL): {
			Intent i = new Intent(this, LGPLView.class);
			startActivity(i);
			return true;
		}
		}
		return false;
	}

}