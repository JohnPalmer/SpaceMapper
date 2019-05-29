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

/**
 * This is a class for holding all of the prefixes and json keys used in the data sent to server.
 * Created by palmer on 3/7/15.
 */
public class DataCodeBook {

    public static String API_JSON_KEY_USER_UUID = "user_UUID";
    public static String API_JSON_KEY_USER_CODE = "user_code";
    public static String API_JSON_KEY_TYPE = "type";
    public static String API_JSON_KEY_APP_VERSION = "app_version";
    public static String API_JSON_KEY_ENCRYPTED_MESSAGE = "encrypted_message";

    public static String CONSENT_PREFIX = "CON";
    public static String CONSENT_KEY_CONSENT_TIME = "consent_time";

    public static String REGISTRATION_PREFIX = "REG";
    public static String REGISTRATION_KEY_PHONE_LANG = "phone_language";
    public static String REGISTRATION_KEY_VERSION = "version";
    public static String REGISTRATION_KEY_SDK = "sdk";
    public static String REGISTRATION_KEY_USER_EMAIL = "user_email";
    public static String REGISTRATION_KEY_USER_CODE = "user_code";


    public static String DEBREIFING_PREFIX = "DEB";
    public static String DEBRIEFING_KEY_HOW_FOUND_FRIEND = "how_found_friend";
    public static String DEBRIEFING_KEY_HOW_FOUND_MARKET = "how_found_market";
    public static String DEBRIEFING_KEY_HOW_FOUND_AD = "how_found_ad";
    public static String DEBRIEFING_KEY_HOW_FOUND_OTHER = "how_found_other";
    public static String DEBRIEFING_KEY_FRIENDS_USING_APP = "friends_using_app";
    public static String DEBRIEFING_KEY_BATTERY_PROBLEMS = "battery_problems";
    public static String DEBRIEFING_KEY_BEHAVIOR_1 = "behavior_1";
    public static String DEBRIEFING_KEY_BEHAVIOR_2 = "behavior_2";

    public static String PRO_UPGRADE_PREFIX  = "PRO";
    public static String PRO_UPGRADE_PRO_MESSAGE = "pro_upgrade";

    public static String CELL_PREFIX = "CEL";
    public static String CELL_KEY_PHONE_TIME = "phone_time";
    public static String CELL_KEY_CID = "cid";
    public static String CELL_KEY_LAC = "lac";
    public static String CELL_KEY_COUNTRY_ISO = "country_iso";
    public static String CELL_KEY_BID = "bid";
    public static String CELL_KEY_NID = "nid";
    public static String CELL_KEY_SID = "sid";
    public static String CELL_KEY_BS_LAT = "bs_lat";
    public static String CELL_KEY_BS_LON = "bs_lon";

    public static String FIX_PREFIX_NORMAL = "FIX";
    public static String FIX_PREFIX_POSSIBLE_MOCK_LOCATION = "PMF";
    public static String FIX_KEY_LAT = "lat";
    public static String FIX_KEY_LON = "lon";
    public static String FIX_KEY_ACCURACY = "accuracy";
    public static String FIX_KEY_PROVIDER = "provider";
    public static String FIX_KEY_TIME = "time";
    public static String FIX_KEY_POWER = "power";

    public static String ON_OFF_PREFIX = "ONF";
    public static String ON_OFF_KEY_ON_OR_OFF = "on_or_off";
    public static String ON_OFF_KEY_TIME = "time";
    public static String ON_OFF_KEY_USAGE_TIME = "usage_time";

    public static String INTERVAL_PREFIX = "INT";
    public static String INTERVAL_KEY_TIME = "time";
    public static String INTERVAL_KEY_INTERVAL = "interval";

    public static String NMEA_PREFIX = "NME";
    public static String NMEA_KEY_TIME  = "time";
    public static String NMEA_KEY_LOCATION_MESSAGE = "location_message";
    public static String NMEA_KEY_HDOP = "hdop";

    public static String TRANSPORT_PREFIX = "TRA";
    public static String TRANSPORT_KEY_TIME  = "time";
    public static String TRANSPORT_KEY_MODE_RESPONSE = "transport_mode_response";


}
