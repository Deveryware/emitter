/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.UUID;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
@ReportsCrashes(mailTo = "smaucourt@gmail.com", formKey = "", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class DwApplication extends Application {

    @Override
    public void onCreate()
    {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);

        DwApplication.init(this);

        super.onCreate();
    }

    public static void init(final Context context)
    {
        // Initialize the identity_key if not exists
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (!pref.contains(Constants.IDENTITY_KEY)) {
            final String identity = UUID.randomUUID().toString().substring(28);
            Log.d(Constants.NAME, "generating identity_key: " + identity);
            pref.edit().putString(Constants.IDENTITY_KEY, identity).commit();
        }

        disableConnectionReuseIfNecessary();
    }

    private static void disableConnectionReuseIfNecessary()
    {
        // Work around pre-Froyo bugs in HTTP connection reuse.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
}
