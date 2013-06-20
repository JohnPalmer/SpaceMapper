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

import edu.princeton.jrpalmer.asmlibrary.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

/**
 * Allows user to review the consent form and see when they signed it.
 * 
 * @author John R.B. Palmer
 * 
 */
public class ReviewConsent extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.review_consent);

		Context context = getApplicationContext();
		
		if (PropertyHolder.isInit() == false)
			PropertyHolder.init(context);
		
		TextView mReviewConsent = (TextView) findViewById(R.id.reviewConsentText);
		mReviewConsent.setText(Html.fromHtml(getString(R.string.consent_text)));
		mReviewConsent.setTextColor(getResources().getColor(
				R.color.light_yellow));
		mReviewConsent.setTextSize(15);

		TextView mConsentTime = (TextView) findViewById(R.id.consentDateText);
		mConsentTime.setText(PropertyHolder.getConsentTime());
		mConsentTime.setTextColor(Color.WHITE);
		mConsentTime.setTextSize(15);

	}

}