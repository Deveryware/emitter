/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

@Deprecated
public class TransportClientOverHTTP {

    public static final String PREFIX_HTTP = "http://";

    public static final String PREFIX_HTTPS = "https://";

    private static HttpParams params = new BasicHttpParams();

    final static String exec(String uri, String query) throws IOException
    {
        HttpProtocolParams.setUserAgent(params, Constants.LIBRARY_VERSION);
        HttpClient client = new DefaultHttpClient(params);
        HttpPost post = new HttpPost(uri);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

        parameters.add(new BasicNameValuePair("p", query));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        if (Log.isLoggable(Constants.NAME, Log.DEBUG)) {
            Log.d(Constants.NAME, "uri:" + post.getURI().toString());
            Log.d(Constants.NAME, "p:" + query);
        }

        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() == 200) {
            final String result = EntityUtils.toString(response.getEntity());
            if (Log.isLoggable(Constants.NAME, Log.DEBUG)) {
                Log.d(Constants.NAME, "result:" + result);
            }
            return result;
        }

        throw new IOException("bad http response:" + response.getStatusLine().getReasonPhrase());
    }
}
