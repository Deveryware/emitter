/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 30 nov. 2011
 *
 */
package com.deveryware.emitter.widget;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.broadcast.PrivacyMode;
import com.deveryware.emitter.broadcast.StartOnce;
import com.deveryware.emitter.broadcast.StopOnce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author sylvek
 * 
 */
public class ManagePrivacyMode extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE);
        pref.edit().putBoolean(Constants.GEOLOC_ENABLE, !enabled).commit();
        ManagePrivacyMode.update(context);
    }

    public static final void update(Context context)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean inform = pref.getBoolean(Constants.INFORM_PRIVACY, Constants.DEFAULT_INFORM_PRIVACY);
        boolean toStart = pref.getBoolean(Constants.START_ON_GEOLOC_ENABLE, Constants.DEFAULT_START_ON_GEOLOC_ENABLE);

        DisplayIconNotification.update(context);
        WidgetProvider.update(context);

        context.sendBroadcast(new Intent(context, StopOnce.class));

        if (inform) {
            context.sendBroadcast(new Intent(context, PrivacyMode.class));
        }

        if (toStart) {
            context.sendBroadcast(new Intent(context, StartOnce.class));
        }
    }
}
