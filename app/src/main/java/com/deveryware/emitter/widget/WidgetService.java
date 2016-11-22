/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 7 déc. 2011
 *
 */
package com.deveryware.emitter.widget;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author sylvek
 * 
 */
public class WidgetService extends IntentService {

    public WidgetService()
    {
        super(WidgetService.class.getName());
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        WidgetProvider.update(this);
    }
}
