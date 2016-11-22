/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.notifications;

import com.deveryware.emitter.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class Notifications extends BroadcastReceiver {

    public boolean displayThisNotification(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(Constants.NOTIFS, Constants.DEFAULT_NOTIF);
    }

}
