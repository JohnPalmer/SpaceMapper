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

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import net.movelab.cmlibrary.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Allows user to provide answers to survey before starting using the app. All
 * responses are encrypted and uploaded to the server.
 *
 * @author John R.B. Palmer
 */
public class Registration extends Activity {
    // private static final String TAG = "Registration";


    String userId = UUID.randomUUID().toString();

    private EditText survey_code_entry;
    private EditText email_entry;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_new);

        Context context = getApplicationContext();
        PropertyHolder.init(context);

        survey_code_entry = (EditText) findViewById(R.id.surveyCodeEntry);
        email_entry = (EditText) findViewById(R.id.emailEntry);

        final Button saveRegistrationButton = (Button) findViewById(R.id.save_reg_button);
        saveRegistrationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Context context = getBaseContext();

                PropertyHolder.setUserId(userId);
                PropertyHolder.setRegistered(true);
                PropertyHolder.setServiceOn(true);
                PropertyHolder.setWithdrawn(false);
                PropertyHolder.setStoreMyData(true);


                Intent schedulingIntent = new Intent(
                        getResources().getString(R.string.internal_message_id)
                                + Util.MESSAGE_SCHEDULE);
                context.sendBroadcast(schedulingIntent);

                String phoneLanguage = Locale.getDefault().getDisplayLanguage();

                String version = Util.getAppVersion(context);

                String thisSDK = Integer.toString(Build.VERSION.SDK_INT);


                String user_code = "";
                CharSequence survey_code_chars = survey_code_entry.getText();
                if (survey_code_chars != null && survey_code_chars.length() > 0) {
                    user_code = survey_code_chars.toString();
                }

                String user_email = "";
                CharSequence email_chars = email_entry.getText();
                if (email_chars != null && email_chars.length() > 0) {
                    user_email = email_chars.toString();
                }


                PropertyHolder.setUserCode(user_code);


                JSONObject response_json = new JSONObject();
                JSONObject consent_json = new JSONObject();
                try {
                    response_json.put(DataCodeBook.REGISTRATION_KEY_PHONE_LANG, phoneLanguage);
                    response_json.put(DataCodeBook.REGISTRATION_KEY_VERSION, version);
                    response_json.put(DataCodeBook.REGISTRATION_KEY_SDK, thisSDK);
                    response_json.put(DataCodeBook.REGISTRATION_KEY_USER_CODE, user_code);
                    response_json.put(DataCodeBook.REGISTRATION_KEY_USER_EMAIL, user_email);

                    consent_json.put(DataCodeBook.CONSENT_KEY_CONSENT_TIME, PropertyHolder.getConsentTime());

                } catch (JSONException e) {
//todo
                }

                String responses = response_json.toString();
                String consent_json_string = consent_json.toString();

                ContentResolver ucr = getContentResolver();

                ucr.insert(Util.getUploadQueueUri(context),
                        UploadContentValues.createUpload(DataCodeBook.REGISTRATION_PREFIX, responses));

                ucr.insert(
                        Util.getUploadQueueUri(context),
                        UploadContentValues.createUpload(DataCodeBook.CONSENT_PREFIX,
                                consent_json_string));

                // try to upload them
                Intent i = new Intent(Registration.this, FileUploader.class);
                startService(i);

                // Util.toast(context,
                // "Activity-Space Mapper is now activated");

                sendBroadcast(new Intent(context.getResources().getString(
                        R.string.internal_message_id)
                        + Util.MESSAGE_START_MESSAGE_AB_TIMER));

                // todo make this only for expert version
                sendBroadcast(new Intent(context.getResources().getString(
                        R.string.internal_message_id)
                        + Util.MESSAGE_START_MESSAGE_C_TIMER));

                // create an intent object and tell it where to go
                Intent intent2ASM = new Intent(Registration.this,
                        MapMyData.class);
                // start the intent
                startActivity(intent2ASM);
                finish();
            }
        });

    }

}