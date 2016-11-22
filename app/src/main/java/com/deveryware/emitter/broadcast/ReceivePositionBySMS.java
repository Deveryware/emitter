/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 21 nov. 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.Position;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class ReceivePositionBySMS extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion > 3 /* DONUT - Android 1.6 */) {
            if (Position.receiveSMS(context, intent)) {
                Log.e(Constants.NAME, "sms treated with success");
                abortBroadcast();
            }
        }
    }
}
