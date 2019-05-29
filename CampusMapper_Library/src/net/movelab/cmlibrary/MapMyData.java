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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Allows user to view their own data in a map (from the database on their phone
 * -- not from the server).
 *
 * @author John R.B. Palmer
 */
public class MapMyData extends FragmentActivity implements OnMapReadyCallback {

    PolylineOptions routeLineOptionsBlack;
    Polyline polylineBlack;

    PolylineOptions routeLineOptionsColor;
    Polyline polylineColor;


    int circleRad = 4;

    int circleZ = 2;
    int lineZ = 1;
    float circleStrokeWidth = 1f;

    int routeColor = Color.parseColor("#2b8cbe");

    private int count = 0;
    private long startMillis = 0;

    Marker currentPosition;

    String TAG = "MapMyData";
    private ToggleButton mServiceButton;


    boolean loadingData = false;

    boolean shareData;

    private int lastRecId = 0;

    public static boolean reloadData = false;

    boolean isServiceOn;

    int BORDER_COLOR_MAP = 0xee4D2EFF;
    int FILL_COLOR_MAP = 0x554D2EFF;

    int BORDER_COLOR_SAT = 0xeeD9FCFF;
    int FILL_COLOR_SAT = 0xbbD9FCFF;

    int PZ_BORDER_COLOR = 0xee00ff00;
    int PZ_FILL_COLOR = 0x5500ff00;

    ProgressBar progressbar;

    List<LatLng> mPoints;

    public static String DATES_BUTTON_MESSAGE = "datesButtonMessage";

    LinearLayout progressNotificationArea;

    LinearLayout receiverNotificationArea;

    private TextView mReceiversOffWarning;

    Context context = this;

    GoogleMap thisMap;

    FixReceiver fixReceiver;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        PropertyHolder.init(context);

        if (Util.trafficCop(this))
            finish();

        if (!PropertyHolder.getInitialStartDateSet()) {
            Calendar now = Calendar.getInstance();
            now.setTimeInMillis(System.currentTimeMillis());
            PropertyHolder.setMapStartDate(now);
            PropertyHolder.setInitialStartDateSet(true);
        }

        setContentView(R.layout.map_layout);

        routeLineOptionsBlack = new PolylineOptions()
                .width(6)
                .color(Color.BLACK)
                .geodesic(true)
                .zIndex(lineZ);
        routeLineOptionsColor = new PolylineOptions()
                .width(4)
                .color(routeColor)
                .zIndex(lineZ)
                .geodesic(true);


        mPoints = new ArrayList<LatLng>();

        receiverNotificationArea = (LinearLayout) findViewById(R.id.mapReceiverNotificationArea);
        progressNotificationArea = (LinearLayout) findViewById(R.id.mapProgressNotificationArea);
        mReceiversOffWarning = (TextView) findViewById(R.id.mapReceiversOffWarning);
        progressbar = (ProgressBar) findViewById(R.id.mapProgressbar);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap map) {

        polylineBlack = map.addPolyline(routeLineOptionsBlack);
        polylineColor = map.addPolyline(routeLineOptionsColor);

        if (mPoints.size() > 0) {
            polylineBlack.setPoints(mPoints);
            polylineColor.setPoints(mPoints);
            for (LatLng p : mPoints) {
                map.addCircle(new CircleOptions()
                        .center(p)
                        .radius(circleRad)
                        .strokeColor(Color.BLACK)
                        .strokeWidth(circleStrokeWidth)
                        .fillColor(routeColor))
                        .setZIndex(circleZ);
            }

            if (currentPosition != null) {
                currentPosition.setPosition(mPoints.get(mPoints.size() - 1));
            } else {
                currentPosition = thisMap.addMarker(new MarkerOptions()
                        .position(mPoints.get(mPoints.size() - 1)));
            }


            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mPoints.get(mPoints.size() - 1), PropertyHolder.getCurrentMapZoom()));

        }
        this.thisMap = map;

    }


    @Override
    protected void onResume() {


        Context context = getApplicationContext();

        loadingData = true;
        new DataGrabberTask().execute(context);


        if (Util.trafficCop(this))
            finish();

        isServiceOn = PropertyHolder.isServiceOn();
        shareData = PropertyHolder.getShareData();

        setNotificationArea();

        // service button
        mServiceButton = (ToggleButton) findViewById(R.id.service_button);
        mServiceButton.setChecked(isServiceOn);
        mServiceButton.setOnClickListener(new ToggleButton.OnClickListener() {
            public void onClick(View view) {
                if (view.getId() != R.id.service_button)
                    return;
                boolean on = ((ToggleButton) view).isChecked();
                String schedule = on ? Util.MESSAGE_SCHEDULE
                        : Util.MESSAGE_UNSCHEDULE;
                // Log.e(TAG, schedule + on);

                Context this_context = view.getContext();
                // now schedule or unschedule
                Intent intent = new Intent(
                        getString(R.string.internal_message_id) + schedule);
                if (this_context != null)
                    this_context.sendBroadcast(intent);
                if (on) {
                    isServiceOn = true;
                    final long ptNow = PropertyHolder.ptStart();

                    ContentResolver ucr = getContentResolver();
                    ucr.insert(
                            Util.getUploadQueueUri(this_context),
                            UploadContentValues.createUpload(DataCodeBook.ON_OFF_PREFIX, Util.makeOnfJsonString(true, ptNow)));
                } else {
                    isServiceOn = false;
                    final long ptNow = PropertyHolder.ptStop();
                    // stop uploader
                    Intent stopUploaderIntent = new Intent(MapMyData.this,
                            FileUploader.class);
                    // Stop service if it is currently running
                    stopService(stopUploaderIntent);
                    ContentResolver ucr = getContentResolver();
                    ucr.insert(
                            Util.getUploadQueueUri(this_context),
                            UploadContentValues.createUpload(DataCodeBook.ON_OFF_PREFIX, Util.makeOnfJsonString(false, ptNow)));

                }

                setNotificationArea();

            }
        });


        IntentFilter fixFilter;
        fixFilter = new IntentFilter(getResources().getString(
                R.string.internal_message_id)
                + Util.MESSAGE_FIX_RECORDED);
        fixReceiver = new FixReceiver();
        registerReceiver(fixReceiver, fixFilter);

        super.onResume();

    }

    private void setNotificationArea() {
        if (isServiceOn) {
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (!manager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    receiverNotificationArea.setVisibility(View.VISIBLE);
                    mReceiversOffWarning.setText(getResources().getString(
                            R.string.noGPSnoNet));
                } else {
                    receiverNotificationArea.setVisibility(View.VISIBLE);

                    mReceiversOffWarning.setText(getResources().getString(
                            R.string.noGPS));
                }

                mReceiversOffWarning
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                            }
                        });
            } else {
                receiverNotificationArea.setVisibility(View.INVISIBLE);
            }
        } else {
            receiverNotificationArea.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onPause() {
        unregisterReceiver(fixReceiver);
        super.onPause();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();
        if (eventaction == MotionEvent.ACTION_UP) {
            long time = System.currentTimeMillis();
            if (startMillis == 0 || (time - startMillis > 3000)) {
                startMillis = time;
                count = 1;
            } else {
                count++;
            }
            if (count == 4) {
                buildExpertMessage();
            }
            return true;
        }
        return false;
    }


    private void buildExpertMessage() {
        final boolean expert = PropertyHolder.getExpertMode();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(expert ? getResources().getString(R.string.exit_expert) : getResources().getString(R.string.enter_expert)).setTitle(getResources().getString(R.string.expert_title))
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                PropertyHolder.setExpertMode(!expert);
                                // if expert was false at first, now we are in expert mode, so make sure alarm on if service on
                                if (!expert) {
                                    sendBroadcast(new Intent(getResources().getString(R.string.internal_message_id) + Util.MESSAGE_START_MESSAGE_C_TIMER));
                                    Util.toast(context, getResources().getString(R.string.expert_title));
                                } else {
                                    sendBroadcast(new Intent(getResources().getString(R.string.internal_message_id) + Util.MESSAGE_CANCEL_C_NOTIFICATION));
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                dialog.dismiss();
                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public class DataGrabberTask extends AsyncTask<Context, Integer, Boolean> {
        int myProgress;
        int nFixes;
        ArrayList<MapPoint> results = new ArrayList<MapPoint>();

        @Override
        protected void onPreExecute() {
            myProgress = 0;
            progressNotificationArea.setVisibility(View.VISIBLE);

        }

        protected Boolean doInBackground(Context... context) {
            results.clear();
            final String selectionString;
            selectionString = Fix.Fixes.KEY_ROWID + " > " + lastRecId;
            ContentResolver cr = getContentResolver();
            Cursor c = cr.query(Util.getFixesUri(context[0]),
                    Fix.Fixes.KEYS_LATLONACCTIMES, selectionString, null, null);
            if (!c.moveToFirst()) {
                c.close();
                return false;
            }
            int latCol = c.getColumnIndexOrThrow("latitude");
            int lonCol = c.getColumnIndexOrThrow("longitude");
            int accCol = c.getColumnIndexOrThrow("accuracy");
            int idCol = c.getColumnIndexOrThrow("_id");
            int timeCol = c.getColumnIndexOrThrow(Fix.Fixes.KEY_TIMELONG);
            int sdtimeCol = c
                    .getColumnIndexOrThrow(Fix.Fixes.KEY_STATION_DEPARTURE_TIMELONG);
            nFixes = c.getCount();
// float lastAcc = 0;
            int currentRecord = 0;
            while (!c.isAfterLast()) {
                myProgress = (int) (((currentRecord + 1) / (float) nFixes) * 100);
                publishProgress(myProgress);
// Escape early if cancel() is called
                if (isCancelled())
                    break;
                lastRecId = c.getInt(idCol);
// First grabbing double values of lat lon and time
                Double lat = c.getDouble(latCol);
                Double lon = c.getDouble(lonCol);
                float acc = c.getFloat(accCol);
                long entryTime = c.getLong(timeCol);
                long exitTime = c.getLong(sdtimeCol);
                results.add(new MapPoint(lat, lon,
                        acc, entryTime, exitTime, MapPoint.ICON_NORMAL));
                c.moveToNext();
                currentRecord++;
            }
            c.close();
            return true;
        }

        protected void onProgressUpdate(Integer... progress) {
            progressbar.setProgress(progress[0]);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                if (results != null && results.size() > 0) {
                    for (MapPoint p : results) {
                        mPoints.add(new LatLng(p.lat, p.lon));
                    }
                    if (polylineBlack != null && polylineColor != null) {
                        polylineBlack.setPoints(mPoints);
                        polylineColor.setPoints(mPoints);
                    }
                    if (thisMap != null) {
                        for (LatLng p : mPoints) {
                            thisMap.addCircle(new CircleOptions()
                                    .center(p)
                                    .radius(circleRad)
                                    .strokeWidth(circleStrokeWidth)
                                    .strokeColor(Color.BLACK)
                                    .fillColor(routeColor))
                                    .setZIndex(circleZ);
                        }
                        if (currentPosition != null) {
                            currentPosition.setPosition(mPoints.get(mPoints.size() - 1));
                        } else {
                            currentPosition = thisMap.addMarker(new MarkerOptions()
                                    .position(mPoints.get(mPoints.size() - 1)));
                        }

                        thisMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPoints.get(mPoints.size() - 1), PropertyHolder.getCurrentMapZoom()));

                    }

                }
            }
            progressNotificationArea.setVisibility(View.INVISIBLE);
            loadingData = false;
        }
    }


    public class FixReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(FixGet.NEW_RECORD, false)) {
                updateMap(intent);
            }
        }
    }


    public void updateMap(Intent intent) {
        double lat = intent.getDoubleExtra("lat", 0);
        double lon = intent.getDoubleExtra("lon", 0);
        if (lat != 0 && lon != 0)
            mPoints.add(new LatLng(lat, lon));
        if (polylineBlack != null && polylineColor != null) {
            polylineBlack.setPoints(mPoints);
            polylineColor.setPoints(mPoints);
        }
        if (thisMap != null) {
            for (LatLng p : mPoints) {
                thisMap.addCircle(new CircleOptions()
                        .center(p)
                        .radius(circleRad)
                        .strokeWidth(circleStrokeWidth)
                        .strokeColor(Color.BLACK)
                        .fillColor(routeColor))
                        .setZIndex(circleZ);

            }
            if (currentPosition != null) {
                currentPosition.setPosition(mPoints.get(mPoints.size() - 1));
            } else {
                currentPosition = thisMap.addMarker(new MarkerOptions()
                        .position(mPoints.get(mPoints.size() - 1)));
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem surveyItem = menu.findItem(R.id.launchSurvey);
        MenuItem expertItem = menu.findItem(R.id.toggleExpertMode);
        MenuItem normalItem = menu.findItem(R.id.toggleNormalMode);

        if (!PropertyHolder.getExpertMode()) {
            if (surveyItem != null) {
                surveyItem.setVisible(false);
            }
            if (normalItem != null) {
                normalItem.setVisible(false);
            }
            if (expertItem != null) {
                expertItem.setVisible(true);
            }

        } else {
            if (surveyItem != null) {
                surveyItem.setVisible(true);
            }
            if (normalItem != null) {
                normalItem.setVisible(true);
            }
            if (expertItem != null) {
                expertItem.setVisible(false);
            }

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.toggleExpertMode) {
            buildExpertMessage();
            return true;
        }
        else if (id == R.id.toggleNormalMode) {
            buildExpertMessage();
            return true;
        }
        else if (id == R.id.launchSurvey) {
            Intent intent = new Intent(MapMyData.this, TransportationModeSurvey.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}