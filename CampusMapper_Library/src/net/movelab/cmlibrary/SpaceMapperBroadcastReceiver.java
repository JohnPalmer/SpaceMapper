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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Property;

import net.movelab.cmlibrary.R;

/**
 * Schedules alarm to run FixGet service upon boot up of the phone, if a
 * registered user. Mostly taken from the Human Mobility Project code.
 *
 * @author Chang Y. Chung
 * @author Necati E. Ozgencil
 * @author John R.B. Palmer
 */
public class SpaceMapperBroadcastReceiver extends BroadcastReceiver {
    /**
     * Responds to Android system broadcast that the phone device has just
     * powered on. If the user is indeed logged in, schedules alarm manager to
     * begin running the FixGet service at regular intervals, and sets the
     * SERVICE_ON flag to true in the shared preferences. Uses PropertyHolder to
     * retrieve the stored alarm interval, if any.
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        PropertyHolder.init(context);

        String action = intent.getAction();
        AlarmManager startFixGetAlarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent2FixGet = new Intent(context, FixGet.class);
        PendingIntent pendingIntent2FixGet = PendingIntent.getService(context,
                0, intent2FixGet, 0);

        AlarmManager startFileUploaderAlarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent2FileUploader = new Intent(context, FileUploader.class);
        PendingIntent pendingIntent2FileUploader = PendingIntent.getService(
                context, 0, intent2FileUploader, 0);

        AlarmManager stopFixGetAlarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent2StopFixGet = new Intent(context.getResources().getString(
                R.string.internal_message_id)
                + Util.MESSAGE_STOP_FIXGET);
        PendingIntent pendingFixGetStop = PendingIntent.getBroadcast(context,
                0, intent2StopFixGet, 0);

        AlarmManager messageCAlarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending_message_C = PendingIntent.getBroadcast(context,
                0, new Intent(context.getResources().getString(
                R.string.internal_message_id)
                + Util.MESSAGE_MAKE_MESSAGE_C_NOTIFICATION), 0);


        if (action != null) {

            if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_UNSCHEDULE)) {
                startFixGetAlarm.cancel(pendingIntent2FixGet);
                messageCAlarm.cancel(pending_message_C);
                PropertyHolder.setServiceOn(false);
                PropertyHolder.ptStop();
                cancelNotification(context);
                cancelTransportNotification(context);
            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_SCHEDULE)) {

                if(PropertyHolder.getExpertMode()){
                 // start repeating alarm with first one in 10 minutes
                messageCAlarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10*60*1000,  Util.MESSAGE_C_INTERVAL, pending_message_C);
                }

                long alarmInterval = PropertyHolder.getAlarmInterval();
                int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
                long triggerTime = SystemClock.elapsedRealtime();
                startFixGetAlarm.setRepeating(alarmType, triggerTime,
                        alarmInterval, pendingIntent2FixGet);
                stopFixGetAlarm.setRepeating(alarmType, triggerTime
                        + Util.LISTENER_WINDOW, alarmInterval, pendingFixGetStop);
                Util.countingFrom = triggerTime;
                PropertyHolder.setServiceOn(true);
                createNotification(context);

                if (PropertyHolder.getShareData())
                    PropertyHolder.ptStart();

                long uploadAlarmInterval = Util.UPLOAD_INTERVAL;
                startFileUploaderAlarm.setRepeating(alarmType, triggerTime,
                        uploadAlarmInterval, pendingIntent2FileUploader);

            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_START_MESSAGE_AB_TIMER)) {
                AlarmManager messageAAlarm = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);
                AlarmManager messageBAlarm = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);

                PendingIntent pending_message_A = PendingIntent.getBroadcast(context,
                        0, new Intent(context.getResources().getString(
                        R.string.internal_message_id)
                        + Util.MESSAGE_MAKE_MESSAGE_A_NOTIFICATION), 0);

                PendingIntent pending_message_B = PendingIntent.getBroadcast(context,
                        0, new Intent(context.getResources().getString(
                        R.string.internal_message_id)
                        + Util.MESSAGE_MAKE_MESSAGE_B_NOTIFICATION), 0);

                messageAAlarm.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + Util.time_to_message_a, pending_message_A);
                messageBAlarm.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + Util.time_to_message_b, pending_message_B);

            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_START_MESSAGE_C_TIMER)) {

                messageCAlarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),  Util.MESSAGE_C_INTERVAL, pending_message_C);

            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_MAKE_MESSAGE_A_NOTIFICATION)) {

                createMessage(context, true, context.getResources().getString(R.string.popup_a_title), context.getResources().getString(R.string.popup_a_ticker), context.getResources().getString(R.string.popup_a), Util.MESSAGE_A_NOTIFICATION);

            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_MAKE_MESSAGE_B_NOTIFICATION)) {

                if (PropertyHolder.getNUploads() > 0) {
                    createMessage(context, true, context.getResources().getString(R.string.popup_b1_title), context.getResources().getString(R.string.popup_b1_ticker), context.getResources().getString(R.string.popup_b1), Util.MESSAGE_B_NOTIFICATION);
                } else {
                    createMessage(context, false, context.getResources().getString(R.string.popup_b2_title), context.getResources().getString(R.string.popup_b2_ticker), context.getResources().getString(R.string.popup_b2), Util.MESSAGE_B_NOTIFICATION);
                }
            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_MAKE_MESSAGE_C_NOTIFICATION)) {

                    createTransportModeNotification(context);

            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_CANCEL_A_NOTIFICATION)) {

                cancelMessage(context, Util.MESSAGE_A_NOTIFICATION);

            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_CANCEL_B_NOTIFICATION)) {

            cancelMessage(context, Util.MESSAGE_B_NOTIFICATION);
            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_CANCEL_C_NOTIFICATION)) {

                cancelTransportNotification(context);

            } else if (action.contains("BOOT_COMPLETED")) {

                PropertyHolder.ptStop();

                if (PropertyHolder.isServiceOn()) {
                    Intent intent2broadcast = new Intent(
                            context.getString(R.string.internal_message_id)
                                    + Util.MESSAGE_SCHEDULE);
                    context.sendBroadcast(intent2broadcast);
                    if (PropertyHolder.getShareData()) {
                        long ptNow = PropertyHolder.ptStart();
                        ContentResolver ucr = context.getContentResolver();
                        ucr.insert(
                                Util.getUploadQueueUri(context),
                                UploadContentValues.createUpload(DataCodeBook.ON_OFF_PREFIX, Util.makeOnfJsonString(true, ptNow)));

                    }
                }

            } else if (action.contains("ACTION_SHUTDOWN")
                    || action.contains("QUICKBOOT_POWEROFF")) {
                long ptNow = PropertyHolder.ptStop();
                ContentResolver ucr = context.getContentResolver();
                ucr.insert(
                        Util.getUploadQueueUri(context),
                        UploadContentValues.createUpload(DataCodeBook.ON_OFF_PREFIX, Util.makeOnfJsonString(false, ptNow)));


            } else if (action.contains(Intent.ACTION_POWER_CONNECTED)) {
                context.startService(intent2FileUploader);
            } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, true)) {
                    context.startService(intent2FileUploader);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void createNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int thisnotification_icon = PropertyHolder.getProVersion() ? R.drawable.notification_pro
                : R.drawable.notification;
        Notification notification = new Notification(thisnotification_icon,
                context.getResources().getString(
                        R.string.tracking_notification_initial),
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        Intent intent = new Intent(context, MapMyData.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(context, context.getResources()
                .getString(R.string.tracking_notification_subject), context
                .getResources().getString(R.string.tracking_notification),
                pendingIntent);
        notificationManager.notify(Util.TRACKING_NOTIFICATION, notification);

    }

    public void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Util.TRACKING_NOTIFICATION);

    }

    @SuppressWarnings("deprecation")
    public void createMessage(Context context, Boolean working, String message_title, String message_ticker, String message_body, int notification_code) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int thisnotification_icon = working ? R.drawable.ic_stat_heart
                : R.drawable.ic_stat_info;
        Notification notification = new Notification(thisnotification_icon,
                message_ticker,
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        Intent intent = new Intent(context, Message.class);
        intent.putExtra("message_body", message_body);
        intent.putExtra("notification_code", notification_code);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(context, message_title, message_ticker,
                pendingIntent);
        notificationManager.notify(notification_code, notification);

    }

    public void cancelMessage(Context context, int notification_code) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notification_code);

    }


    @SuppressWarnings("deprecation")
    public void createTransportModeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int thisnotification_icon = R.drawable.ic_stat_survey;
        Notification notification = new Notification(thisnotification_icon,
                context.getResources().getString(R.string.popup_c_ticker),
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        Intent ts_intent = new Intent(context, TransportationModeSurvey.class);

        PendingIntent ts_pendingIntent = PendingIntent.getActivity(context, 0,
                ts_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(context, context.getResources().getString(R.string.popup_c_title), context.getResources().getString(R.string.popup_c_ticker),
                ts_pendingIntent);
        notificationManager.notify(Util.MESSAGE_C_NOTIFICATION, notification);

    }

    public void cancelTransportNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Util.MESSAGE_C_NOTIFICATION);

    }


}
