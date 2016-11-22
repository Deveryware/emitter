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
import android.preference.PreferenceManager;

public class StartOnBoot extends Start {

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.START_ON_BOOT,
                Constants.DEFAULT_START_ON_BOOT);
    }

    @Override
    protected boolean resetAttempt()
    {
        return true;
    }
}
