/**
 *
 * Copyright (C) 2012 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 27 janv. 2012
 *
 */
package com.deveryware.emitter.ui.geofencing;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @author sylvek
 * 
 */
public class StopGeofencing extends BroadcastReceiver {

    public static final int GEOFENCING_NOTIFICATION_ID = 56564345;

    private static final String PROXIMITY_ALERT_INTENT = "proximity_alert";

    public static volatile boolean isStarted = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeProximityAlert((PendingIntent) intent.getParcelableExtra(PROXIMITY_ALERT_INTENT));

        Log.d(Constants.NAME, "the geofencing is cancelled");
        Toast.makeText(context, R.string.stopped_geofencing, Toast.LENGTH_LONG).show();

        isStarted = false;
    }

    /**
     * @param configurationActivity
     * @return
     */
    static PendingIntent get(Context context, PendingIntent pendingIntentProximityAlert)
    {
        isStarted = true;
        final Intent intent = new Intent(context, StopGeofencing.class);
        intent.putExtra(PROXIMITY_ALERT_INTENT, pendingIntentProximityAlert);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
