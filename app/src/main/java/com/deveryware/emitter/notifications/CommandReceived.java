/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.notifications;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.ui.preferences.Settings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class CommandReceived extends Notifications {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (displayThisNotification(context)) {
            String from = intent.getStringExtra(Constants.FROM);
            String reason = context.getString(R.string.command_received, from);
            Intent settings = new Intent(context, Settings.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, settings, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon)
                    .setContentText(reason)
                    .setContentTitle(context.getText(R.string.app_name))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Settings.UUID, notification);
        }
    }
}
