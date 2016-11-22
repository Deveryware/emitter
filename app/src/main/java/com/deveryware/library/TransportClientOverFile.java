/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Environment;

public class TransportClientOverFile {

    public static final String PREFIX = "file://";
    
    private static final String FOLDER = "/Android/data/com.deveryware.emitter/files";

    final static String exec(String uri, String query) throws IOException
    {
        final File storage = Environment.getExternalStorageDirectory();
        final File appDirectory = new File(storage.getAbsolutePath() + FOLDER);
        final File log = new File(appDirectory, uri);

        if (!appDirectory.exists()) {
            appDirectory.mkdirs();
        }

        if (!appDirectory.isDirectory()) {
            if (appDirectory.delete()) {
                appDirectory.mkdirs();
            }
        }

        final PrintWriter pw = new PrintWriter(new FileWriter(log, true));
        pw.println(query);
        pw.flush();
        pw.close();
        return TransportClient.RESULT_EXTRA;
    }
}
