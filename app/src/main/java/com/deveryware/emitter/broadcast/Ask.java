/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 7 mai 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.ui.DisplayAsk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class Ask extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(Constants.NAME, "phone is asking ...");
        final Intent ask = (Intent) intent.clone();
        ask.setClass(context, DisplayAsk.class);
        ask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(ask);
    }

}
