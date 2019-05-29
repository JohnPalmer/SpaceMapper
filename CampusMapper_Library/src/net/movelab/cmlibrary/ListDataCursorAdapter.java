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

import java.util.Date;

import net.movelab.cmlibrary.R;
import net.movelab.cmlibrary.Fix.Fixes;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class ListDataCursorAdapter extends SimpleCursorAdapter {

	private Context context;

	private int layout;

	public ListDataCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flag) {
		super(context, layout, c, from, to, flag);

		this.context = context;
		this.layout = layout;

	}

	@Override
	public void bindView(View v, Context context, Cursor c) {

		String firstTime = Util.userDate(new Date((c.getLong(c
				.getColumnIndex(Fixes.KEY_TIMELONG)))));
		String sdTime = Util.userDate(new Date((c.getLong(c
				.getColumnIndex(Fixes.KEY_STATION_DEPARTURE_TIMELONG)))));

		TextView firstTimeText = (TextView) v.findViewById(R.id.firstTimeLong);
		if (firstTimeText != null) {
			firstTimeText.setText(firstTime);
		}
		TextView sdTimeText = (TextView) v.findViewById(R.id.sdTimeLong);
		if (sdTimeText != null) {
			sdTimeText.setText(sdTime);
		}

		TextView lat = (TextView) v.findViewById(R.id.latitude);
		if (lat != null) {
			lat.setText(String.format("%.5f",
					c.getDouble(c.getColumnIndex(Fixes.KEY_LATITUDE))));
		}

		TextView lon = (TextView) v.findViewById(R.id.longitude);
		if (lon != null) {
			lon.setText(String.format("%.5f",
					c.getDouble(c.getColumnIndex(Fixes.KEY_LONGITUDE))));
		}

		TextView alt = (TextView) v.findViewById(R.id.altitude);
		if (alt != null) {
			alt.setText(Double.toString(c.getDouble(c
					.getColumnIndex(Fixes.KEY_ALTITUDE))));
		}

		TextView acc = (TextView) v.findViewById(R.id.accuracy);
		if (acc != null) {
			acc.setText(Double.toString(c.getDouble(c
					.getColumnIndex(Fixes.KEY_ACCURACY))));
		}

		TextView prov = (TextView) v.findViewById(R.id.provider);
		if (prov != null) {
			prov.setText(c.getString(c.getColumnIndex(Fixes.KEY_PROVIDER)));
		}

	}

}
