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

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * Allows user to delete all app data on the phone and send message to server
 * requesting that all data be deleted there as well.
 * 
 * @author John R.B. Palmer
 * 
 */
public class Withdraw extends Activity {
	// private static final String TAG = "Withdraw";
	Context context;
	LinearLayout progressBarArea;
	ProgressBar progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();

		if (PropertyHolder.isInit() == false)
			PropertyHolder.init(context);

		if (PropertyHolder.getTryingToWithdraw())
			buildAlertMessage2();

		setContentView(R.layout.withdraw);

		progressBarArea = (LinearLayout) findViewById(R.id.withdrawProgressNotificationArea);
		progressBar = (ProgressBar) findViewById(R.id.withdrawProgressbar);
		progressBarArea.setVisibility(View.INVISIBLE);

		final Button mWithdrawButton = (Button) findViewById(R.id.withdrawDeleteButton);
		mWithdrawButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (Util.isOnline(context))
					buildAlertMessage1();
				else
					buildAlertMessage3();

				return;
			}
		});

	}

	private void buildAlertMessage1() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.withdraw_warning))
				.setCancelable(false)
				.setPositiveButton(
						getResources().getString(R.string.withdraw_warning_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								// Stop service if it is currently running
								Intent i2FixGet = new Intent(Withdraw.this,
										FixGet.class);
								stopService(i2FixGet);
								// now unschedule
								Intent intent = new Intent(
										getString(R.string.internal_message_id)
												+ Util.MESSAGE_UNSCHEDULE);
								context.sendBroadcast(intent);

								PropertyHolder.setShareData(false);

								if (!PropertyHolder.getProVersion()) {
									PropertyHolder.setConsent(false);
									PropertyHolder.setRegistered(false);
								}
								new WithdrawTask().execute(context);
							}
						})
				.setNegativeButton(getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildAlertMessage2() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.withdraw_error))
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								new WithdrawTask().execute(context);
								dialog.cancel();
							}
						})
				.setNegativeButton(getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								PropertyHolder.setTryingToWithdraw(false);
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildAlertMessage3() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.offline_warning))
				.setCancelable(true)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public class WithdrawTask extends AsyncTask<Context, Integer, Boolean> {

		int myProgress;
		String resultUsed = "0 bytes";
		String resultAvailable = "0 bytes";
		boolean sent = false;

		@Override
		protected void onPreExecute() {

			progressBarArea.setVisibility(View.VISIBLE);
			myProgress = 0;

		}

		protected Boolean doInBackground(Context... context) {

			sent = Util.uploadEncryptedString(context[0], "WIT", "withdraw",
					Util.SERVER);

			if (sent)
				myProgress = 25;

			publishProgress(myProgress);

			ContentResolver cr = getContentResolver();
			cr.delete(Util.getFixesUri(context[0]), null, null);

			myProgress = myProgress + 25;
			publishProgress(myProgress);

			cr.delete(Util.getUploadQueueUri(context[0]), null, null);

			myProgress = myProgress + 25;
			publishProgress(myProgress);

			if (context[0].getFilesDir().getAbsolutePath() != null) {

				File directory = new File(context[0].getFilesDir()
						.getAbsolutePath());
				File[] files = directory.listFiles();
				int nFiles = files.length;
				int i = 0;
				for (File f : files) {
					myProgress = myProgress + (int) 25 * (i / nFiles);
					publishProgress(myProgress);
					f.delete();
					i++;
				}
			}

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

			progressBar.setProgress(progress[0]);

		}

		protected void onPostExecute(Boolean result) {

			progressBarArea.setVisibility(View.INVISIBLE);

			if (sent) {

				if (PropertyHolder.getProVersion()) {

					PropertyHolder.setTryingToWithdraw(false);

					Util.toast(
							context,
							getResources().getString(
									R.string.withdraw_message_pro));
					Intent i = new Intent(Withdraw.this, MapMyData.class);
					startActivity(i);

					finish();

				} else {
					PropertyHolder.setWithdrawn(true);
					PropertyHolder.setTryingToWithdraw(false);
					Intent i = new Intent(Withdraw.this, WithdrawLock.class);
					startActivity(i);
					finish();
				}
			} else {
				buildAlertMessage2();
				PropertyHolder.setTryingToWithdraw(true);
			}

		}
	}

}
