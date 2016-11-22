/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 13 mars 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.library.GiftClient;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class Ring extends Answer {

    final static Uri ALERT = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

    final static Uri ALL = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(Constants.NAME, "phone is ringing ...");

        Ringtone ringtone = null;
        int result = 1;

        if (ALERT == null) {
            ringtone = RingtoneManager.getRingtone(context, ALERT);
        } else {
            ringtone = RingtoneManager.getRingtone(context, ALL);
        }

        if (ringtone != null) {
            result = 0;
            ringtone.play();
        }

        intent.putExtra(GiftClient.CMD_ACK, result);
        super.onReceive(context, intent);
    }

}
