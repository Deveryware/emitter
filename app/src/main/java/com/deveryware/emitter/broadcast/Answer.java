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
import android.util.Log;

public class Answer extends StartOnce {

    public static final String MESSAGE = "com.deveryware.emitter.ANSWER";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(Constants.NAME, "phone is answering ...");
        intent.putExtra(UpdateLocationService.POSITION_OPTIONAL, true);
        intent.putExtra(LockableSynchronizedService.FORCE, true);
        intent.putExtra(Start.REPEAT, false);
        super.onReceive(context, intent);
    }
}
