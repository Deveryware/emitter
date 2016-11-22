/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 23 nov. 2011
 *
 */
package com.deveryware.emitter;

import com.deveryware.gift.GiftQuery;
import com.deveryware.gift.data.AlarmParameter;
import com.deveryware.gift.data.AlarmParameter.Alarm;
import com.deveryware.gift.data.GpsLocation;
import com.deveryware.gift.data.Query;
import com.deveryware.library.TransportClientOverSMS;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * @author sylvek
 * 
 */
public class Position {

    public static final String EXTRA_LOCATION = "position";

    private static final GiftQuery GIFT = new GiftQuery();

    final public static boolean receiveSMS(Context context, Intent intent)
    {
        final Bundle bundle = intent.getExtras();
        final Object[] pdusObj = (Object[]) bundle.get("pdus");

        boolean success = false;

        for (int i = 0; i < pdusObj.length; i++) {
            final SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
            final String from = currentMessage.getDisplayOriginatingAddress();
            final String query = currentMessage.getMessageBody();

            if (query != null && query.startsWith(TransportClientOverSMS.PREFIX_GIFT) && query.length() > 4) {
                final String gift = query.substring(3);
                try {
                    final Query q = GIFT.parse(gift);
                    String reason = context.getString(R.string.posted_position, from);

                    final GpsLocation gps = q.getGpsLocation();
                    final AlarmParameter alarm = q.getAlarmParameter();
                    if (alarm != null && Alarm.PanicButton.equals(alarm.getAlarm())) {
                        reason = context.getString(R.string.posted_position_with_panicbutton, from);
                    }

                    Uri position = Uri.parse("geo:0,0?q=" + gps.getLatitude() + "," + gps.getLongitude() + " (" + from + ")");
                    Intent map = new Intent(android.content.Intent.ACTION_VIEW, position);

                    PendingIntent pi = PendingIntent.getActivity(context, 0, map, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon)
                            .setContentText(reason)
                            .setContentTitle(context.getText(R.string.app_name))
                            .setContentIntent(pi)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                            .build();

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(564341233 /* chiffre totalement au hasard */, notification);

                    success = true;
                } catch (Exception e) {
                    Log.e(Constants.NAME, "unable to retrieve the position", e);
                }
            }
        }

        return success;
    }
}
