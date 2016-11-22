/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class ServiceManager extends BroadcastReceiver {

    protected static final String REPEAT = "repeat";

    protected static PendingIntent wakeUp;

    private static final int STOP_NOW = 0;

    private static final int INFINITE_LOOP = -1;

    private static final int TO_BE_SET = -2;

    /**
     * Informs if a location is needed.
     * 
     * @param context
     * @param intent
     * @return true if location is needed
     */
    protected abstract boolean needTo(Context context, Intent intent);

    private int performLooping(int loop, int defaultLoop)
    {
        switch (loop) {
        case INFINITE_LOOP:
            return INFINITE_LOOP;
        case STOP_NOW:
            return STOP_NOW;
        case TO_BE_SET:
            return performLooping(defaultLoop, defaultLoop);
        default:
            return loop - 1;
        }
    }

    protected void startRepeating(Context context)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isWakeUp = pref.getBoolean(Constants.WAKE_UP, Constants.DEFAULT_WAKE_UP);

        if (isWakeUp) {
            final int defaultLoop = pref.getInt(Constants.ITERATION, Constants.DEFAULT_ITERATION);
            final int currentLoop = pref.getInt(Constants.CURRENT_ITERATION, TO_BE_SET);
            final int newLoop = performLooping(currentLoop, defaultLoop);
            if (newLoop == STOP_NOW) {
                Log.i(Constants.NAME, "end of repeat, we have stopped the repeating service.");
                pref.edit().remove(Constants.CURRENT_ITERATION).commit(); // la prochaine fois on sera en TO_BE_SET
                stopRepeating(context);
                return;
            } else {
                Log.i(Constants.NAME, "there is " + newLoop + " repeat");
                pref.edit().putInt(Constants.CURRENT_ITERATION, newLoop).commit();
            }

            if (wakeUp == null) {
                final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                final Intent startOnce = new Intent(context, StartOnce.class);
                wakeUp = PendingIntent.getBroadcast(context, 0, startOnce, PendingIntent.FLAG_UPDATE_CURRENT);
                final long minTime = pref.getLong(Constants.MIN_TIME, Constants.DEFAULT_MIN_TIME) * 1000L;
                final long firstOccurs = SystemClock.elapsedRealtime() + minTime;
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstOccurs, minTime, wakeUp);
                Log.i(Constants.NAME, "each repeat at " + minTime + " milliseconds");
            }
        }
    }

    protected void stopRepeating(Context context)
    {
        if (wakeUp != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(wakeUp);
            wakeUp = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
    }
}
