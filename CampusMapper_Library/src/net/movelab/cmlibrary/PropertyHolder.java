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
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;

/**
 * Manipulates the application's shared preferences, values that must persist
 * throughout the application's installed lifetime. These shared preferences are
 * essential for determining states, such as whether the user has completed the
 * consent and registration stage.
 * <p/>
 * Dependencies: CountdownDisplay.java, SpaceMapperBroadcastReceiver.java,
 * Consent.java, Data.java, FileUploader.java, FixGet.java, Help.java,
 * Registration.java, ReviewConsent.java, Settings.java, SplashScree.java,
 * Util.java, Withdraw.java, Withdrawlock.java
 *
 * @author Necati E. Ozgencil
 * @author John R.B. Palmer
 */
public class PropertyHolder {
    private static SharedPreferences sharedPreferences;
    private static Editor editor;

    /**
     * Initialize the shared preferences handle.
     *
     * @param context Interface to application environment
     */
    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("PROPERTIES",
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static boolean isInit() {
        return sharedPreferences != null;
    }

    public static void deleteAll() {
        editor.clear();
        editor.commit();
    }

    public static final String NEEDS_DEBRIEFING_SURVEY = "NEEDS_DEBRIEFING_SURVEY";

    public static void setNeedsDebriefingSurvey(boolean surveyneeded) {
        editor.putBoolean(NEEDS_DEBRIEFING_SURVEY, surveyneeded);
        editor.commit();
    }

    public static boolean getNeedsDebriefingSurvey() {
        return Util.debriefing_surveys_on && sharedPreferences.getBoolean(NEEDS_DEBRIEFING_SURVEY, false);
    }

    public static final String TRYING_TO_WITHDRAW = "TRYING_TO_WITHDRAW";

    public static void setTryingToWithdraw(boolean trying) {
        editor.putBoolean(TRYING_TO_WITHDRAW, trying);
        editor.commit();
    }

    public static boolean getTryingToWithdraw() {
        return sharedPreferences.getBoolean(TRYING_TO_WITHDRAW, false);
    }


    public static final String CURRENT_ZOOM = "current_zoom";

    public static void setCurrentMapZoom(int zoom) {
        editor.putInt(CURRENT_ZOOM, zoom);
        editor.commit();
    }

    public static int getCurrentMapZoom() {
        return sharedPreferences.getInt(CURRENT_ZOOM, 18);
    }


    public static void setUploadOldFiles(boolean uploadOld) {
        editor.putBoolean("UPLOAD_OLD_FILES", uploadOld);
        editor.commit();
    }

    public static boolean getUploadOldFiles() {
        return sharedPreferences.getBoolean("UPLOAD_OLD_FILES", true);
    }

    public static void setProVersion(boolean pro) {
        editor.putBoolean("PRO", pro);
        editor.commit();
    }

    public static boolean getProVersion() {
        return Util.pro_versions_on && sharedPreferences.getBoolean("PRO", false);
    }


    public static void setExpertMode(boolean expert) {
        editor.putBoolean("EXPERT", expert);
        editor.commit();
    }

    public static boolean getExpertMode() {
        return sharedPreferences.getBoolean("EXPERT", false);
    }


    final static String START_PT = "START_PARTICIPATION_TIME";
    final static String TOTAL_PT = "TOTAL_PARTICIPATION_TIME";
    final static String PT_RUNNING = "PT_RUNNING";

    public static long ptStart() {
        final long now = SystemClock.elapsedRealtime();
        final long st = sharedPreferences.getLong(START_PT, now);
        final long diff = now - st;
        final long storedTT = sharedPreferences.getLong(TOTAL_PT, 0);
        // If clock is now running now, then put current time as start time and
        // set clock to running
        if (!sharedPreferences.getBoolean(PT_RUNNING, false)) {
            editor.putLong(START_PT, now);
            editor.putBoolean(PT_RUNNING, true);
            editor.commit();
            // Return the stored total time
            return storedTT;
        } else if (diff > 0)
            // If the clock was already running, then return the stored total
            // time plus whatever has elapsed since last start
            return storedTT + (now - st);
        else
            return storedTT;
    }

    public static long ptStop() {
        final long now = SystemClock.elapsedRealtime();
        final long st = sharedPreferences.getLong(START_PT, now);
        final long diff = now - st;
        final long storedTT = sharedPreferences.getLong(TOTAL_PT, 0);
        if (sharedPreferences.getBoolean(PT_RUNNING, false)) {

            if (diff > 0) {
                final long tt = storedTT + (now - st);
                editor.putLong(TOTAL_PT, tt);
                editor.putBoolean(PT_RUNNING, false);
                editor.commit();
                return tt;
            } else {
                editor.putBoolean(PT_RUNNING, false);
                editor.commit();
            }
        }
        return storedTT;
    }

    public static long ptCheck() {
        final long now = SystemClock.elapsedRealtime();
        final long st = sharedPreferences.getLong(START_PT, -1);
        final long diff = now - st;
        final long storedTT = sharedPreferences.getLong(TOTAL_PT, 0);
        if (sharedPreferences.getBoolean(PT_RUNNING, false) && st != -1
                && (diff > 0)) {
            return storedTT + (now - st);
        } else {
            return storedTT;
        }
    }

    public static final String INITIAL_START_DATE_SET = "INITIAL_START_DATE_SET";

    public static boolean getInitialStartDateSet() {

        return sharedPreferences.getBoolean(INITIAL_START_DATE_SET, false);
    }

    public static void setInitialStartDateSet(boolean isSet) {
        editor.putBoolean(INITIAL_START_DATE_SET, isSet);
        editor.commit();
    }

    public static void setMapStartDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        editor.putInt("MAP_START_YEAR", c.get(Calendar.YEAR));
        editor.putInt("MAP_START_MONTH", c.get(Calendar.MONTH));
        editor.putInt("MAP_START_DAY", c.get(Calendar.DAY_OF_MONTH));
        editor.commit();
    }

    public static void setMapStartDate(Calendar c) {
        editor.putInt("MAP_START_YEAR", c.get(Calendar.YEAR));
        editor.putInt("MAP_START_MONTH", c.get(Calendar.MONTH));
        editor.putInt("MAP_START_DAY", c.get(Calendar.DAY_OF_MONTH));
        editor.commit();
    }

    public static void setMapStartDate(int year, int month, int day) {
        editor.putInt("MAP_START_YEAR", year);
        editor.putInt("MAP_START_MONTH", month);
        editor.putInt("MAP_START_DAY", day);
        editor.commit();
    }

    public static Date getMapStartDate() {
        int year = sharedPreferences.getInt("MAP_START_YEAR", -1);
        int month = sharedPreferences.getInt("MAP_START_MONTH", -1);
        int day = sharedPreferences.getInt("MAP_START_DAY", -1);

        Calendar c = Calendar.getInstance();

        if (year == -1 || month == -1 || day == -1) {
            int currentDay = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.DAY_OF_MONTH, currentDay - getStorageDays());
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);

        } else {
            c.set(year, month, day, 0, 0);
        }
        return c.getTime();
    }

    public static Calendar getMapStartCalendar() {
        int year = sharedPreferences.getInt("MAP_START_YEAR", -1);
        int month = sharedPreferences.getInt("MAP_START_MONTH", -1);
        int day = sharedPreferences.getInt("MAP_START_DAY", -1);

        Calendar c = Calendar.getInstance();

        if (year == -1 || month == -1 || day == -1) {
            int currentDay = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.DAY_OF_MONTH, currentDay - getStorageDays());
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);

        } else {
            c.set(year, month, day, 0, 0);
        }
        return c;
    }

    public static void setMapStartDay(int day) {
        editor.putInt("MAP_START_DAY", day);
        editor.commit();
    }

    public static void setMapStartMonth(int month) {
        editor.putInt("MAP_START_MONTH", month);
        editor.commit();
    }

    public static void setMapStartYear(int year) {
        editor.putInt("MAP_START_YEAR", year);
        editor.commit();
    }

    public static void setMapEndDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        editor.putInt("MAP_END_YEAR", c.get(Calendar.YEAR));
        editor.putInt("MAP_END_MONTH", c.get(Calendar.MONTH));
        editor.putInt("MAP_END_DAY", c.get(Calendar.DAY_OF_MONTH));
        editor.commit();
    }

    public static void setMapEndDate(Calendar c) {
        editor.putInt("MAP_END_YEAR", c.get(Calendar.YEAR));
        editor.putInt("MAP_END_MONTH", c.get(Calendar.MONTH));
        editor.putInt("MAP_END_DAY", c.get(Calendar.DAY_OF_MONTH));
        editor.commit();
    }

    public static void setMapEndDate(int year, int month, int day) {
        editor.putInt("MAP_END_YEAR", year);
        editor.putInt("MAP_END_MONTH", month);
        editor.putInt("MAP_END_DAY", day);
        editor.commit();
    }

    public static Date getMapEndDate() {
        int year = sharedPreferences.getInt("MAP_END_YEAR", -1);
        int month = sharedPreferences.getInt("MAP_END_MONTH", -1);
        int day = sharedPreferences.getInt("MAP_END_DAY", -1);

        Calendar c = Calendar.getInstance();

        if (year == -1 || month == -1 || day == -1) {
            int currentDay = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.DAY_OF_MONTH, currentDay);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);

        } else {
            c.set(year, month, day, 23, 59);
        }
        return c.getTime();
    }

    public static Calendar getMapEndCalendar() {
        int year = sharedPreferences.getInt("MAP_END_YEAR", -1);
        int month = sharedPreferences.getInt("MAP_END_MONTH", -1);
        int day = sharedPreferences.getInt("MAP_END_DAY", -1);

        Calendar c = Calendar.getInstance();

        if (year == -1 || month == -1 || day == -1) {
            int currentDay = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.DAY_OF_MONTH, currentDay);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);

        } else {
            c.set(year, month, day, 23, 59);
        }
        return c;
    }

    public static void setMapEndDay(int day) {
        editor.putInt("MAP_END_DAY", day);
        editor.commit();
    }

    public static void setMapEndMonth(int month) {
        editor.putInt("MAP_END_MONTH", month);
        editor.commit();
    }

    public static void setMapEndYear(int year) {
        editor.putInt("MAP_END_YEAR", year);
        editor.commit();
    }

    public static void setMapSat(boolean sat) {
        editor.putBoolean("MAP_SAT", sat);
        editor.commit();
    }

    public static boolean getMapSat() {
        return sharedPreferences.getBoolean("MAP_SAT", false);
    }

    public static void setMapIcons(boolean icons) {
        editor.putBoolean("MAP_ICONS", icons);
        editor.commit();
    }

    public static boolean getMapIcons() {
        return sharedPreferences.getBoolean("MAP_ICONS", true);
    }

    public static void setLimitStartDate(boolean sd) {
        editor.putBoolean("LIMIT_START_DATE", sd);
        editor.commit();
    }

    public static boolean getLimitStartDate() {
        return sharedPreferences.getBoolean("LIMIT_START_DATE", true);
    }

    public static void setLimitEndDate(boolean sd) {
        editor.putBoolean("LIMIT_END_DATE", sd);
        editor.commit();
    }

    public static boolean getLimitEndDate() {
        return sharedPreferences.getBoolean("LIMIT_END_DATE", false);
    }

    public static void setMapAcc(boolean acc) {
        editor.putBoolean("MAP_ACC", acc);
        editor.commit();
    }

    public static boolean getMapAcc() {
        return sharedPreferences.getBoolean("MAP_ACC", true);
    }

    public static void setRefLocation(int locId) {
        editor.putInt("REF_LOCATION", locId);
        editor.commit();
    }

    public static int getRefLocation() {
        return sharedPreferences.getInt("REF_LOCATION", -1);
    }

    public static void setNeedDatabaseValueUpdate(boolean needUpdate) {
        editor.putBoolean("NEEDDATABASEVALUEUPDATE", needUpdate);
        editor.commit();
    }

    public static boolean needDatabaseValueUpdate() {
        return sharedPreferences.getBoolean("NEEDDATABASEVALUEUPDATE", false);
    }

    public static void setWithdrawn(boolean withdrawn) {
        editor.putBoolean("WITHDRAWN", withdrawn);
        editor.commit();
    }

    public static boolean isWithdrawn() {
        return sharedPreferences.getBoolean("WITHDRAWN", false);
    }

    public static void setIntro(boolean intro) {
        editor.putBoolean("INTRO", intro);
        editor.commit();
    }

    public static boolean getIntro() {
        return sharedPreferences.getBoolean("INTRO", true);
    }

    public static void setAlarmInterval(long alarmInterval) {
        editor.putLong("ALARM_INTERVAL", alarmInterval);
        editor.commit();
    }

    public static long getAlarmInterval() {
        long interval = sharedPreferences.getLong("ALARM_INTERVAL", -1);
        if (interval == -1) {
            interval = Util.ALARM_INTERVAL;
            PropertyHolder.setAlarmInterval(interval);
        }
        return interval;
    }

    public static void setStoreMyData(boolean store) {
        editor.putBoolean("STORE_MY_DATA", store);
        editor.commit();
    }

    public static boolean getStoreMyData() {
        boolean store = sharedPreferences.getBoolean("STORE_MY_DATA", true);
        return store;
    }

    public static void setShareData(boolean share) {
        editor.putBoolean("SHARE_DATA", share);
        editor.commit();
    }

    public static boolean getShareData() {

        boolean share = sharedPreferences.getBoolean("SHARE_DATA", true);

        return share;
    }

    public static void setStorageDays(int days) {
        editor.putInt("STORAGE_DAYS", days);
        editor.commit();
    }

    public static int getStorageDays() {
        int days = sharedPreferences.getInt("STORAGE_DAYS", Util.STORAGE_DAYS);
        return days;
    }

    /**
     * Checks if alarm service is scheduled to run the FixGet service/if the
     * FixGet service is currently running. Returns a default value of
     * <code>false</code> if the SERVICE_ON flag has not been explicitly set
     * previously.
     *
     * @return <code>true</code> if the FixGet service is scheduled and running,
     * <code>false</code> if the FixGet service is currently stopped.
     */
    public static boolean isServiceOn() {
        return sharedPreferences.getBoolean("SERVICE_ON", getProVersion());
    }

    /**
     * Sets the SERVICE_ON flag in the shared preferences to the given boolean
     * value.
     *
     * @param _isOn The boolean value to which to set the SERVICE_ON flag.
     */
    public static void setServiceOn(boolean _isOn) {
        editor.putBoolean("SERVICE_ON", _isOn);
        editor.commit();
    }

    /**
     * Checks if a user is currently logged in to the CountdownDisplay
     * application. Returns a default value of <code>false</code> if the
     * IS_REGISTERED flag has not been explicitly set previously.
     *
     * @return <code>true</code> if a user is currently logged in to the
     * CountdownDisplay application, <code>false</code> if no user is
     * logged in.
     */
    public static boolean isRegistered() {
        return sharedPreferences.getBoolean("IS_REGISTERED", false);
    }

    /**
     * Sets the IS_REGISTERED flag in the shared preferences to the given
     * boolean value.
     *
     * @param _isRegistered The boolean value to which to set the IS_REGISTERED flag.
     */
    public static void setRegistered(boolean _isRegistered) {
        editor.putBoolean("IS_REGISTERED", _isRegistered);
        editor.commit();
    }

    /**
     * Gets the user ID stored in shared preferences. This user ID refers to the
     * unique row ID for this user in the User table of the PMP mobility
     * database. Returns a default value of -1 if the USER_ID flag has not been
     * explicitly set previously.
     *
     * @return The logged in user's user ID if a user is logged in, -1 if no one
     * is logged in.
     */
    public static String getUserId() {
        return sharedPreferences.getString("USER_ID", null);
    }

    /**
     * Sets the USER_ID in the shared preferences to the given value.
     *
     * @param _userId The value to which to set the USER_ID.
     */
    public static void setUserId(String _userId) {
        editor.putString("USER_ID", _userId);
        editor.commit();
    }


    /**
     * Gets the user code stored in shared preferences. This user ID refers to the
     * unique row ID for this user in the User table of the PMP mobility
     * database. Returns a default value of -1 if the USER_ID flag has not been
     * explicitly set previously.
     *
     * @return The logged in user's user ID if a user is logged in, -1 if no one
     * is logged in.
     */
    public static String getUserCode() {
        return sharedPreferences.getString("USER_CODE", null);
    }

    /**
     * Sets the USER_CODE in the shared preferences to the given value.
     *
     * @param _userCode The value to which to set the USER_ID.
     */
    public static void setUserCode(String _userCode) {
        editor.putString("USER_CODE", _userCode);
        editor.commit();
    }


    /**
     * Gets the public key stored in shared preferences.
     *
     * @return The current phone's public key if set, na otherwise.
     */
    public static String getPublicKey() {
        return sharedPreferences.getString("PK", "na");
    }

    /**
     * Sets the public key in the shared preferences to the given value.
     *
     * @param _pk The value to which to set the oublic key.
     */
    public static void setPublicKey(String _pk) {
        editor.putString("PK", _pk);
        editor.commit();
    }

    /**
     * Gets the number of uploads stored in shared preferences.
     *
     * @return The current number of uploads, -1 otherwise.
     */
    public static int getNUploads() {
        return sharedPreferences.getInt("N_UPLOADS", 0);
    }

    /**
     * Sets the user's nUploads in the shared preferences to the given value.
     * <p/>
     * The value to which to set the nUploads key.
     */
    public static void setNUploads(int _nUploads) {
        editor.putInt("N_UPLOADS", _nUploads);
        editor.commit();
    }

    public static int incrementUploads() {
        int nUploads = getNUploads();
        nUploads++;
        editor.putInt("N_UPLOADS", nUploads);
        editor.commit();
        return nUploads;
    }

    /**
     * Gets the number of uploads stored in shared preferences.
     *
     * @return The current number of uploads, -1 otherwise.
     */
    public static int getNFixes() {
        return sharedPreferences.getInt("N_FIXES", -1);
    }

    public static void setNFixes(int _nFixes) {
        editor.putInt("N_FIXES", _nFixes);
        editor.commit();
    }

    public static void incrementFixes() {
        int nFixes = getNFixes();
        nFixes++;
        editor.putInt("N_FIXES", nFixes);
        editor.commit();
    }

    /**
     * Gets the number of fixes stored in user's database
     *
     * @return The current number of fixes, -1 otherwise.
     */
    public static int getNUserFixes() {
        return sharedPreferences.getInt("N_USER_FIXES", -1);
    }

    public static void setNUserFixes(int _nUserFixes) {
        editor.putInt("N_USER_FIXES", _nUserFixes);
        editor.commit();
    }

    public static void updateNUserFixes(int netChange) {
        int nUserFixes = getNUserFixes() + netChange;
        setNUserFixes(nUserFixes);
    }

    /**
     * Sets flag indicating user has consented.
     *
     * @param _consented True if user has consented; false otherwise.
     */
    public static void setConsent(boolean _consented) {
        editor.putBoolean("CONSENTED", _consented);
        editor.commit();
    }

    public static boolean hasConsented() {
        return sharedPreferences.getBoolean("CONSENTED", false);
    }

    /**
     * Stores the time of consent in the shared preferences to the given value.
     *
     * @param _consentTime The time of consent.
     */
    public static void setConsentTime(String _consentTime) {
        editor.putString("CONSENT_TIME", _consentTime);
        editor.commit();
    }

    public static String getConsentTime() {
        return sharedPreferences.getString("CONSENT_TIME", "");
    }

    public static String USER_KEY = "user_key";

    public static String getUserKey() {
        return sharedPreferences.getString(USER_KEY, UtilLocal.CAMPUS_MAPPER_ANONYMOUS_API_KEY);
    }


}