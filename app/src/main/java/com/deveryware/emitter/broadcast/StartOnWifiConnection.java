/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class StartOnWifiConnection extends Start {

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        boolean isWifi = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.START_ON_WIFI,
                Constants.DEFAULT_START_ON_WIFI);
        return networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && isWifi;
    }

    @Override
    protected boolean resetAttempt()
    {
        return true;
    }
}
