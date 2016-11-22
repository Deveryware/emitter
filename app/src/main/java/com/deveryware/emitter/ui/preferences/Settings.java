/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.ui.preferences;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.DwApplication;
import com.deveryware.emitter.R;
import com.deveryware.emitter.broadcast.Restart;
import com.deveryware.emitter.broadcast.StartOnce;
import com.deveryware.emitter.broadcast.StopOnce;
import com.deveryware.emitter.services.LockableSynchronizedService;
import com.deveryware.emitter.services.UploadQueriesService;
import com.deveryware.emitter.ui.geofencing.Geofencing;
import com.deveryware.emitter.ui.geofencing.StopGeofencing;
import com.deveryware.emitter.ui.histories.Histories;
import com.deveryware.emitter.widget.DisplayIconNotification;
import com.deveryware.emitter.widget.ManagePrivacyMode;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StatFs;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final int PROVIDER_NOT_CURRENTLY_ENABLED = 0;

    public static final int NEED_TO_RELOAD_MANUALLY = PROVIDER_NOT_CURRENTLY_ENABLED + 1;

    public static final int GPS_PROVIDER_IS_NEEDED = NEED_TO_RELOAD_MANUALLY + 1;

    public static final int COMMAND_BY_SMS = GPS_PROVIDER_IS_NEEDED + 1;

    public static final int ENERGY_CAUTION = COMMAND_BY_SMS + 1;

    public static final int DATABASE_DELETION = ENERGY_CAUTION + 1;

    public static final int CONFIRM_RESET = DATABASE_DELETION + 1;

    public static final int CHANGE_PASSWORD = CONFIRM_RESET + 1;

    public static final int ASK_PASSWORD = CHANGE_PASSWORD + 1;

    public static final String MESSAGE = "com.deveryware.emitter.SETTINGS";

    public static final int UUID = R.xml.settings;

    protected static final int PUSH = 0;

    protected static final int START = 1;

    private SharedPreferences pref;

    private File databaseFile;

    private volatile Looper looper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        addPreferencesFromResource(R.xml.settings);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> allProviders = locationManager.getAllProviders();
        ListPreference providers = (ListPreference) findPreference(Constants.PROVIDER);

        // Ajout du provider wifi de deveryware
        allProviders.add(Constants.DEVERYWARE_WIFI_PROVIDER);

        if (allProviders.contains(LocationManager.GPS_PROVIDER) && allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            allProviders.add(Constants.SEAMLESS_PROVIDER);
        }

        providers.setEntries(allProviders.toArray(new String[0]));
        providers.setEntryValues(allProviders.toArray(new String[0]));

        Preference version = (Preference) findPreference(Constants.VERSION_PREFERENCE);
        version.setSummary(Constants.VERSION);

        final HandlerThread thread = new HandlerThread("thread-" + Histories.class.getSimpleName(),
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        final Intent upload = new Intent(Settings.this, UploadQueriesService.class);
        upload.putExtra(UploadQueriesService.LIMIT, -1);
        looper = thread.getLooper();
        final Handler handler = new Handler(looper) {

            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                default:
                case START:
                    Settings.this.sendBroadcast(new Intent(Settings.this, StartOnce.class));
                    break;
                case PUSH:
                    LockableSynchronizedService.startService(Settings.this, upload);
                    break;
                }
            }
        };

        Preference start = (Preference) findPreference(Constants.START_PREFERENCE);
        start.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                final String text = getString(R.string.start_service_summary);
                Toast.makeText(Settings.this, text, Toast.LENGTH_LONG).show();
                return handler.sendEmptyMessage(START);
            }
        });

        Preference geofencing = (Preference) findPreference(Constants.START_ON_GEOFENCING);
        geofencing.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                startActivity(new Intent(Settings.this, Geofencing.class));
                return true;
            }
        });

        Preference synchro = (Preference) findPreference(Constants.SYNCHRO_PREFERENCE);
        synchro.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                final String text = getString(R.string.pushing_history);
                Toast.makeText(Settings.this, text, Toast.LENGTH_LONG).show();
                return handler.sendEmptyMessage(PUSH);
            }
        });

        Preference database = (Preference) findPreference(Constants.DATABASE_PREFERENCE);
        database.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                showDialog(DATABASE_DELETION);
                return true;
            }
        });
    }

    private String getAvailableMemories()
    {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(this, availableBlocks * blockSize);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        pref.registerOnSharedPreferenceChangeListener(this);

        Preference database = (Preference) findPreference(Constants.DATABASE_PREFERENCE);
        final SQLiteDatabase db = openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        databaseFile = new File(db.getPath());
        final String size = Formatter.formatFileSize(this, databaseFile.length());
        final String available = getAvailableMemories();
        database.setSummary(getString(R.string.database_size, db.getVersion(), size, available));
        if (db.isOpen()) {
            db.close();
        }

        if (pref.contains(Constants.ADMIN_PASSWORD)) {
            showDialog(ASK_PASSWORD);
        }

        Preference geofencing = (Preference) findPreference(Constants.START_ON_GEOFENCING);
        geofencing.setEnabled(!StopGeofencing.isStarted);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        pref.unregisterOnSharedPreferenceChangeListener(this);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        final boolean isEnabled = sharedPreferences.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE);
        final boolean isWakeup = sharedPreferences.getBoolean(Constants.WAKE_UP, Constants.DEFAULT_WAKE_UP);
        final boolean isCommandBySMS = sharedPreferences.getBoolean(Constants.COMMAND_BY_SMS, Constants.DEFAULT_COMMAND_BY_SMS);
        final boolean isGPS = sharedPreferences.getString(Constants.PROVIDER, Constants.DEFAULT_PROVIDER).equals(
                LocationManager.GPS_PROVIDER);
        final boolean isStartOnGPS = key.equals(Constants.START_ON_GPS)
                && sharedPreferences.getBoolean(Constants.START_ON_GPS, Constants.DEFAULT_START_ON_GPS);
        final boolean isForceAGPS = key.equals(Constants.FORCE_AGPS)
                && sharedPreferences.getBoolean(Constants.FORCE_AGPS, Constants.DEFAULT_FORCE_AGPS);

        if (key.equals(Constants.PROVIDER)) {
            final String provider = sharedPreferences.getString(Constants.PROVIDER, Constants.DEFAULT_PROVIDER);

            if (!provider.equals(Constants.DEVERYWARE_WIFI_PROVIDER) && !provider.equals(Constants.SEAMLESS_PROVIDER)) {
                final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(provider)) {
                    showDialog(PROVIDER_NOT_CURRENTLY_ENABLED);
                }
            }
        }

        if (key.equals(Constants.COMMAND_BY_SMS) && isCommandBySMS) {
            showDialog(COMMAND_BY_SMS);
        }

        if ((isStartOnGPS || isForceAGPS) && !isGPS) {
            showDialog(GPS_PROVIDER_IS_NEEDED);
        }

        if (key.equals(Constants.DISPLAY_ON_OFF)) {
            DisplayIconNotification.update(getApplicationContext());
        }

        if (key.equals(Constants.GEOLOC_ENABLE)) {
            ManagePrivacyMode.update(getApplicationContext());
        }

        if (key.equals(Constants.WAKE_UP) && !isWakeup && isEnabled) {
            final Intent stop = new Intent(this, StopOnce.class);
            sendBroadcast(stop);
        }

        if ((key.equals(Constants.MIN_TIME) || key.equals(Constants.WAKE_UP) || key.equals(Constants.ITERATION)) && isWakeup
                && isEnabled) {
            showDialog(NEED_TO_RELOAD_MANUALLY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.menu_clear_preferences:
            showDialog(CONFIRM_RESET);
            return true;
        case R.id.menu_admin_menu:
            showDialog(CHANGE_PASSWORD);
            return true;
        default:
            return false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        final View password = LayoutInflater.from(this).inflate(R.layout.password, null);
        final EditText field = (EditText) password.findViewById(R.id.password_field);
        switch (id) {
        case ASK_PASSWORD:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setView(password)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            final String currentPassword = pref.getString(Constants.ADMIN_PASSWORD, "");
                            if (currentPassword.equals(field.getText().toString())) {
                                dismissDialog(ASK_PASSWORD);
                            } else {
                                finish();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create();
        case CHANGE_PASSWORD:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setView(password)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            pref.edit().putString(Constants.ADMIN_PASSWORD, field.getText().toString()).commit();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.reset, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            pref.edit().remove(Constants.ADMIN_PASSWORD).commit();
                        }
                    })
                    .create();
        case CONFIRM_RESET:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setMessage(R.string.confirm_reset)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            pref.edit().clear().commit();
                            DwApplication.init(Settings.this);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        case DATABASE_DELETION:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setMessage(R.string.database_deletion)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            Toast.makeText(Settings.this, getText(R.string.deletion), Toast.LENGTH_LONG).show();
                            databaseFile.delete();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        case PROVIDER_NOT_CURRENTLY_ENABLED:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setMessage(R.string.provider_not_currently_enabled)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        case NEED_TO_RELOAD_MANUALLY:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setMessage(R.string.need_to_reload_manually)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            final Intent restart = new Intent(Settings.this, Restart.class);
                            sendBroadcast(restart);
                            Log.i(Constants.NAME, "preferences is changed, service is starting");
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        case GPS_PROVIDER_IS_NEEDED:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setMessage(R.string.gps_provider_is_needed)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        case COMMAND_BY_SMS:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setMessage(R.string.command_by_sms_caution)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        case ENERGY_CAUTION:
            return new AlertDialog.Builder(this).setTitle(R.string.app_name)
                    .setMessage(R.string.energy_caution)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        default:
            return super.onCreateDialog(id);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
