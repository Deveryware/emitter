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
import com.deveryware.emitter.services.UploadQueriesService;
import com.deveryware.emitter.widget.WidgetProvider;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class Stop extends ServiceManager {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        if (needTo(context, intent)) {

            /*
             * on stop le mécanisme de répétition
             */
            if (intent.getBooleanExtra(REPEAT, true)) {
                stopRepeating(context);
            }

            Log.d(Constants.NAME, "update location service is stopping");
            context.stopService(new Intent(context, UpdateLocationService.class));

            Log.d(Constants.NAME, "upload queries service is stopping");
            context.stopService(new Intent(context, UploadQueriesService.class));

            /*
             * make sure that we off the star
             */
            WidgetProvider.stopService(context);
        }
    }
}
