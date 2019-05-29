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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import net.movelab.cmlibrary.Fix.Fixes;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Various static fields and methods used in the application, some taken from
 * Human Mobility Project.
 *
 * @author Chang Y. Chung
 * @author Necati E. Ozgencil
 * @author John R.B. Palmer
 */
public class Util {


    // general  controls
    public static boolean debriefing_surveys_on = false;
    public static boolean pro_versions_on = false;

    public final static int PRIVACY_ZONE_RADIUS = 500;

    public final static String MESSAGE_STOP_FIXGET = ".STOP_FIXGET";
    public final static String MESSAGE_LONGSTOP_FIXGET = ".LONGSTOP_FIXGET";
    public final static String MESSAGE_SCHEDULE = ".SCHEDULE_SERVICE";
    public final static String MESSAGE_UNSCHEDULE = ".UNSCHEDULE_SERVICE";
    public final static String MESSAGE_FIX_RECORDED = ".NEW_FIX_RECORDED";
    public final static String MESSAGE_FIX_UPLOADED = ".NEW_FIX_UPLOADED";


    public final static int TRACKING_NOTIFICATION = 0;
    public final static int PRO_CONVERSION_NOTIFICATION = 1;
    public final static int MESSAGE_A_NOTIFICATION = 2;
    public final static int MESSAGE_B_NOTIFICATION = 3;
    public final static int MESSAGE_C_NOTIFICATION = 4;



    public final static String MESSAGE_START_MESSAGE_AB_TIMER = ".message_start_message_ab_timer";
    public final static String MESSAGE_START_MESSAGE_C_TIMER = ".message_start_message_c_timer";
    public final static String MESSAGE_MAKE_MESSAGE_A_NOTIFICATION = ".message_make_a_notification";
    public final static String MESSAGE_MAKE_MESSAGE_B_NOTIFICATION = ".message_make_b_notification";
    public final static String MESSAGE_MAKE_MESSAGE_C_NOTIFICATION = ".message_make_c_notification";
    public final static String MESSAGE_CANCEL_A_NOTIFICATION = ".message_cancel_a_notification";
    public final static String MESSAGE_CANCEL_B_NOTIFICATION = ".message_cancel_b_notification";
    public final static String MESSAGE_CANCEL_C_NOTIFICATION = ".message_cancel_c_notification";

    public final static long SECONDS = 1000;
    public final static long MINUTES = SECONDS * 60;
    public final static long HOURS = MINUTES * 60;
    public final static long DAYS = HOURS * 24;
    public final static long WEEKS = DAYS * 7;


    public static long time_to_message_a = 1*DAYS;

    public static long time_to_message_b = 3 * DAYS;


    public final static long MESSAGE_C_INTERVAL = 4*HOURS;


    public static long UPLOAD_INTERVAL = 8 * HOURS;

    public static boolean PASSED_INTRO = false;

    // TODO note that I am setting this to 100 weeks just to make sure it is not triggered during the study period by accident
    public static long TIME_TO_PRO = 100 * WEEKS;

    public static int UPLOADS_TO_PRO = 1000;

    public static int MAX_FILE_STORAGE_NUMBER = 10000;

    // Min average comfortable walking speed (cm/s) from Bohannon 1997,
    // http://ageing.oxfordjournals.org/content/26/1/15.full.pdf+html
    public static int WALKING_SPEED = 127;

    // Use the distance one would cover at walking speed capped at 50
    public static int getMinDist() {
//        int fixIntervalSeconds = (int) ((int) PropertyHolder.getAlarmInterval() / (int) SECONDS);

 //       int expectedWalkingDistanceMeters = (int) (WALKING_SPEED * fixIntervalSeconds) / 100;
  //      return Math.min(MIN_DIST, expectedWalkingDistanceMeters);
        return MIN_DIST;
    }

    public static int MIN_DIST = 50;

    public static boolean needDatabaseUpdate = false;

    public static int EXTRARUNS = 4;

    public static boolean flushGPSFlag = false;

    public static boolean redrawMap = false;

    public static long xTime = 1 * 60 * 60 * 1000;
    /**
     * Default value for the interval between location fixes. In milliseconds.
     */
    public static final long ALARM_INTERVAL = 2 * 60000; // 2 minutes

    /**
     * Server URL for uploads.
     */
    public static final String SERVER = UtilLocal.URL_DATA_POINTS;

    /**
     * Extension to append to all files saved for uploading.
     */
    public static final String EXTENSION = ".uploadque";

    /**
     * Maximum length of time to run location listeners during each fix attempt.
     * In milliseconds.
     */
    public static final long LISTENER_WINDOW = 5 * 1000;

    /**
     * Value at which a GPS location will be preferred to a network location,
     * even if the network location is listed with a higher accuracy.
     */
    public static final float MIN_GPS_ACCURACY = 50;

    /**
     * Value at which a location will be used, and both listeners stopped even
     * if not yet at the end of the listener window.
     */
    public static final float OPT_ACCURACY = 15;

    /**
     * Value at which a location will be used, and both listeners stopped even
     * if not yet at the end of the listener window - for long runs.
     */
    public static final float OPT_ACCURACY_LONGRUNS = 50;

    /**
     * Minimum accuracy necessary for location to be used.
     */
    public static final float MIN_ACCURACY = 500;

    /**
     * Default time for storing user data when user selects to do so. In days.
     */
    public static final int STORAGE_DAYS = 7;


    /**
     * Dummy variable indicating whether application is currently taking fix.
     */
    public static boolean locatingNow = false;

    /**
     * Default value for figuring out when alarm manager started counting. For
     * use with the display timer in the CountdownDisplay activity.
     */
    public static long countingFrom = 0;

    public static long lastFixStartedAt = 0;

    /**
     * counter for how many fixes have been missed in a row.
     */
    public static int missedFixes = 0;

    /**
     * temp holder for info on latest fix.
     */
    public static String lastFixTimeStamp = null;

    /**
     * temp holder for info on latest fix.
     */
    public static long lastFixTime = 0;

    /**
     * temp holder for info on latest fix.
     */
    public static double lastFixLat = 0;

    /**
     * temp holder for info on latest fix.
     */
    public static double lastFixLon = 0;

    /**
     * holder for current value of the listener window
     */
    public static long listenerTimer = LISTENER_WINDOW;

    /**
     * Surrounds the given string in quotation marks. Taken from Human Mobility
     * Project code written by Chang Y. Chung and Necati E. Ozgencil.
     *
     * @param str The string to be encased in quotation marks.
     * @return The given string trimmed and encased in quotation marks.
     */
    public static String enquote(String str) {
        final String dq = "\"";
        final String ddq = dq + dq;
        StringBuilder sb = new StringBuilder("");
        sb.append(dq);
        sb.append((str.trim()).replace(dq, ddq));
        sb.append(dq);
        return sb.toString();
    }

    /**
     * Formats the given coordinate and converts to String form. Taken from
     * Human Mobility Project code written by Chang Y. Chung and Necati E.
     * Ozgencil.
     *
     * @param coord The coordinate value to be formatted.
     * @return The properly formatted coordinate in String form
     */
    public static String fmtCoord(double coord) {
        return String.format("%1$11.6f", coord);
    }

    /**
     * Formats the given time and converts to String form. Taken from Human
     * Mobility Project code written by Chang Y. Chung and Necati E. Ozgencil.
     *
     * @param time The time value to be formatted.
     * @return The properly formatted time value in String form
     */
    public static String iso8601(long time) {
        return String.format("%1$tFT%1$tT", time);
    }

    /**
     * Formats the given time and converts to String form. Taken from Human
     * Mobility Project code written by Chang Y. Chung and Necati E. Ozgencil.
     *
     * @param datetime The Date object, whose long time value must be formatted.
     * @return The properly formatted time value of the Date Object in String
     * form
     */
    public static String iso8601(Date datetime) {
        return iso8601(datetime.getTime());
    }

    /**
     * Formats a date object for displaying it to the user.
     *
     * @param date The Date object to be formatted.
     * @return The properly formatted time and date as a String.
     */
    public static String userDate(Date date) {
        SimpleDateFormat s = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String format = s.format(date);
        return format;
    }

    public static String userDateNoTime(Date date) {
        SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy");
        String format = s.format(date);
        return format;
    }

    public static String userDateY2D(Date date) {
        SimpleDateFormat s = new SimpleDateFormat("dd/MM/yy");
        String format = s.format(date);
        return format;
    }

    /**
     * Formats the location time, given as a long in milliseconds, for use in
     * filenames.
     *
     * @param locationTime The long value to be formatted.
     * @return The properly formatted time and date as a String.
     */
    public static String fileNameDate(long locationTime) {
        Date date = new Date(locationTime);
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String format = s.format(date);
        return format;
    }

    /**
     * Gets the current system time in milliseconds. Taken from Human Mobility
     * Project code written by Chang Y. Chung and Necati E. Ozgencil.
     *
     * @return The current system time in milliseconds.
     */
    public static String now() {
        return iso8601(System.currentTimeMillis());
    }

    /**
     * Displays a brief message on the phone screen. Taken from Human Mobility
     * Project code written by Chang Y. Chung and Necati E. Ozgencil.
     *
     * @param context Interface to application environment
     * @param msg     The message to be displayed to the user
     */
    public static void toast(Context context, String msg) {
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        // t.setDuration(5*Toast.LENGTH_LONG);
        t.show();
    }

    /**
     * Encrypt a byte array using RSA. Relies on the public key file stored in
     * the raw folder (which is not included in the public source code).
     *
     * @param context The application context.
     * @param in      The byte array to be encrypted.
     * @return An encrypted byte array.
     */
    public static byte[] encryptRSA(Context context, byte[] in) {
        // String TAG = "Util.encryptRSA";
        BufferedInputStream is = null;
        ByteArrayOutputStream bos = null;
        byte[] pk;
        byte[] result = null;

        try {
            is = new BufferedInputStream(context.getResources()
                    .openRawResource(R.raw.pubkey));
            bos = new ByteArrayOutputStream();
            while (is.available() > 0) {
                bos.write(is.read());
            }

            pk = bos.toByteArray();

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pk);

            KeyFactory kf;
            kf = KeyFactory.getInstance("RSA");
            PublicKey pkPublic;

            pkPublic = kf.generatePublic(publicKeySpec);

            // Encrypt
            Cipher pkCipher;

            pkCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");

            pkCipher.init(Cipher.ENCRYPT_MODE, pkPublic);

            result = pkCipher.doFinal(in);
        } catch (IllegalBlockSizeException e) {
            // logging exception, and simply letting the result return as null.
            // Log.e(TAG, "Exception " + e);
        } catch (BadPaddingException e) {
            // logging exception, and simply letting the result return as null.
            // Log.e(TAG, "Exception " + e);
        } catch (InvalidKeyException e) {
            // logging exception, and simply letting the result return as null.
            // Log.e(TAG, "Exception " + e);
        } catch (NoSuchAlgorithmException e) {
            // logging exception, and simply letting the result return as null.
            // Log.e(TAG, "Exception " + e);
        } catch (NoSuchPaddingException e) {
            // logging exception, and simply letting the result return as null.
            // Log.e(TAG, "Exception " + e);
        } catch (InvalidKeySpecException e) {
            // logging exception, and simply letting the result return as null.
            // Log.e(TAG, "Exception " + e);
        } catch (IOException e) {
            // logging exception, and simply letting the result return as null.
            // Log.e(TAG, "Exception " + e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    // logging exception
                    // Log.e(TAG, "Exception " + e);
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // logging exception
                    // Log.e(TAG, "Exception " + e);
                }
            }
        }
        return result;
    }

    /**
     * Checks if the phone has an internet connection.
     *
     * @param context The application context.
     * @return True if phone has a connection; false if not.
     */
    public static boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static String csvFixes(Cursor c) {

        int accuracy = c.getColumnIndexOrThrow(Fixes.KEY_ACCURACY);
        int altitude = c.getColumnIndexOrThrow(Fixes.KEY_ALTITUDE);
        int latitude = c.getColumnIndexOrThrow(Fixes.KEY_LATITUDE);
        int longitude = c.getColumnIndexOrThrow(Fixes.KEY_LONGITUDE);
        int provider = c.getColumnIndexOrThrow(Fixes.KEY_PROVIDER);
        int timelong = c.getColumnIndexOrThrow(Fixes.KEY_TIMELONG);
        StringBuilder sb = new StringBuilder("");
        sb.append("accuracy").append(",");
        sb.append("altitude").append(",");
        sb.append("latitude").append(",");
        sb.append("longitude").append(",");
        sb.append("provider").append(",");
        sb.append("time");

        c.moveToFirst();
        while (!c.isAfterLast()) {
            sb.append("\n");
            sb.append(doubleFieldVal(c, accuracy)).append(",");
            sb.append(doubleFieldVal(c, altitude)).append(",");
            sb.append(doubleFieldVal(c, latitude)).append(",");
            sb.append(doubleFieldVal(c, longitude)).append(",");
            sb.append(Util.enquote(c.getString(provider))).append(",");
            sb.append(Util.enquote(Util.userDate(new Date(c.getLong(timelong)))
                    .trim()));
            c.moveToNext();
        }
        return sb.toString();
    }

    /**
     * Converts the supposedly double value contained in the row, at which the
     * given Cursor is pointing, and the col(umn) specified to its String
     * representation.
     *
     * @return The String representation of the value contained in the cell
     * [c.getPosition(), col]
     */
    public static String doubleFieldVal(Cursor c, int col) {
        Double val = (Double) c.getDouble(col);
        return (val == null) ? "" : val.toString();
    }

    /**
     * Locates the index of the first element greater than or equal to a given
     * value from an array of integers sorted in ascending order.
     *
     * @param A    The array to search in. Must be sorted in ascending order
     * @param key  The value to be searched for.
     * @param imin The minimum index of the array to search in.
     * @param imax The maximum index of the array to search in.
     * @return -1 if imax is less than imin or if the key is above imax.
     * Otherwise the index of the first element that is greater than or
     * equal to the key.
     * @author = John R.B. Palmer
     */
    public static int minElementGreaterThanOrEqualToKey(int A[], int key,
                                                        int imin, int imax) {

        // Return -1 if the maximum value is less than the minimum or if the key
        // is great than the maximum
        if (imax < imin || key > A[imax])
            return -1;

        // Return the first element of the array if that element is greater than
        // or equal to the key.
        if (key < A[imin])
            return imin;

        // When the minimum and maximum values become equal, we have located the
        // element.
        if (imax == imin)
            return imax;

        else {
            // calculate midpoint to cut set in half, avoiding integer overflow
            int imid = imin + ((imax - imin) / 2);

            // if key is in upper subset, then recursively search in that subset
            if (A[imid] < key)
                return minElementGreaterThanOrEqualToKey(A, key, imid + 1, imax);

                // if key is in lower subset, then recursively search in that subset
            else
                return minElementGreaterThanOrEqualToKey(A, key, imin, imid);
        }
    }

    /**
     * Locates the index of the first element greater than or equal to a given
     * value from an array of integers sorted in ascending order.
     *
     * @param A    The array to search in. Must be sorted in ascending order
     * @param key  The value to be searched for.
     * @param imin The minimum index of the array to search in.
     * @param imax The maximum index of the array to search in.
     * @return -1 if imax is less than imin or if the key is above imax.
     * Otherwise the index of the first element that is greater than or
     * equal to the key.
     * @author = John R.B. Palmer
     */
    public static int minElementGreaterThanOrEqualToKey(long A[], long key,
                                                        int imin, int imax) {

        // Return -1 if the maximum value is less than the minimum or if the key
        // is great than the maximum
        if (imax < imin || key > A[imax])
            return -1;

        // Return the first element of the array if that element is greater than
        // or equal to the key.
        if (key < A[imin])
            return imin;

        // When the minimum and maximum values become equal, we have located the
        // element.
        if (imax == imin)
            return imax;

        else {
            // calculate midpoint to cut set in half, avoiding integer overflow
            int imid = imin + ((imax - imin) / 2);

            // if key is in upper subset, then recursively search in that subset
            if (A[imid] < key)
                return minElementGreaterThanOrEqualToKey(A, key, imid + 1, imax);

                // if key is in lower subset, then recursively search in that subset
            else
                return minElementGreaterThanOrEqualToKey(A, key, imin, imid);
        }
    }

    /**
     * Locates the index of the last element less than or equal to a given value
     * from an array sorted in ascending order.
     *
     * @param A    The array to search in. Must be sorted in ascending order
     * @param key  The value to be searched for.
     * @param imin The minimum index of the array to search in.
     * @param imax The maximum index of the array to search in.
     * @return -1 if imax is less than imin or if the key is below imin.
     * Otherwise the index of the last element that is less than or
     * equal to the key.
     * @author = John R.B. Palmer
     */
    public static int maxElementLessThanOrEqualToKey(int A[], int key,
                                                     int imin, int imax) {

        if (imax < imin || key < A[imin])
            return -1;
        if (key > A[imax])
            return imax;
        if (imax == imin) {
            return imax;
        } else {
            // calculate midpoint to cut set in half
            int imid = imax - ((imax - imin) / 2);

            if (A[imid] > key)
                // key is in lower subset
                return maxElementLessThanOrEqualToKey(A, key, imin, imid - 1);
            else
                // key is in upper subset
                return maxElementLessThanOrEqualToKey(A, key, imid, imax);

        }
    }

    /**
     * Locates the index of the last element less than or equal to a given value
     * from an array sorted in ascending order.
     *
     * @param A    The array to search in. Must be sorted in ascending order
     * @param key  The value to be searched for.
     * @param imin The minimum index of the array to search in.
     * @param imax The maximum index of the array to search in.
     * @return -1 if imax is less than imin or if the key is below imin.
     * Otherwise the index of the last element that is less than or
     * equal to the key.
     * @author = John R.B. Palmer
     */
    public static int maxElementLessThanOrEqualToKey(long A[], long key,
                                                     int imin, int imax) {

        if (imax < imin || key < A[imin])
            return -1;
        if (key > A[imax])
            return imax;
        if (imax == imin) {
            return imax;
        } else {
            // calculate midpoint to cut set in half
            int imid = imax - ((imax - imin) / 2);

            if (A[imid] > key)
                // key is in lower subset
                return maxElementLessThanOrEqualToKey(A, key, imin, imid - 1);
            else
                // key is in upper subset
                return maxElementLessThanOrEqualToKey(A, key, imid, imax);

        }
    }

    public static File[] listFiles(Context context) {
        File directory = new File(context.getFilesDir().getAbsolutePath());
        File[] files = directory.listFiles();
        return files;
    }

    public static String getAvailableMB() {
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        final File path = Environment.getDataDirectory();
        long availableSpace = -1L;
        StatFs stat = new StatFs(path.getPath());
        availableSpace = (long) stat.getAvailableBlocks()
                * (long) stat.getBlockSize();
        return String.valueOf(availableSpace / SIZE_MB + " MB");

    }

    public static String getUserDbMB(Context context) {
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        if (context.getDatabasePath(SpaceMapperContentProvider.DATABASE_NAME) != null) {
            final long totalSize = context.getDatabasePath(
                    SpaceMapperContentProvider.DATABASE_NAME).length();
            if (totalSize > SIZE_MB)
                return String.valueOf(totalSize / SIZE_MB) + " MB";
            if (totalSize < SIZE_MB && totalSize > SIZE_KB)
                return String.valueOf(totalSize / SIZE_KB) + " KB";
            else
                return String.valueOf(totalSize) + " bytes";
        } else
            return "0 bytes";

    }

    public static String getPendingUploadsMB(Context context) {
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        if (context.getDatabasePath(UploadQueueContentProvider.DATABASE_NAME) != null) {
            final long totalSize = context.getDatabasePath(
                    UploadQueueContentProvider.DATABASE_NAME).length();
            if (totalSize > SIZE_MB)
                return String.valueOf(totalSize / SIZE_MB) + " MB";
            if (totalSize < SIZE_MB && totalSize > SIZE_KB)
                return String.valueOf(totalSize / SIZE_KB) + " KB";
            else
                return String.valueOf(totalSize) + " bytes";
        } else
            return "0 bytes";

    }

    public static String formatLongTime(Long time) {
        final long hours = time / HOURS;
        final long minutes = (time % HOURS) / MINUTES;
        final long seconds = (time % MINUTES) / SECONDS;
        return String.valueOf(hours) + ":" + String.valueOf(minutes) + ":"
                + String.valueOf(seconds);
    }

    public static String createFileName(String prefix) {
        return prefix + PropertyHolder.getUserId() + "_"
                + fileNameDate(System.currentTimeMillis()) + EXTENSION;
    }

    public static String TAG = "UPLOADER";


    /**
     * Uploads JSONObject to Tigaserver API using HTTP PUT request
     */
    public static HttpResponse postJSON(JSONObject jsonData,
                                        String apiEndpoint, Context context) {
        HttpResponse result = null;
        if (!isOnline(context)) {
            return null;
        } else {
            result = postJsonString(jsonData.toString(), apiEndpoint, context);
            return result;
        }
    }


    public static HttpResponse postJsonString(String jsonString, String targetUrl, Context context) {
        HttpResponse result = null;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);
            int timeoutSocket = 3000;
            HttpConnectionParams
                    .setSoTimeout(httpParameters, timeoutSocket);

            DefaultHttpClient httpclient = new DefaultHttpClient(
                    httpParameters);
            HttpPost httpost = new HttpPost(targetUrl);
            StringEntity se = new StringEntity(jsonString, "UTF-8");
            httpost.setEntity(se);
            httpost.setHeader("Accept", "application/json");
            httpost.setHeader("Content-type", "application/json");
            httpost.setHeader("Authorization", "Token " + PropertyHolder.getUserKey());

            result = httpclient.execute(httpost);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "error: " + e);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "error: " + e);
        } catch (IOException e) {
            Log.e(TAG, "error: " + e);
        }

        return result;
    }


    public static boolean uploadEncryptedString(Context context,
                                                String filePrefix, String stringToUpload, String uploadurl) {

        if (!isOnline(context))
            return false;

        byte[] bytes;
        String encoded_string;
        JSONObject json_to_post = new JSONObject();

        try {
            bytes = encryptRSA(context, stringToUpload.getBytes("UTF-8"));
            encoded_string = Base64.encodeToString(bytes, Base64.DEFAULT);

            json_to_post.put(DataCodeBook.API_JSON_KEY_USER_UUID, PropertyHolder.getUserId());
            json_to_post.put(DataCodeBook.API_JSON_KEY_USER_CODE, PropertyHolder.getUserCode());
            json_to_post.put(DataCodeBook.API_JSON_KEY_TYPE, filePrefix);
            json_to_post.put(DataCodeBook.API_JSON_KEY_APP_VERSION, getAppVersion(context));
            json_to_post.put(DataCodeBook.API_JSON_KEY_ENCRYPTED_MESSAGE, encoded_string);


        } catch (UnsupportedEncodingException e) {

            return false;
        } catch (JSONException e) {

            return false;
        }

        if (json_to_post.length() > 0) {
            HttpResponse this_response = postJSON(json_to_post, uploadurl, context);
            int this_status_code = getResponseStatusCode(this_response);
            if (this_status_code >= 200 && this_status_code < 300) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }


    public static int getResponseStatusCode(HttpResponse httpResponse) {

        int statusCode = 0;
        if (httpResponse != null) {
            StatusLine status = httpResponse.getStatusLine();
            statusCode = status.getStatusCode();
        }
        return statusCode;
    }


    public static boolean trafficCop(Context context) {

        if (PropertyHolder.getTryingToWithdraw()) {
            Intent intent = new Intent(context, Withdraw.class);
            context.startActivity(intent);
            return true;
        }

        if (PropertyHolder.isWithdrawn() && !PropertyHolder.getProVersion()) {
            Intent intent = new Intent(context, WithdrawLock.class);
            context.startActivity(intent);
            return true;
        }

        if ((!PropertyHolder.hasConsented() || !PropertyHolder.isRegistered())
                && PropertyHolder.getShareData()) {
            Intent intent = new Intent(context, Intro.class);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static void createProNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(
                R.drawable.notification_proconversion,
                context.getResources().getString(
                        R.string.proconversion_notification_initial),
                System.currentTimeMillis());

        Intent intent = new Intent(context, Settings.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(
                context,
                context.getResources().getString(
                        R.string.proconversion_notification_subject),
                context.getResources().getString(
                        R.string.proconversion_notification), pendingIntent);
        notificationManager.notify(Util.PRO_CONVERSION_NOTIFICATION,
                notification);

    }

    public static Uri getUploadQueueUri(Context context) {

        return Uri.parse("content://"
                + context.getResources().getString(
                R.string.content_provider_authority_uploadqueue) + "/"
                + UploadQueueContentProvider.DATABASE_TABLE);

    }

    public static Uri getFixesUri(Context context) {

        return Uri.parse("content://"
                + context.getResources().getString(
                R.string.content_provider_authority_fixes) + "/"
                + SpaceMapperContentProvider.DATABASE_TABLE);

    }


    public static String makeOnfJsonString(boolean on, long usage_time) {
        String result = "";
        JSONObject onf_json_data = new JSONObject();
        try {
            onf_json_data.put(DataCodeBook.ON_OFF_KEY_ON_OR_OFF, "on");
            onf_json_data.put(DataCodeBook.ON_OFF_KEY_TIME, Util.iso8601(System.currentTimeMillis()));
            onf_json_data.put(DataCodeBook.ON_OFF_KEY_USAGE_TIME, usage_time);
            result = onf_json_data.toString();
        } catch (JSONException e) {
            //todo
        }
        return result;

    }

    public static String getAppVersion(Context context){
        String version = "";
        PackageInfo pInfo = null;
        try {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                pInfo = pm.getPackageInfo(
                        context.getPackageName(), 0);
            }
        } catch (PackageManager.NameNotFoundException e) {
            //todo
        }

        if (pInfo != null) {
            version = pInfo.versionName;
        }
        return version;
    }
}
