/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;


import android.content.Context;
import android.content.Intent;

public class Restart extends Start {

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        return true;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        stopRepeating(context);
        super.onReceive(context, intent);
    }

    @Override
    protected boolean resetAttempt()
    {
        return true;
    }
}
