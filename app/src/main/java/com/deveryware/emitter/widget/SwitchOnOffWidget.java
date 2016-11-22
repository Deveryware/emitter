/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 2 déc. 2011
 *
 */
package com.deveryware.emitter.widget;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.broadcast.StartOnce;
import com.deveryware.emitter.broadcast.StopOnce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class SwitchOnOffWidget extends BroadcastReceiver {

    private static boolean started = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE);

        Log.d(Constants.NAME, "SwitchOnOffWidget called:" + started);
        if (enabled) {
            if (!started) {
                final Intent start = new Intent(context, StartOnce.class);
                context.sendBroadcast(start);
                WidgetProvider.startService(context);
            } else {
                final Intent stop = new Intent(context, StopOnce.class);
                context.sendBroadcast(stop);
                WidgetProvider.stopService(context);
            }
        } else {
            started = false;
        }
    }

    public static void setStarted(boolean started)
    {
        SwitchOnOffWidget.started = started;
    }

}
