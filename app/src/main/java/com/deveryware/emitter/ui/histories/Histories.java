/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.ui.histories;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.provider.EmitterProvider;
import com.deveryware.emitter.services.LockableSynchronizedService;
import com.deveryware.emitter.services.UploadQueriesService;
import com.deveryware.gift.data.Query;

import java.text.ParseException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class Histories extends ListActivity implements OnItemLongClickListener {

    public static final int UUID = R.layout.history;

    static final int PLEASE_WAIT = 1;

    static final int DISPLAY_CHOICES = 2;

    static final int CONFIRM_DELETION = 3;

    private Cursor c;

    private Intent upload;

    private volatile Looper looper;

    private volatile ServiceHandler executorService;

    private long longClickedId;

    private int lastPosition;

    private final class ServiceHandler extends Handler {

        public static final int DELETE = 0;

        public static final int PUSH = 1;

        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
            default:
                break;
            case ServiceHandler.PUSH:
                LockableSynchronizedService.startService(Histories.this, upload);
                break;
            case ServiceHandler.DELETE:
                final int result = getContentResolver().delete(EmitterProvider.CONTENT_URI, null, null);
                Histories.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run()
                    {
                        sendBroadcast(new Intent(Constants.HISTORY_NEEDS_REFRESH));
                        Histories.this.removeDialog(PLEASE_WAIT);
                        String text = Histories.this.getString(R.string.deleted, result);
                        Toast.makeText(Histories.this, text, Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
        }
    }

    final BroadcastReceiver dbUpdated = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            if (c != null && !c.isClosed()) {
                c.requery();
            }
        }
    };

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig)
    {
        try {
            super.onConfigurationChanged(newConfig);
            setContentView(R.layout.history);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        final HandlerThread thread = new HandlerThread("thread-" + Histories.class.getSimpleName(),
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        looper = thread.getLooper();
        executorService = new ServiceHandler(looper);

        upload = new Intent(Histories.this, UploadQueriesService.class);
        upload.putExtra(UploadQueriesService.LIMIT, -1);

        getListView().setOnItemLongClickListener(this);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        getListView().setSmoothScrollbarEnabled(true);
        getListView().setFastScrollEnabled(true);

        try {
            c = managedQuery(EmitterProvider.CONTENT_URI, new String[] { EmitterProvider.ID, EmitterProvider.TIME,
                    EmitterProvider.RESULT, EmitterProvider.UPLOADED, EmitterProvider.TRYLATER, EmitterProvider.QUERY,
                    EmitterProvider.ELAPSED_TIME }, null, null, "time DESC");
            setListAdapter(new HistoryAdapter(this, R.layout.history_adapter, c));
        } catch (Exception e) {
            Log.e(Constants.NAME, "unable to query database", e);
            Toast.makeText(this, getText(R.string.error_during_opening_histories), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state)
    {
        lastPosition = state.getInt("lastPosition");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt("lastPosition", lastPosition);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try {
            Histories.this.getListView().setSelectionFromTop(lastPosition, 10);
            registerReceiver(dbUpdated, new IntentFilter(Constants.HISTORY_NEEDS_REFRESH));
        } catch (SecurityException e) {
            Log.w(Constants.NAME, "As of ICE_CREAM_SANDWICH, receivers registered with this method will correctly respect ...");
        }
    }

    @Override
    protected void onPause()
    {
        try {
            unregisterReceiver(dbUpdated);
        } catch (IllegalArgumentException e) {
            Log.e(Constants.NAME, "out of memory detected ? " + e.getMessage());
        }

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        if (looper.getThread().isAlive()) {
            looper.quit();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_histories, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        final boolean enabled = getListAdapter().getCount() > 0;
        menu.getItem(0).setEnabled(enabled);
        menu.getItem(1).setEnabled(enabled);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.menu_clear_history:
            showDialog(CONFIRM_DELETION);
            return true;
        case R.id.menu_push_history:
            final String text = getString(R.string.pushing_history);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            return executorService.sendEmptyMessage(ServiceHandler.PUSH);
        default:
            return false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id) {
        default:
        case DISPLAY_CHOICES:
            final CharSequence watch = getText(R.string.watch_element);
            final CharSequence delete = getText(R.string.delete_element);
            final CharSequence reset = getText(R.string.reset_element);
            final CharSequence[] items = { reset, delete, watch };
            return new AlertDialog.Builder(this).setTitle(R.string.history)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            final ContentResolver cr = getContentResolver();
                            final Uri uri = ContentUris.withAppendedId(EmitterProvider.CONTENT_URI, longClickedId);
                            switch (item) {
                            case 2:
                                final String query = HistoryView.getQuery(Histories.this, uri);
                                try {
                                    final Query q = HistoryView.GIFT.parse(query);
                                    HistoryView.startMap(Histories.this, q);
                                } catch (ParseException e) {
                                    Toast.makeText(Histories.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 1:
                                final CharSequence deletion = getText(R.string.deletion);
                                Toast.makeText(Histories.this, deletion, Toast.LENGTH_SHORT).show();
                                cr.delete(uri, EmitterProvider.ID + "=?", new String[] { Long.toString(longClickedId) });
                                c.requery();
                                break;
                            case 0:
                                final CharSequence reseted = getText(R.string.reseted);
                                Toast.makeText(Histories.this, reseted, Toast.LENGTH_SHORT).show();
                                final ContentValues values = new ContentValues();
                                values.put(EmitterProvider.TRYLATER, true);
                                values.putNull(EmitterProvider.RESULT);
                                values.putNull(EmitterProvider.UPLOADED);
                                cr.update(uri, values, EmitterProvider.ID + "=?", new String[] { Long.toString(longClickedId) });
                                c.requery();
                                break;
                            default:
                                break;
                            }
                        }
                    })
                    .create();
        case PLEASE_WAIT:
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle(R.string.app_name);
            progress.setMessage(getString(R.string.please_wait));
            progress.setCancelable(false);
            progress.setIndeterminate(true);
            return progress;
        case CONFIRM_DELETION:
            return new AlertDialog.Builder(this).setTitle(R.string.history)
                    .setMessage(getString(R.string.confirm_deletion))
                    .setPositiveButton(android.R.string.yes, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            showDialog(PLEASE_WAIT);
                            executorService.sendEmptyMessage(ServiceHandler.DELETE);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        this.longClickedId = id;
        showDialog(DISPLAY_CHOICES);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        lastPosition = position;
        final Uri uri = ContentUris.withAppendedId(EmitterProvider.CONTENT_URI, id);
        final Intent intent = new Intent(Histories.this, HistoryView.class);
        intent.setData(uri);
        startActivity(intent);
    }
}
