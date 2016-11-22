/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.services.UpdateLocationService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @see http
 *      ://www.netmite.com/android/mydroid/1.0/frameworks/base/location/java/com/android/internal/location/GpsLocationProvider.
 *      java
 * @author sylvek
 * 
 */
public class StartOnLocation extends Start {

    public static final String GPS_FIX_CHANGE_ACTION = "android.location.GPS_FIX_CHANGE";

    public static final String EXTRA_ENABLED = "enabled";

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean isGPS = pref.getString(Constants.PROVIDER, Constants.DEFAULT_PROVIDER).equals(LocationManager.GPS_PROVIDER);
        final boolean isLocation = pref.getBoolean(Constants.START_ON_GPS, Constants.DEFAULT_START_ON_GPS);
        final boolean isON = intent.getBooleanExtra(EXTRA_ENABLED, false);

        // si le service UploadLocationService tourne alors on est dans le cas où nous traitons déjà la position.
        final boolean preventCyclicCall = UpdateLocationService.isStarted();
        if (isLocation && isGPS && isON && preventCyclicCall) {
            Log.w(Constants.NAME, "preventing a cyclic call of the emitter (GPS)");
        }

        return isLocation && isGPS && isON && !preventCyclicCall;
    }

    @Override
    protected boolean resetAttempt()
    {
        return true;
    }

}
