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

public class Upload {

	public Upload() {
	}

	public static final class Uploads implements BaseColumns {
		private Uploads() {
		}


		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.palmerasmlibrary.uploads";

		/** The row ID key name */
		public static final String KEY_ROWID = "_id";

		/** The timelong key name */
		public static final String KEY_PREFIX = "prefix";

		public static final String KEY_DATA = "data";

		/** The names of all the fields contained in the location fix table */
		public static final String[] KEYS_ALL = { KEY_ROWID, KEY_PREFIX,
				KEY_DATA };


	}

}
