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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import net.movelab.cmlibrary.R;
import net.movelab.cmlibrary.Fix.Fixes;

public class ListMyDataCursorLoader extends FragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADER_ID = 0x02;

	// String TAG = "ListMyData";
	TextView mNewFixBlinker;
	int color2;
	int color1;
	CountDownTimer newFixBlinkerTimer;
	Context context;
	ListView mListView;
	ListDataCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_my_data);

		context = getBaseContext();

		getSupportLoaderManager().initLoader(LOADER_ID, null, this);

		adapter = new ListDataCursorAdapter(this, R.layout.dblistitem, null,
				new String[] { "timelong", "latitude", "longitude", "altitude",
						"accuracy", "provider", "sdtimelong" }, new int[] {
						R.id.firstTimeLong, R.id.latitude, R.id.longitude,
						R.id.altitude, R.id.accuracy, R.id.provider,
						R.id.sdTimeLong }, Adapter.NO_SELECTION);

		ListView listView = (ListView) findViewById(R.id.listDataListView);
		listView.setAdapter(adapter);

		listView.setFastScrollEnabled(true);

		mNewFixBlinker = (TextView) findViewById(R.id.newFixBlinker);
		mNewFixBlinker.setVisibility(View.INVISIBLE);

		// Log.e(TAG, "onCreate");
	}

	public class FixReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateList(intent);
		}

	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

		return new CursorLoader(context, Util.getFixesUri(context),
				Fixes.KEYS_ALL, null, null, Fixes.KEY_ROWID + " DESC");
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}

	public void updateList(Intent intent) {

		if (newFixBlinkerTimer != null) {
			newFixBlinkerTimer.cancel();
		}

		mNewFixBlinker.setVisibility(View.VISIBLE);
		color2 = Color.BLACK;
		color1 = getResources().getColor(R.color.light_yellow);
		newFixBlinkerTimer = new CountDownTimer(3000, 300) {
			public void onTick(long millisUntilFinished) {
				mNewFixBlinker.setTextColor(color1);
				int temp = color1;
				color1 = color2;
				color2 = temp;
			}

			public void onFinish() {
				mNewFixBlinker.setVisibility(View.INVISIBLE);
			}
		}.start();

	}

	FixReceiver fixReceiver;

	@Override
	protected void onResume() {

		IntentFilter fixFilter;
		fixFilter = new IntentFilter(getResources().getString(R.string.internal_message_id)
				+ Util.MESSAGE_FIX_RECORDED);
		fixReceiver = new FixReceiver();
		registerReceiver(fixReceiver, fixFilter);

		if (Util.trafficCop(this))
			finish();
		super.onResume();

	}

	static final private int LOCATE_NOW = Menu.FIRST;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, LOCATE_NOW, Menu.NONE, R.string.menu_locate_now);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (LOCATE_NOW): {
			if (Util.locatingNow == false) {
				Intent i = new Intent(ListMyDataCursorLoader.this, FixGet.class);
				startService(i);
			}
			return true;
		}
		}
		return false;
	}

	@Override
	protected void onPause() {
		unregisterReceiver(fixReceiver);
		super.onPause();
	}

}