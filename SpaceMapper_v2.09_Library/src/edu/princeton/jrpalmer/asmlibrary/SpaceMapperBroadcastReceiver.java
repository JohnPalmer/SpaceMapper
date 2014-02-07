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
 * 
 * This file incorporates code written by Chang Y. Chung and Necati E. Ozgencil 
 * for the Human Mobility Project, which is subject to the following terms: 
 * 
 * 		Copyright (C) 2010, 2011 Human Mobility Project
 *
 *		Permission is hereby granted, free of charge, to any person obtaining 
 *		a copy of this software and associated documentation files (the
 *		"Software"), to deal in the Software without restriction, including
 *		without limitation the rights to use, copy, modify, merge, publish, 
 *		distribute, sublicense, and/or sell copies of the Software, and to
 *		permit persons to whom the Software is furnished to do so, subject to
 *		the following conditions:
 *
 *		The above copyright notice and this permission notice shall be included
 *		in all copies or substantial portions of the Software.
 *
 *		THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *		EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *		MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *		IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *		CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *		TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *		SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. * 
 */

package edu.princeton.jrpalmer.asmlibrary;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

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

		if (action.contains(context.getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_UNSCHEDULE)) {
			startFixGetAlarm.cancel(pendingIntent2FixGet);
			PropertyHolder.setServiceOn(false);
			PropertyHolder.ptStop();
			cancelNotification(context);
		} else if (action.contains(context.getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_SCHEDULE)) {
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

		} else if (action.contains("BOOT_COMPLETED")) {

			PropertyHolder.ptStop();

			if (PropertyHolder.isServiceOn()) {
				Intent intent2broadcast = new Intent(
						context.getString(R.string.internal_message_id)
								+ Util.MESSAGE_SCHEDULE);
				context.sendBroadcast(intent2broadcast);
				if (PropertyHolder.getShareData())
					PropertyHolder.ptStart();
			}

		} else if (action.contains("ACTION_SHUTDOWN")
				|| action.contains("QUICKBOOT_POWEROFF")) {
			PropertyHolder.ptStop();

		} else {
			// do nothing
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

}
