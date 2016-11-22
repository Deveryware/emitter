/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.services;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.provider.EmitterProvider;
import com.deveryware.library.GiftClient;
import com.deveryware.library.TransportClient;
import com.deveryware.library.TransportClientOverFile;
import com.deveryware.library.TransportClientOverHTTP;
import com.deveryware.library.TransportClientOverSMS;
import com.deveryware.library.TransportClientOverTCP;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

public class UploadQueriesService extends LockableSynchronizedService {

    private static final String TO_UPLOAD = "1";

    private static final String QUERY = "query";

    private static final int DEFAULT_LIMIT = 10;

    public static final String LIMIT = "limit";

    private ConnectivityManager connectivityManager;

    private SharedPreferences pref;

    private boolean isSMS, isFile, isHttp, isTcp;

    private String uri;

    private boolean isDeleteAfterUpload;

    private SQLiteDatabase db;

    private volatile Looper looper;

    private volatile ServiceHandler executorService;

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            String limit = "";
            if (msg.what > 0) {
                limit = String.valueOf(msg.what);
            }
            Cursor c = null;
            try {
                if (db.isOpen()) {
                    c = db.query(Constants.TABLE_NAME, new String[] { EmitterProvider.ID, EmitterProvider.QUERY },
                            EmitterProvider.TRYLATER + "=?", new String[] { TO_UPLOAD }, null, null, null, limit);
                    if (!c.moveToFirst()) {
                        Log.w(Constants.NAME, "upload queries service have nothing to do");
                    } else {
                        do {
                            int i = c.getColumnIndex(EmitterProvider.ID);
                            int q = c.getColumnIndex(EmitterProvider.QUERY);
                            try {
                                String query = c.getString(q);
                                String result = TransportClient.exec(uri, query);
                                String id = c.getString(i);

                                if (isDeleteAfterUpload) {
                                    Log.i(Constants.NAME,
                                            "com.deveryware.delete_after_upload is enabled, so we delete the entry.");
                                    db.delete(Constants.TABLE_NAME, EmitterProvider.ID + "=?", new String[] { id });
                                } else {
                                    ContentValues values = new ContentValues();
                                    values.put(EmitterProvider.TRYLATER, false);
                                    values.put(EmitterProvider.RESULT, result);
                                    values.put(EmitterProvider.UPLOADED, System.currentTimeMillis());
                                    db.update(Constants.TABLE_NAME, values, EmitterProvider.ID + "=?", new String[] { id });
                                }

                                Intent[] commands = GiftClient.extractCommands(result);
                                for (Intent command : commands) {
                                    sendBroadcast(command);
                                }

                                Intent queryUpdated = new Intent(Constants.QUERY_UPLOADED);
                                queryUpdated.putExtra(QUERY, id);
                                sendBroadcast(queryUpdated);

                                /**
                                 * redmine #491 : si la seule action que nous ayons est le résultat d'une demande de dernière
                                 * position, alors nous ne notifions pas l'utilisateur.
                                 */
                                if (commands.length > 0
                                        && !(commands.length == 1 && Constants.LASTPOS_ACTION.equals(commands[0].getAction()))) {
                                    Intent commandsReceived = new Intent(Constants.MESSAGE);
                                    commandsReceived.putExtra(Constants.FROM, getString(R.string.from_server));
                                    sendBroadcast(commandsReceived);
                                }
                            } catch (IOException e) {
                                Log.e(Constants.NAME, "upload queries service IOException: " + e.getMessage());
                                break;
                            } catch (IllegalArgumentException e) {
                                Log.e(Constants.NAME, "upload queries service IllegalArgumentException: " + e.getMessage());
                                break;
                            }
                        } while (c.moveToNext());
                    }
                }
            } catch (NullPointerException e) {
                /* ceci est arriv� sur le HTC HD d'Alain sur le db.query :-o */
                Log.e(Constants.NAME, "db.query retourne une NPE");
            } catch (IllegalStateException e) {
                /* ceci arrive quand on est en mode patate ;-) */
                Log.e(Constants.NAME, e.getMessage());
            } catch (SQLiteException e) {
                Log.e(Constants.NAME, "database not yet available");
            } finally {
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            }

            // send broadcast to refresh history activity if it's opened
            sendBroadcast(new Intent(Constants.HISTORY_NEEDS_REFRESH));

            stop();
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        isDeleteAfterUpload = pref.getBoolean(Constants.DELETE_AFTER_UPLOAD, Constants.DEFAULT_DELETE_AFTER_UPLOAD);
        uri = pref.getString(Constants.URL, getString(R.string.default_url));
        isSMS = uri.startsWith(TransportClientOverSMS.PREFIX);
        isFile = uri.startsWith(TransportClientOverFile.PREFIX);
        isHttp = uri.startsWith(TransportClientOverHTTP.PREFIX_HTTP) || uri.startsWith(TransportClientOverHTTP.PREFIX_HTTPS);
        isTcp = uri.startsWith(TransportClientOverTCP.PREFIX);
        db = openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);

        final HandlerThread thread = new HandlerThread("thread-" + UpdateLocationService.class.getSimpleName(),
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        looper = thread.getLooper();
        executorService = new ServiceHandler(looper);
    }

    @Override
    public void onStartSynchronized(Intent intent, int startId)
    {
        Log.i(Constants.NAME, "upload queries service is started");

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = info != null && info.isConnected();

        if (((isHttp || isTcp) && isConnected) || isSMS || isFile) {
            executorService.sendEmptyMessage(intent.getIntExtra(LIMIT, DEFAULT_LIMIT));
        } else {
            Log.w(Constants.NAME, "media storage is not available, we will try later.");
            Log.d(Constants.NAME, "uri = " + this.uri);
            stop();
        }
    }

    @Override
    public void onDestroy()
    {
        looper.quit();

        if (db.isOpen()) {
            db.close();
        }

        super.onDestroy();
        Log.i(Constants.NAME, "upload queries service is stopped");
    }

    @Override
    protected boolean isForce(Intent intent)
    {
        // force is disallow for this service.
        return false;
    }
}
