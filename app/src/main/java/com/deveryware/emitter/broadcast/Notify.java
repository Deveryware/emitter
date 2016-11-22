/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 13 mars 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.ui.DisplayAsk;
import com.deveryware.library.GiftClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class Notify extends Answer {

    private static final int NOTIFY_ID = 542454;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(Constants.NAME, "phone is notifying ...");

        int valid = 1;
        final String[] params = intent.getStringArrayExtra(GiftClient.CMD_PARAMS);
        if (params != null && params.length == 1) {
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final String toNotify = params[0];
            final Intent display = (Intent) intent.clone();
            display.setClass(context, DisplayAsk.class);
            display.putExtra(DisplayAsk.ACK, false);
            final PendingIntent pi = PendingIntent.getActivity(context, 0, display, PendingIntent.FLAG_UPDATE_CURRENT);

            final Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true)
                    .setContentText(toNotify)
                    .setContentTitle(context.getText(R.string.app_name))
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                    .setContentIntent(pi)
                    .build();

            notificationManager.notify(NOTIFY_ID, notification);
            valid = 0;
        }

        intent.putExtra(GiftClient.CMD_ACK, valid /* 1 si erreur */);
        super.onReceive(context, intent);
    }
}
