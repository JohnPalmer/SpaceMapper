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
 **/

package edu.princeton.jrpalmer.asmlibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.princeton.jrpalmer.asmlibrary.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Displays the GPL.
 * 
 * @author John R.B. Palmer
 * 
 */
public class GPLView extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gpl_view);
		TextView t = (TextView) findViewById(R.id.gplView);

		t.setText(readTxt());
		t.setTextColor(getResources().getColor(R.color.light_yellow));
		t.setTextSize(getResources().getDimension(R.dimen.textsize_normal));
	}

	private String readTxt() {
		InputStream inputStream = null;
		inputStream = getResources().openRawResource(R.raw.gpl);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
		} catch (IOException e) {

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {

				}
			}
		}
		return byteArrayOutputStream.toString();
	}
	
	@Override
	protected void onResume() {


		if(Util.trafficCop(this))
			finish();		
		super.onResume();

	}


}
