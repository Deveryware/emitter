/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.services.UpdateLocationService;
import com.deveryware.library.AlarmParameter;

import android.content.Context;
import android.content.Intent;

public class AlarmOnce extends Answer {

    public static final String MESSAGE = "com.deveryware.emitter.ALARM";

    public static final String ALARM_CODE = "alarmcode";

    public static final String PARAM1 = "param1";

    public static final String PARAM2 = "param2";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String alarmCode = intent.getStringExtra(ALARM_CODE);
        String param1 = intent.getStringExtra(PARAM1);
        String param2 = intent.getStringExtra(PARAM2);
        intent.putExtra(UpdateLocationService.CONDITION, new AlarmParameter(4, alarmCode, param1, param2));
        super.onReceive(context, intent);
    }
}
