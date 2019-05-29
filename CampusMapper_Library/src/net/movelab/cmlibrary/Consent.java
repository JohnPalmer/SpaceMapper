
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Displays the IRB consent form and allows users to consent or decline.
 *
 * @author John R.B. Palmer
 */
public class Consent extends Activity {

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.consent);
        context = getApplicationContext();
        PropertyHolder.init(context);

        TextView consent = (TextView) findViewById(R.id.consenttext);
        consent.setText(Html.fromHtml(getString(R.string.consent_text)));
//        consent.setTextColor(Color.WHITE);
//        consent.setTextSize(getResources()
//                .getDimension(R.dimen.textsize_normal));

        final Button consentButton = (Button) findViewById(R.id.consent_button);
        consentButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Date now = new Date(System.currentTimeMillis());
                String consentTime = Util.userDate(now);
                PropertyHolder.setConsentTime(consentTime);
                PropertyHolder.setConsent(true);

                if (PropertyHolder.isRegistered()) {
                    if (PropertyHolder.getUserId() != null) {
                        ContentResolver ucr = getContentResolver();

                        JSONObject json_data = new JSONObject();
                        String json_data_string;
                        try {
                            json_data.put(DataCodeBook.CONSENT_KEY_CONSENT_TIME, consentTime);
                            json_data_string = json_data.toString();
                            ucr.insert(Util.getUploadQueueUri(context), UploadContentValues
                                    .createUpload(DataCodeBook.CONSENT_PREFIX, json_data_string));
                        } catch (JSONException e) {
                            // todo
                        }

                    }
                    Intent i = new Intent(Consent.this, MapMyData.class);
                    startActivity(i);
                    finish();
                    return;
                } else {

                    Intent i = new Intent(Consent.this, Registration.class);
                    // start the intent
                    startActivity(i);
                    finish();
                    return;
                }
            }
        });

        final Button consentDeclineButton = (Button) findViewById(R.id.consent_decline_button);
        consentDeclineButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                finish();
                return;
            }
        });

    }

}
