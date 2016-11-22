/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.util.Log;

public class TransportClientOverHTTPv2 {

    private static final int TIMEOUT = 3000;

    private static final String USER_AGENT = "User-Agent";

    public static final String PREFIX_HTTP = "http://";

    public static final String PREFIX_HTTPS = "https://";

    final static String exec(String uri, String query) throws IOException
    {
        URL url = new URL(uri);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setRequestProperty(USER_AGENT, Constants.LIBRARY_VERSION);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(("p=" + URLEncoder.encode(query, "UTF-8")).getBytes());
            out.flush();
            out.close();

            Log.d(Constants.NAME, "uri:" + uri);
            Log.d(Constants.NAME, "p:" + query);

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int read = 0;
                int bufSize = 512;
                byte[] buffer = new byte[bufSize];
                while (true) {
                    read = in.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    baf.append(buffer, 0, read);
                }
                in.close();
                final String result = new String(baf.toByteArray());
                Log.d(Constants.NAME, "result:" + result);
                return result;
            }

            throw new IOException("bad http response:" + urlConnection.getResponseMessage());
        } finally {
            urlConnection.disconnect();
        }

    }
}
