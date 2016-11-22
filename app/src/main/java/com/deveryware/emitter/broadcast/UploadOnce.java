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

public class UploadOnce extends Upload {

    public static final String MESSAGE = "com.deveryware.emitter.UPLOAD";

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        return true;
    }

}
