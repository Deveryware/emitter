/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.library.TransportClientOverSMS;
import com.deveryware.library.TransportClientOverSMSForSDK1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class ReceiveCommandBySMS extends BroadcastReceiver {

    public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (intent.getAction().equals(ACTION) && pref.getBoolean(Constants.COMMAND_BY_SMS, Constants.DEFAULT_COMMAND_BY_SMS)) {
            int sdkVersion = Build.VERSION.SDK_INT;

            Log.i(Constants.NAME, "receiving an sms ...");

            boolean success;
            if (sdkVersion < 4 /* DONUT - Android 1.6 */) {
                success = TransportClientOverSMSForSDK1.receiveSMS(context, intent);
            } else {
                success = TransportClientOverSMS.receiveSMS(context, intent);
            }

            if (success) {
                Log.i(Constants.NAME, "this sms is a command.");
                abortBroadcast();
            }
        }
    }

}
