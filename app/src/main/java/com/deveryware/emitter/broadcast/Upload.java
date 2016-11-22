/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.services.LockableSynchronizedService;
import com.deveryware.emitter.services.UploadQueriesService;

import android.content.Context;
import android.content.Intent;

public abstract class Upload extends ServiceManager {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        if (needTo(context, intent)) {
            Intent upload = new Intent(context, UploadQueriesService.class);
            LockableSynchronizedService.startService(context, upload);
        }
    }
}
