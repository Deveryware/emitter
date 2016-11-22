/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import java.io.IOException;
import java.net.Socket;

import android.util.Log;

public class TransportClientOverTCP {

    public static final String PREFIX = "tcp://";

    private static Socket socket;

    final static String exec(String uri, String query) throws IOException
    {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            Log.d(Constants.NAME, "socket not connected, we are trying to connect.");
            try {
                final int indexofdoubledots = uri.indexOf(":");
                final String host = uri.substring(0, indexofdoubledots);
                final int port = Integer.valueOf(uri.substring(indexofdoubledots + 1));
                TransportClientOverTCP.socket = new Socket(host, port);
            } catch (NumberFormatException e) {
                throw new IOException(uri + " is not valid. the port number is incorrect");

            } catch (IndexOutOfBoundsException e) {
                throw new IOException(uri + " is not valid. tcp://host:port needed");
            }
        }

        if (TransportClientOverTCP.socket.isConnected()) {
            try {
                TransportClientOverTCP.socket.getOutputStream().write(query.getBytes());
                byte[] buffer = new byte[1024];
                int readBytes = TransportClientOverTCP.socket.getInputStream().read(buffer, 0, 1024);
                return new String(buffer, 0, readBytes, "UTF-8");
            } catch (IOException e) {
                TransportClientOverTCP.socket = null;
                throw e;
            }
        }

        return TransportClient.RESULT_EXTRA;
    }
}
