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
import edu.princeton.jrpalmer.asmlibrary.Fix.Fixes;

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
	
	// 18 December 2013: end of data collection:
	if(PropertyHolder.getShareData()){
		PropertyHolder.setShareData(false);
		buildEndResearchAnnouncement();		
	} else{

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

		if (PropertyHolder.getUploadOldFiles()) {
			Intent oldFileUploaderIntent = new Intent(SplashScreen.this,
					OldFileUploader.class);
			startService(oldFileUploaderIntent);
		}

		Intent intent = new Intent(SplashScreen.this, MapMyData.class);
		startActivity(intent);
		finish();
	}
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

								if (PropertyHolder.getUploadOldFiles()) {
									Intent oldFileUploaderIntent = new Intent(SplashScreen.this,
											OldFileUploader.class);
									startService(oldFileUploaderIntent);
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

