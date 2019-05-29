
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import net.movelab.cmlibrary.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Displays the Debriefing Survey.
 *
 * @author John R.B. Palmer
 */
public class DebriefingSurvey extends Activity {

    private CheckBox dbqHowFoundFriendBox;
    private CheckBox dbqHowFoundAdBox;
    private CheckBox dbqHowFoundMarketBox;
    private CheckBox dbqHowFoundOtherBox;

    private RadioGroup dbqFriendsRadioGroup;
    private RadioGroup dbqBatteryRadioGroup;
    private RadioGroup dbqBehavior1RadioGroup;
    private RadioGroup dbqBehavior2RadioGroup;

    private Button dbSubmitButton;
    private Button dbCancelButton;

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.debriefing_survey);

        context = getApplicationContext();

        dbqHowFoundFriendBox = (CheckBox) findViewById(R.id.dbHowFoundFriendBox);
        dbqHowFoundAdBox = (CheckBox) findViewById(R.id.dbHowFoundAdBox);
        dbqHowFoundMarketBox = (CheckBox) findViewById(R.id.dbHowFoundMarketBox);
        dbqHowFoundOtherBox = (CheckBox) findViewById(R.id.dbHowFoundOtherBox);

        dbqFriendsRadioGroup = (RadioGroup) findViewById(R.id.dbqFriendsRadioGroup);
        dbqBatteryRadioGroup = (RadioGroup) findViewById(R.id.dbqBatteryRadioGroup);
        dbqBehavior1RadioGroup = (RadioGroup) findViewById(R.id.dbqBehavior1RadioGroup);
        dbqBehavior2RadioGroup = (RadioGroup) findViewById(R.id.dbqBehavior2RadioGroup);

        dbSubmitButton = (Button) findViewById(R.id.dbSaveButton);
        dbCancelButton = (Button) findViewById(R.id.dbCancelButton);

    }

    @Override
    protected void onResume() {

        dbSubmitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                JSONObject json_data = new JSONObject();
                String json_data_string;
                try {
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_HOW_FOUND_FRIEND, (dbqHowFoundFriendBox.isChecked() ? "y" : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_HOW_FOUND_AD, (dbqHowFoundAdBox.isChecked() ? "y" : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_HOW_FOUND_MARKET, (dbqHowFoundMarketBox.isChecked() ? "y" : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_HOW_FOUND_OTHER, (dbqHowFoundOtherBox.isChecked() ? "y" : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_FRIENDS_USING_APP, (dbqFriendsRadioGroup.getCheckedRadioButtonId() == R.id.dbqFriendsYes ? "y"
                            : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_FRIENDS_USING_APP, (dbqFriendsRadioGroup.getCheckedRadioButtonId() == R.id.dbqFriendsYes ? "y"
                            : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_BATTERY_PROBLEMS, (dbqBatteryRadioGroup.getCheckedRadioButtonId() == R.id.dbqBatteryYes ? "y"
                            : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_BEHAVIOR_1, (dbqBehavior1RadioGroup.getCheckedRadioButtonId() == R.id.dbqBehavior1Yes ? "y"
                            : "n"));
                    json_data.put(DataCodeBook.DEBRIEFING_KEY_BEHAVIOR_2, (dbqBehavior2RadioGroup.getCheckedRadioButtonId() == R.id.dbqBehavior2Yes ? "y"
                            : "n"));

                    json_data_string = json_data.toString();

                    ContentResolver ucr = getContentResolver();

                    ucr.insert(Util.getUploadQueueUri(context),
                            UploadContentValues.createUpload(DataCodeBook.DEBREIFING_PREFIX, json_data_string));

                } catch (JSONException e) {
                    //todo
                }


                Intent i = new Intent(DebriefingSurvey.this, FileUploader.class);
                startService(i);

                finish();

            }

        });

        dbCancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                finish();

            }

        });

        super.onResume();

    }
}