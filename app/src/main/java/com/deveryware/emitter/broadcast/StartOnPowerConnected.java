/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 8 déc. 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.services.UpdateLocationService;
import com.deveryware.library.AlarmParameter;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * @author sylvek
 * 
 */
public class StartOnPowerConnected extends Start {

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        intent.putExtra(UpdateLocationService.CONDITION, new AlarmParameter(4, "32"));
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.START_ON_POWER_DISCONNECTED,
                Constants.DEFAULT_START_ON_POWER_DISCONNECTED);
    }

    @Override
    protected boolean resetAttempt()
    {
        return true;
    }

}
