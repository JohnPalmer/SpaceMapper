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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import net.movelab.cmlibrary.R;
import net.movelab.cmlibrary.Fix.Fixes;

/**
 * Initial screen triggered when app starts. This is used mainly to direct app
 * where to go, depending on what user has done so far.
 * 
 * @author John R.B. Palmer
 * 
 */
public class SplashScreen extends Activity {

	static Context context;

	// private static final String TAG = "ActivitySpace";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash);

		context = getApplicationContext();
		PropertyHolder.init(context);
		
	if (context.getPackageName().toLowerCase().contains("asmpro")) {
			PropertyHolder.setProVersion(true);
		}
	

		if (Util.trafficCop(this))
			finish();
		// check if there was a database update that tried to
		// trigger a values update before any crash
		Util.needDatabaseUpdate = PropertyHolder.needDatabaseValueUpdate();

		// open and close database in order to trigger any updates
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(Util.getFixesUri(context),
				new String[] { Fixes.KEY_ROWID }, null, null, null);
		c.close();

		if (PropertyHolder.isServiceOn()) {
			Intent scheduler = new Intent(
					getResources().getString(R.string.internal_message_id)
							+ Util.MESSAGE_SCHEDULE);
			context.sendBroadcast(scheduler);
		}


		Intent intent = new Intent(SplashScreen.this, MapMyData.class);
		startActivity(intent);
		finish();

	}
	private void buildEndResearchAnnouncement() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		

		final SpannableString s = new SpannableString(
				getResources().getString(R.string.end_research_msg)
				+ "\n\n" + getResources().getString(R.string.project_website));
		Linkify.addLinks(s, Linkify.ALL);

		builder.setMessage(s)
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								if (PropertyHolder.getTryingToWithdraw()) {
									Intent intent = new Intent(context, Withdraw.class);
									context.startActivity(intent);
									finish();
								}

								// check if there was a database update that tried to
								// trigger a values update before any crash
								Util.needDatabaseUpdate = PropertyHolder.needDatabaseValueUpdate();

								// open and close database in order to trigger any updates
								ContentResolver cr = getContentResolver();
								Cursor c = cr.query(Util.getFixesUri(context),
										new String[] { Fixes.KEY_ROWID }, null, null, null);
								c.close();

								if (PropertyHolder.isServiceOn()) {
									Intent scheduler = new Intent(
											getResources().getString(R.string.internal_message_id)
													+ Util.MESSAGE_SCHEDULE);
									context.sendBroadcast(scheduler);
								}


								Intent intent = new Intent(SplashScreen.this, MapMyData.class);
								startActivity(intent);
								finish();

							}
						});
		final AlertDialog alert = builder.create();
		alert.show();

		((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
		
	}

}

