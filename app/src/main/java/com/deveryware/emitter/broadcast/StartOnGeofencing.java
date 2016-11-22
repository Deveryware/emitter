/**
 *
 * Copyright (C) 2012 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 27 janv. 2012
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.services.UpdateLocationService;
import com.deveryware.library.AlarmParameter;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class StartOnGeofencing extends Start {

    public static final String KEY_PROXIMITY_ENTERING = "start_on_entering";

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        final boolean startOnEntering = intent.getBooleanExtra(KEY_PROXIMITY_ENTERING, false);
        final boolean enteryStatus = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);

        final String alarmCode = (enteryStatus) ? "11" : "10";
        intent.putExtra(UpdateLocationService.CONDITION, new AlarmParameter(4, alarmCode));

        Log.d(Constants.NAME, "expected:" + startOnEntering + ", received:" + enteryStatus);
        return startOnEntering == enteryStatus;
    }

    @Override
    protected boolean resetAttempt()
    {
        return true;
    }

}
