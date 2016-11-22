/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.notifications;

import com.deveryware.emitter.R;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class LocationRequested extends Notifications {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (displayThisNotification(context)) {
            Toast.makeText(context, R.string.location_requested, Toast.LENGTH_LONG).show();
        }
    }

}
