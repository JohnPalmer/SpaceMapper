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

import android.provider.BaseColumns;

public class Fix {

	public Fix() {
	}

	public static final class Fixes implements BaseColumns {
		private Fixes() {
		}


		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.palmerasmlibrary.fixes";

		/** The row ID key name */
		public static final String KEY_ROWID = "_id";

		/** The timelong key name */
		public static final String KEY_TIMELONG = "timelong";

		public static final String KEY_ACCURACY = "accuracy";

		public static final String KEY_ALTITUDE = "altitude";

		public static final String KEY_LATITUDE = "latitude";

		public static final String KEY_LONGITUDE = "longitude";

		public static final String KEY_PROVIDER = "provider";
		
		public static final String KEY_STATION_DEPARTURE_TIMELONG = "sdtimelong";

		/** The names of all the fields contained in the location fix table */
		public static final String[] KEYS_ALL = { KEY_ROWID, KEY_ACCURACY,
				KEY_ALTITUDE, KEY_LATITUDE, KEY_LONGITUDE, KEY_PROVIDER,
				KEY_TIMELONG, KEY_STATION_DEPARTURE_TIMELONG };

		public static final String[] KEYS_SAVECSV = { KEY_ACCURACY,
				KEY_ALTITUDE, KEY_LATITUDE, KEY_LONGITUDE, KEY_PROVIDER,
				KEY_TIMELONG, KEY_STATION_DEPARTURE_TIMELONG};

		public static final String[] KEYS_LATLON = { KEY_ROWID, KEY_LATITUDE,
				KEY_LONGITUDE };

		public static final String[] KEYS_LATLONACC = { KEY_ROWID,
				KEY_LATITUDE, KEY_LONGITUDE, KEY_ACCURACY };

		public static final String[] KEYS_LATLONTIME = { KEY_ROWID,
				KEY_LATITUDE, KEY_LONGITUDE, KEY_TIMELONG };

		public static final String[] KEYS_LATLONACCTIMES = { KEY_ROWID,
			KEY_LATITUDE, KEY_LONGITUDE, KEY_ACCURACY, KEY_TIMELONG, KEY_STATION_DEPARTURE_TIMELONG };

	}

}
