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

public class StopOnce extends Stop {

    public static final String MESSAGE = "com.deveryware.emitter.STOP";

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        return true;
    }

}
