/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.services;

import com.deveryware.emitter.Constants;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public abstract class LockableSynchronizedService extends Service {

    public static final String FORCE = "force";

    private final static String TAG = Constants.NAME + "." + LockableSynchronizedService.class.getSimpleName();

    private static boolean toReset = false;

    private volatile int startedSynchronized;

    private static WakeLock LOCK = null;

    private static void clearLock()
    {
        if (LOCK != null && LOCK.isHeld()) {
            LOCK.release();
        }
        LOCK = null;
        toReset = false;
    }

    private static PowerManager.WakeLock getLock(Context context)
    {
        if (toReset) {
            clearLock();
        }

        if (LOCK == null) {
            initLock(context, PowerManager.PARTIAL_WAKE_LOCK);
        }

        return LOCK;
    }

    public static void resetLock()
    {
        toReset = true;
    }

    public static void startService(Context ctxt, Intent i)
    {
        getLock(ctxt).acquire();
        ctxt.startService(i);
    }

    public abstract void onStartSynchronized(Intent intent, int startId);

    private static void initLock(Context context, int flags)
    {
        final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        LOCK = powerManager.newWakeLock(flags, TAG);
        LOCK.setReferenceCounted(true);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        /* no-op on android 2.x used for old phone */
        // setForeground(true);

        /* make sure that isStartedSynchronized is false */
        startedSynchronized = 0;
    }

    protected abstract boolean isForce(final Intent intent);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        final WakeLock lock = getLock(this);
        if (!lock.isHeld()) { // fail-safe for crash restart
            Log.w(Constants.NAME, "fail safe for crash restart");
            lock.acquire();
        }

        final boolean isForce = isForce(intent);
        if (startedSynchronized < 1 || isForce) {
            if (isForce) {
                Log.i(Constants.NAME, "force mode activated, we force the launching of the service.");
            }
            startedSynchronized += 1; // startId is an increment of calling of "onStart"
            Log.d(Constants.NAME, "starting service. number of call increasing = " + startedSynchronized);
            onStartSynchronized(intent, startId);
        } else {
            Log.w(Constants.NAME, "the service is currently running. We prevent a second call.");
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    protected void stop()
    {
        startedSynchronized -= 1;
        Log.d(Constants.NAME, "stopping service. number of call decreasing = " + startedSynchronized);

        if (startedSynchronized < 1) { // prevents negative value error, like -1. But it should never appear.
            Log.i(Constants.NAME, "[stopSelf] service.");
            stopSelf();
        }
    }

    @Override
    public void onDestroy()
    {
        if (getLock(this).isHeld()) {
            getLock(this).release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onLowMemory()
    {
        clearLock();
        super.onLowMemory();
        Log.w(Constants.NAME, "low memory detected");
    }
}
