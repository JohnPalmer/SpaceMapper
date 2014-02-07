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

import android.content.ContentValues;
import android.location.Location;
import edu.princeton.jrpalmer.asmlibrary.Fix.Fixes;

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
