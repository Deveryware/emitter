/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import java.io.IOException;

import android.os.Build;
import android.os.Environment;

public class TransportClient {

    public static final String LF = "\n";

    public static final String RESULT_EXTRA = "11" + LF;

    public final static String exec(String uri, String query) throws IOException
    {
        if (!uri.startsWith(TransportClientOverHTTPv2.PREFIX_HTTP) && !uri.startsWith(TransportClientOverHTTPv2.PREFIX_HTTPS)
                && !uri.startsWith(TransportClientOverSMS.PREFIX) && !uri.startsWith(TransportClientOverFile.PREFIX)
                && !uri.startsWith(TransportClientOverTCP.PREFIX)) {
            throw new IOException(uri + " is a wrong uri, so we do not try to upload this position");
        }

        if (uri.startsWith(TransportClientOverHTTPv2.PREFIX_HTTP) || uri.startsWith(TransportClientOverHTTPv2.PREFIX_HTTPS)) {
            return TransportClientOverHTTPv2.exec(uri, query);
        }

        if (uri.startsWith(TransportClientOverTCP.PREFIX)) {
            return TransportClientOverTCP.exec(uri.substring(6), query); // we remove tcp://
        }

        if (uri.startsWith(TransportClientOverSMS.PREFIX)) {
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion < 4 /* DONUT - Android 1.6 */) {
                return TransportClientOverSMSForSDK1.exec(uri.substring(6), query); // we remove sms://
            } else {
                return TransportClientOverSMS.exec(uri.substring(6), query); // we remove sms://
            }
        }

        if (uri.startsWith(TransportClientOverFile.PREFIX)) {
            final String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return TransportClientOverFile.exec(uri.substring(7), query); // we remove file://
            } else {
                throw new IOException("external data not available");
            }
        }

        return RESULT_EXTRA;
    }
}
