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

import net.movelab.cmlibrary.Fix.Fixes;
import android.content.ContentValues;
import android.location.Location;

public class LocationContentValues {
	
	
	/**
	 * Creates and content values from location fix data.
	 * 
	 * @param accuracy
	 *            The radial accuracy in meters of this fix
	 * @param altitude
	 *            The altitude of this fix
	 * @param latitude
	 * @param longitude
	 * @param provider
	 *            Either network or gps
	 * @param time
	 *            The time this fix was taken
	 * @return rowId if fix record successfully created and added, -1 if the
	 *         insert failed.
	 */
	public static ContentValues createFix(float accuracy, double altitude, double latitude,
			double longitude, String provider, long time, long sdtimelong) {

		// Log.e(TAG, "we are in the createFix part of the DB adapter...");

		ContentValues initialValues = new ContentValues();
		initialValues.put(Fixes.KEY_ACCURACY, (double) accuracy);
		initialValues.put(Fixes.KEY_ALTITUDE, (double) altitude);
		initialValues.put(Fixes.KEY_LATITUDE, (double) latitude);
		initialValues.put(Fixes.KEY_LONGITUDE, (double) longitude);
		initialValues.put(Fixes.KEY_PROVIDER, provider);
		initialValues.put(Fixes.KEY_TIMELONG, (long) time);
		initialValues.put(Fixes.KEY_STATION_DEPARTURE_TIMELONG, sdtimelong);

		return initialValues;
	}

	
	/**
	 * Creates content values from Location object.
	 * 
	 * @param loc
	 *            The Location Object containing this fix
	 * @return ContentValues.
	 */
	public static ContentValues createFix(Location loc, long sdtimelong) {
		return createFix(loc.getAccuracy(), loc.getAltitude(),
				loc.getLatitude(), loc.getLongitude(), loc.getProvider(),
				loc.getTime(), sdtimelong);
	}



}
