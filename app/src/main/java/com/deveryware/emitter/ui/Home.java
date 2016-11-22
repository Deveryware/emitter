/**
 *
 * Copyright (C) 2012 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 28 janv. 2012
 *
 */
package com.deveryware.emitter.ui;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.broadcast.AlarmOnce;
import com.deveryware.emitter.broadcast.StartOnce;
import com.deveryware.emitter.ui.histories.Histories;
import com.deveryware.emitter.ui.preferences.Settings;
import com.deveryware.emitter.widget.ManagePrivacyMode;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author sylvek
 * 
 */
public class Home extends ActionBarActivity implements OnItemSelectedListener, OnSeekBarChangeListener {

    private SharedPreferences pref;

    private TextView statusText, idText, provider, release, timer_text;

    private Spinner dest;

    private SeekBar timer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        statusText = (TextView) findViewById(R.id.status);
        idText = (TextView) findViewById(R.id.identifiant);
        provider = (TextView) findViewById(R.id.provider);
        release = (TextView) findViewById(R.id.version);
        dest = (Spinner) findViewById(R.id.destination);
        timer = (SeekBar) findViewById(R.id.timer);
        timer_text = (TextView) findViewById(R.id.timer_text);

        final ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.dest_keys_array,
                android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dest.setAdapter(adapter1);
        dest.setOnItemSelectedListener(this);

        timer.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        idText.setText(pref.getString(Constants.IDENTITY_KEY, ""));
        release.setText(String.valueOf(Constants.APPLICATION_VERSION));

        udpateStatus(pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE));
        updateDestination(pref.getString(Constants.URL, getString(R.string.default_url)));
        updateTimer(pref.getBoolean(Constants.WAKE_UP, Constants.DEFAULT_WAKE_UP),
                pref.getLong(Constants.MIN_TIME, Constants.DEFAULT_MIN_TIME));

        final String p = pref.getString(Constants.PROVIDER, Constants.DEFAULT_PROVIDER);
        if (LocationManager.GPS_PROVIDER.equals(p) || Constants.SEAMLESS_PROVIDER.equals(p)) {
            if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, R.string.gps_provider_is_needed, Toast.LENGTH_LONG).show();
            }
        }

        if (LocationManager.NETWORK_PROVIDER.equals(p)) {
            provider.setText("Google");
        } else if (Constants.DEVERYWARE_WIFI_PROVIDER.equals(p)) {
            provider.setText("WI-FI + CELL-ID");
        } else if (Constants.SEAMLESS_PROVIDER.equals(p)) {
            provider.setText("GPS + Google");
        } else {
            provider.setText(p);
        }
    }

    private void updateTimer(boolean wakeup, long mintime)
    {
        if (wakeup) {
            if (mintime <= 60) {
                timer.setProgress(1);
                return;
            }
            if (mintime <= 300) {
                timer.setProgress(2);
                return;
            }
            if (mintime <= 900) {
                timer.setProgress(3);
                return;
            }
            if (mintime <= 1800) {
                timer.setProgress(4);
                return;
            }
            if (mintime <= 3600) {
                timer.setProgress(5);
                return;
            }
        }
    }

    private void updateDestination(String dest2)
    {
        final String[] dests = getResources().getStringArray(R.array.dest_values_array);
        for (int i = 0; i < dests.length; i++) {
            if (dests[i].equals(dest2)) {
                dest.setSelection(i);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        final MenuItem item = menu.getItem(0);
        final boolean status = pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE);
        menu.getItem(1).setEnabled(status);
        menu.getItem(2).setEnabled(status);
        if (status) {
            item.setIcon(R.drawable.icon_enabled);
            item.setTitle(R.string.disable_service);
        } else {
            item.setIcon(R.drawable.icon_disabled);
            item.setTitle(R.string.enable_service);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.menu_service:
            udpateStatus(!pref.getBoolean(Constants.GEOLOC_ENABLE, Constants.DEFAULT_GEOLOC_ENABLE));
            sendBroadcast(new Intent(this, ManagePrivacyMode.class));
            return true;
        case R.id.menu_panic_button:
            final Intent panicButton = new Intent(this, AlarmOnce.class);
            panicButton.putExtra(AlarmOnce.ALARM_CODE, "16");
            sendBroadcast(panicButton);
            return true;
        case R.id.menu_start_now:
            sendBroadcast(new Intent(this, StartOnce.class));
            return true;
        case R.id.menu_history:
            startActivity(new Intent(this, Histories.class));
            return true;
        case R.id.menu_settings:
            startActivity(new Intent(this, Settings.class));
            return true;
        default:
            return false;
        }
    }

    private void udpateStatus(final boolean status)
    {
        if (status) {
            statusText.setText(R.string.status_enabled);
        } else {
            statusText.setText(R.string.status_disabled);
        }

        dest.setEnabled(!status);
        timer.setEnabled(!status);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        final String dest = getResources().getStringArray(R.array.dest_values_array)[arg2];
        pref.edit().putString(Constants.URL, dest).commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        updateTimerText(seekBar.getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }

    private void updateTimerText(final int progress)
    {
        boolean wakeup = true;
        int mintime = 0;
        switch (progress) {
        default:
        case 0:
            wakeup = false;
            timer_text.setText("");
            break;
        case 1:
            mintime = 60;
            timer_text.setText(getString(R.string.timer_repetition, getString(R.string.timer_minute)));
            break;
        case 2:
            mintime = 300;
            timer_text.setText(getString(R.string.timer_repetition, "5 " + getString(R.string.timer_minute)));
            break;
        case 3:
            mintime = 900;
            timer_text.setText(getString(R.string.timer_repetition, "15 " + getString(R.string.timer_minute)));
            break;
        case 4:
            mintime = 1800;
            timer_text.setText(getString(R.string.timer_repetition, "30 " + getString(R.string.timer_minute)));
            break;
        case 5:
            mintime = 3600;
            timer_text.setText(getString(R.string.timer_repetition, getString(R.string.timer_hour)));
            break;
        }
        pref.edit()
                .putBoolean(Constants.FORCE_SEND, wakeup)
                .putBoolean(Constants.WAKE_UP, wakeup)
                .putLong(Constants.MIN_TIME, mintime)
                .putInt(Constants.ITERATION, -1)
                .putInt(Constants.TIMEOUT, Math.min((mintime / 2), 120))
                .putBoolean(Constants.START_ON_BOOT, true)
                .putBoolean(Constants.START_ON_GEOLOC_ENABLE, true)
                .commit();
    }
}
