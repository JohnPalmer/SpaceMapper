
/*
 * Mobility Mapper is a mobile phone app for studying activity spaces on campuses. It is based in part on code from the Human Mobility Project.
 *
 * Copyright (c) 2015 John R.B. Palmer.
 *
 * Mobility Mapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Mobility Mapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
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
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class TransportationModeSurvey extends Activity {

    Spinner s;

    Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.transport_mode_survey);

        s = (Spinner) findViewById(R.id.spinner);

        Button b = (Button) findViewById(R.id.messageButtonOK);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject tra_json_data = new JSONObject();
                String tra_json_data_string;
                try {
                    tra_json_data.put(DataCodeBook.TRANSPORT_KEY_TIME, Util.iso8601(System.currentTimeMillis()));
                    tra_json_data.put(DataCodeBook.TRANSPORT_KEY_MODE_RESPONSE, s.getSelectedItem().toString());
                    tra_json_data_string = tra_json_data.toString();
                    ContentResolver ucr = getContentResolver();
                    ucr.insert(Util.getUploadQueueUri(context),
                            UploadContentValues.createUpload(DataCodeBook.TRANSPORT_PREFIX, tra_json_data_string));
                } catch (JSONException e) {
                    //todo
                }

                sendBroadcast(new Intent(getResources().getString(R.string.internal_message_id) + Util.MESSAGE_CANCEL_C_NOTIFICATION));

                Util.toast(context, getResources().getString(R.string.thankyou));

                startActivity(new Intent(TransportationModeSurvey.this, MapMyData.class));
                finish();
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();

    }


}