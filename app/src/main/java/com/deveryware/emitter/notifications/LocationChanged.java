/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.notifications;

import com.deveryware.emitter.R;
import com.deveryware.emitter.ui.histories.Histories;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class LocationChanged extends Notifications {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (displayThisNotification(context)) {
            String reason = context.getString(R.string.location_changed);
            Intent settings = new Intent(context, Histories.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, settings, PendingIntent.FLAG_UPDATE_CURRENT);
            final Notification notification = new NotificationCompat.Builder(context).setContentText(reason)
                    .setContentTitle(context.getText(R.string.app_name))
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Histories.UUID, notification);
        }
    }

}
