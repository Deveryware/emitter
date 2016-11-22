/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 8 mai 2011
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;
import com.deveryware.library.GiftClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class Get extends Answer {

    public static final String MESSAGE = "com.deveryware.emitter.GET";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(Constants.NAME, "phone is getting ...");
        int valid = 0;
        String result = null;

        final String[] params = intent.getStringArrayExtra(GiftClient.CMD_PARAMS);

        if (params != null && params.length == 3) {
            final String key = "com.deveryware." + params[0];
            final String type = params[1];
            final String value = params[2];
            Log.i(Constants.NAME, "getting of " + key);

            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            try {
                if ("string".equals(type)) {
                    result = pref.getString(key, value);
                } else if ("int".equals(type)) {
                    result = Integer.toString(pref.getInt(key, Integer.valueOf(value)));
                } else if ("long".equals(type)) {
                    result = Long.toString(pref.getLong(key, Long.valueOf(value)));
                } else if ("bool".equals(type)) {
                    result = Boolean.toString(pref.getBoolean(key, Boolean.valueOf(value)));
                } else if ("float".equals(type)) {
                    result = Float.toString(pref.getFloat(key, Float.valueOf(value)));
                } else {
                    valid = 1;
                    Log.e(Constants.NAME, "type (" + type + ") must be string/int/long/bool/float");
                }
            } catch (NumberFormatException e) {
                valid = 1;
                Log.e(Constants.NAME, "given value (" + value + ") for specified type (" + type + ") is invalid for key " + key);
            }
        } else {
            valid = 1;
            Log.e(Constants.NAME, "3 params needed, key (without com.deveryware.), type and value");
        }

        intent.putExtra(GiftClient.CMD_ACK, valid /* 1 si erreur */);
        intent.putExtra(GiftClient.CMD_TEXT, result);
        super.onReceive(context, intent);
    }
}
