/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 14 mars 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.library.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class LstPosReceived extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Location location = (Location) intent.getParcelableExtra(Constants.PROVIDER);
        if (location != null) {
            Log.i(com.deveryware.emitter.Constants.NAME, "position received from server");
            Log.d(com.deveryware.emitter.Constants.NAME, "latitude:" + location.getLatitude());
            Log.d(com.deveryware.emitter.Constants.NAME, "longitude:" + location.getLongitude());
            Log.d(com.deveryware.emitter.Constants.NAME, "altitude:" + location.getAltitude());
            Log.d(com.deveryware.emitter.Constants.NAME, "time:" + location.getTime());
        }
    }

}
