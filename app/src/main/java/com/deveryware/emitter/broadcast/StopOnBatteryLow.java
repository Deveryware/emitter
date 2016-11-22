/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.widget.DisplayIconNotification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StopOnBatteryLow extends Stop {

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean stop = pref.getBoolean(Constants.STOP_LOW_BATTERY, Constants.DEFAULT_STOP_LOW_BATTERY);

        if (stop) {
            DisplayIconNotification.update(context);
        }

        return stop;
    }
}
