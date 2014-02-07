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

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;
import edu.princeton.jrpalmer.asmlibrary.Fix.Fixes;

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
