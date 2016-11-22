/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.services.LockableSynchronizedService;
import com.deveryware.emitter.services.UpdateLocationService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class Start extends ServiceManager {

    /**
     * Informs if the number of attempt need to be reseted
     * 
     * @return true if it should be reseted
     */
    protected abstract boolean resetAttempt();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE);
        boolean isForce = intent.getBooleanExtra(LockableSynchronizedService.FORCE, false);

        if (needTo(context, intent) && (enabled || isForce)) {

            if (!enabled) {
                Log.d(Constants.NAME, "location is forced and service is disabled");
            }

            if (resetAttempt()) {
                pref.edit().remove(Constants.CURRENT_ITERATION).commit(); // la prochaine fois on sera en TO_BE_SET
            }

            final Intent updateLocation = (Intent) intent.clone();
            updateLocation.setClass(context, UpdateLocationService.class);

            /*
             * on lance le mécanisme de répétition seulement si on n'est pas en mode forcé
             */
            if (intent.getBooleanExtra(REPEAT, true) && !isForce) {
                startRepeating(context);
            }

            Log.d(Constants.NAME, "update location service is starting");
            LockableSynchronizedService.startService(context, updateLocation);
        }
    }
}
