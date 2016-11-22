/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 3 déc. 2011
 *
 */
package com.deveryware.emitter.notifications;

import com.deveryware.emitter.R;
import com.deveryware.emitter.broadcast.PrivacyMode;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @author sylvek
 * 
 */
public class PrivacyModeSwitched extends Notifications {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final boolean apc = intent.getBooleanExtra(PrivacyMode.APC, true);
        if (displayThisNotification(context)) {
            if (!apc) {
                Toast.makeText(context, R.string.privacy_mode_switched_on, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, R.string.privacy_mode_switched_off, Toast.LENGTH_LONG).show();
            }
        }
    }

}
