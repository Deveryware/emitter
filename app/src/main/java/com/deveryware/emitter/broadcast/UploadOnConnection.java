/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.broadcast;

import com.deveryware.emitter.Constants;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class UploadOnConnection extends Upload {

    @Override
    protected boolean needTo(Context context, Intent intent)
    {
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        boolean isUploadOnConnection = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Constants.UPLOAD_ON_CONNECTION, Constants.DEFAULT_UPLOAD_ON_CONNECTION);
        return networkInfo.getState() == NetworkInfo.State.CONNECTED && isUploadOnConnection;
    }
}
