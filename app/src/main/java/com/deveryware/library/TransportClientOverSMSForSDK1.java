/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;

/**
 * needs android SDK 1 to 3
 * 
 * @author sylvek
 * 
 */
@SuppressWarnings("deprecation")
public class TransportClientOverSMSForSDK1 {

    private static final String PREFIX_GIFT = "GT ";

    public static final String PREFIX = "sms://";

    final static String exec(String uri, String query)
    {
        int prefixIndex = uri.indexOf("/");
        String prefix = "";
        String number = "";
        if (prefixIndex > -1) {
            prefix = uri.substring(0, prefixIndex) + " ";
            number = uri.substring(prefixIndex + 1);
        } else {
            number = uri;
        }

        final SmsManager smsManager = SmsManager.getDefault();
        final ArrayList<String> messages = smsManager.divideMessage(prefix + PREFIX_GIFT + query);
        // TODO: use sentIntent and deliveyIntent
        smsManager.sendMultipartTextMessage(number, null, messages, null, null);
        return TransportClient.RESULT_EXTRA;
    }

    final public static boolean receiveSMS(Context context, Intent intent)
    {
        final Bundle bundle = intent.getExtras();
        final Object[] pdusObj = (Object[]) bundle.get("pdus");

        boolean success = false;

        for (int i = 0; i < pdusObj.length; i++) {
            final SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
            final String from = currentMessage.getDisplayOriginatingAddress();
            final String command = currentMessage.getMessageBody();
            /* nécessité de remplacer les \\n en \n car l'opérateur SMS échappe les caractères spéciaux */
            final Intent[] commands = GiftClient.extractCommands(command.replace("\\n", "\n"));

            for (Intent c : commands) {
                context.sendBroadcast(c);
            }

            /**
             * redmine #491 : si la seule action que nous ayons est le résultat d'une demande de dernière position, alors nous ne
             * notifions pas l'utilisateur.
             */
            if (commands.length > 0 && !(commands.length == 1 && Constants.LASTPOS_ACTION.equals(commands[0].getAction()))) {
                final Intent notification = new Intent(Constants.MESSAGE);
                notification.putExtra(Constants.FROM, from);
                context.sendBroadcast(notification);
            }

            success = commands.length > 0;
        }

        return success;
    }
}
