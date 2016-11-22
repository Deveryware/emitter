/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 29 nov. 2011
 *
 */
package com.deveryware.emitter.widget;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.ui.Home;
import com.deveryware.emitter.ui.histories.Histories;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class DisplayIconNotification extends BroadcastReceiver {

    private static final int ID = 756754;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        DisplayIconNotification.update(context.getApplicationContext());
    }

    public static final void update(Context context)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE);
        boolean displayIcon = pref.getBoolean(Constants.DISPLAY_ON_OFF, Constants.DEFAULT_DISPLAY_ON_OFF);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (displayIcon) {
            Log.d(Constants.NAME, "displaying notification icon ...");

            final int icon = (enabled) ? R.drawable.icon_enabled : R.drawable.icon_disabled;
            final String toNotify = (enabled) ? context.getString(R.string.enabled) : context.getString(R.string.disabled);
            final Intent display = new Intent(context, Home.class);
            final Intent history = new Intent(context, Histories.class);
            final Intent switchOnOff = new Intent(context, ManagePrivacyMode.class);
            final Notification notification = new NotificationCompat.Builder(context).setContentTitle(
                    context.getText(R.string.app_name))
                    .setContentText(toNotify)
                    .setSmallIcon(icon)
                    .setWhen(0L)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentIntent(PendingIntent.getActivity(context, 0, display, PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(R.drawable.ic_action_history, context.getText(R.string.history),
                            PendingIntent.getActivity(context, 0, history, PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(R.drawable.ic_action_privacy, context.getText(R.string.start_stop),
                            PendingIntent.getBroadcast(context, 0, switchOnOff, PendingIntent.FLAG_UPDATE_CURRENT))
                    .build();

            notificationManager.notify(ID, notification);
        } else {
            notificationManager.cancel(ID);
        }
    }
}
