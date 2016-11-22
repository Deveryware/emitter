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
public class Set extends Answer {

    public static final String MESSAGE = "com.deveryware.emitter.SET";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(Constants.NAME, "phone is setting ...");
        int valid = 0;

        final String[] params = intent.getStringArrayExtra(GiftClient.CMD_PARAMS);

        if (params != null && params.length == 3) {
            final String key = "com.deveryware." + params[0];
            final String type = params[1];
            final String value = params[2];
            Log.i(Constants.NAME, "setting of " + key + " with value " + value);

            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            try {
                if ("string".equals(type)) {
                    pref.edit().putString(key, value).commit();
                } else if ("int".equals(type)) {
                    pref.edit().putInt(key, Integer.valueOf(value)).commit();
                } else if ("long".equals(type)) {
                    pref.edit().putLong(key, Long.valueOf(value)).commit();
                } else if ("bool".equals(type)) {
                    pref.edit().putBoolean(key, Boolean.valueOf(value)).commit();
                } else if ("float".equals(type)) {
                    pref.edit().putFloat(key, Float.valueOf(value)).commit();
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
        super.onReceive(context, intent);
    }
}
