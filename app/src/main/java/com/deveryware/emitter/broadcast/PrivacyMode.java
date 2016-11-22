/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 3 déc. 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.library.GiftClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class PrivacyMode extends Answer {

    public static final String APC = "apc";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(Constants.NAME, "privacy mode is switched ...");

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE);

        final Intent notif = new Intent(Constants.PRIVACY_MODE_SWITCH);
        notif.putExtra(APC, enabled);
        context.sendBroadcast(notif);

        intent.putExtra(GiftClient.APC, enabled);
        super.onReceive(context, intent);
    }
}
