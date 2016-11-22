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
import com.deveryware.emitter.broadcast.UploadOnce;
import com.deveryware.emitter.provider.EmitterProvider;
import com.deveryware.emitter.widget.WidgetProvider;
import com.deveryware.library.AlarmParameter;
import com.deveryware.library.GiftClient;
import com.deveryware.library.GiftClient.RequestInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class UpdateLocationService extends LockableSynchronizedService {

    private static volatile boolean isStarted;

    public static final String CONDITION = "condition";

    public static final String PID = "pid";

    public static final String POSITION_OPTIONAL = "location_optional";

    private LocationManager locationManager;

    private ConnectivityManager connectivityManager;

    private TelephonyManager telephonyManager;

    private WifiManager wifiManager;

    private SharedPreferences pref;

    private WifiLock wifiLock;

    final GiftClient.CellIdInfo cellIdInfo = new GiftClient.CellIdInfo();

    final GiftClient.PhoneInfo phoneInfo = new GiftClient.PhoneInfo();

    final GiftClient.LocationInfo locationInfo = new GiftClient.LocationInfo();

    final GiftClient.WifiInfo wifiInfo = new GiftClient.WifiInfo();

    final GiftClient.IpInfo ipInfo = new GiftClient.IpInfo();

    final Handler timer = new Handler();

    final List<ForceSendMessage> forceSendMessages = Collections.synchronizedList(new ArrayList<UpdateLocationService.ForceSendMessage>());

    class ForceSendMessage implements Runnable {

        private final RequestInfo requestInfo;

        ForceSendMessage(final RequestInfo requestInfo)
        {
            this.requestInfo = requestInfo;
        }

        @Override
        public void run()
        {
            synchronized (forceSendMessages) {
                timer.removeCallbacks(this);
                if (forceSendMessages.remove(this)) {
                    Log.i(Constants.NAME, "not yet position, we send the message anyway.");
                    UpdateLocationService.this.uploadLocation(null, this.requestInfo);
                }
            }
        }
    };

    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength)
        {
            synchronized (UpdateLocationService.this) {
                Log.d(Constants.NAME, "signal strength is available");
                UpdateLocationService.this.cellIdInfo.signalStrength = signalStrength.getGsmSignalStrength();
            }
        };

    };

    private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int arg0)
        {
            if (GpsStatus.GPS_EVENT_SATELLITE_STATUS == arg0) {
                /*
                 * don't log this event, it's a cyclic call and trash your traces.
                 */
                synchronized (UpdateLocationService.this) {
                    UpdateLocationService.this.locationInfo.gpsStatus = locationManager.getGpsStatus(UpdateLocationService.this.locationInfo.gpsStatus);
                }
            }
        }
    };

    private final BroadcastReceiver batteryLevel = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            synchronized (this) {
                Log.d(Constants.NAME, "battery infos are available");

                phoneInfo.level = arg1.getIntExtra("level", 0);
                phoneInfo.scale = arg1.getIntExtra("scale", 100);
                phoneInfo.plugged = arg1.getIntExtra("plugged", 0);
                phoneInfo.status = arg1.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
            }
        }
    };

    private List<ScanResultsAvailable> scansResultsAvailable = Collections.synchronizedList(new ArrayList<UpdateLocationService.ScanResultsAvailable>());

    class ScanResultsAvailable extends BroadcastReceiver {

        private final RequestInfo requestInfo;

        ScanResultsAvailable(final RequestInfo requestInfo)
        {
            this.requestInfo = requestInfo;
        }

        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            synchronized (UpdateLocationService.this) {
                Log.d(Constants.NAME, "hotspot wifis are available");
                this.requestInfo.isWifiAvailable = true;

                synchronized (scansResultsAvailable) {
                    try {
                        unregisterReceiver(this);
                    } catch (IllegalArgumentException e) {
                        // do nothing
                    } finally {
                        scansResultsAvailable.remove(this);
                    }
                }

                String provider = pref.getString(Constants.PROVIDER, Constants.DEFAULT_PROVIDER);
                if (provider.equals(Constants.DEVERYWARE_WIFI_PROVIDER)) {
                    UpdateLocationService.this.uploadLocation(null, this.requestInfo);
                }
            }
        }
    };

    private List<RequestLocationUpdates> requestsLocationUpdates = Collections.synchronizedList(new ArrayList<UpdateLocationService.RequestLocationUpdates>());

    private class RequestLocationUpdates implements LocationListener {

        private final RequestInfo requestInfo;

        private final ForceSendMessage forceSendMessage;

        public RequestLocationUpdates(final RequestInfo requestInfo, final ForceSendMessage forceSendMessage)
        {
            this.requestInfo = requestInfo;
            this.forceSendMessage = forceSendMessage;
        }

        @Override
        public void onLocationChanged(Location location)
        {
            if (locationInfo.isSeamless && location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                Log.d(Constants.NAME, "seamless mode, default location: " + location.toString());
                this.requestInfo.defaultLocation = location;
                return;
            }

            // retry to have best values
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                if (this.requestInfo.retry-- > 0) {
                    Log.d(Constants.NAME, "GPS provider, retry: " + this.requestInfo.retry);
                    Log.d(Constants.NAME, "location: " + location.toString());
                    return;
                }
            }

            synchronized (forceSendMessages) {
                final boolean forcePosition = requestInfo.isLocationOptional;
                if (forceSendMessages.remove(forceSendMessage) || !forcePosition) {
                    if (!forcePosition) {
                        Log.d(Constants.NAME, "position is not optional");
                    }
                    synchronized (requestsLocationUpdates) {
                        locationManager.removeUpdates(this);
                        if (requestsLocationUpdates.remove(this)) {
                            uploadLocation(location, requestInfo);
                        }
                    }
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.w(Constants.NAME, provider + " is disabled. We stop the service.");
            stop();
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.w(Constants.NAME, provider + " is enabled. Strange...");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.w(Constants.NAME, provider + " have status changed.");
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, Constants.NAME + ".wifi");
        wifiLock.setReferenceCounted(false);
    }

    @Override
    protected void stop()
    {
        isStarted = false;
        super.stop();
    }

    @Override
    public void onStartSynchronized(Intent intent, int startId)
    {
        Log.i(Constants.NAME, "update location service is started");
        isStarted = true;

        boolean isWifi = pref.getBoolean(Constants.UPLOAD_WIFI, Constants.DEFAULT_UPLOAD_WIFI);
        boolean isBattery = pref.getBoolean(Constants.BATTERY_LEVEL, Constants.DEFAULT_BATTERY_LEVEL);
        boolean isSignal = pref.getBoolean(Constants.TELEPHONY_SIGNAL, Constants.DEFAULT_TELEPHONY_SIGNAL);
        boolean isVersion = pref.getBoolean(Constants.DISPLAY_VERSION, Constants.DEFAULT_DISPLAY_VERSION);
        boolean isReturnLastPos = pref.getBoolean(Constants.RETURN_LAST_POS, Constants.DEFAULT_RETURN_LAST_POS);

        String provider = pref.getString(Constants.PROVIDER, Constants.DEFAULT_PROVIDER);

        boolean locationOptionalDefault = pref.getBoolean(Constants.FORCE_SEND, Constants.DEFAULT_FORCE_SEND);
        long delay = pref.getInt(Constants.TIMEOUT, Constants.DEFAULT_TIMEOUT) * 1000L;

        final RequestInfo requestInfo = new RequestInfo();
        requestInfo.startedAt = SystemClock.elapsedRealtime();
        requestInfo.pid = intent.getStringExtra(PID);
        requestInfo.alarmParameter = (AlarmParameter) intent.getParcelableExtra(CONDITION);
        requestInfo.cmdid = intent.getStringExtra(GiftClient.CMD_ID);
        requestInfo.cmdack = intent.getIntExtra(GiftClient.CMD_ACK, 0);
        requestInfo.cmdtext = intent.getStringExtra(GiftClient.CMD_TEXT);
        requestInfo.text1 = intent.getStringExtra(GiftClient.INPUT_TEXT1);
        requestInfo.text2 = intent.getStringExtra(GiftClient.INPUT_TEXT2);
        requestInfo.apc = intent.getBooleanExtra(GiftClient.APC, true);
        requestInfo.isLocationOptional = intent.getBooleanExtra(POSITION_OPTIONAL, locationOptionalDefault);

        // we keep the user value
        if (requestInfo.pid == null) {
            requestInfo.pid = pref.getString(Constants.IDENTITY_KEY, null);
        }

        requestInfo.displayVersion = isVersion;
        requestInfo.returnLastPos = isReturnLastPos;
        requestInfo.radius = true;

        locationInfo.isSeamless = provider.equals(Constants.SEAMLESS_PROVIDER);
        locationInfo.isDeveryware = provider.equals(Constants.DEVERYWARE_WIFI_PROVIDER);
        locationInfo.provider = provider;

        if (locationInfo.isDeveryware || isWifi) {
            /* the wifi is currently enabled? */
            boolean isEnabled = wifiManager.isWifiEnabled();

            if (!isEnabled) {
                /* we enable the wifi only if it is necessary */
                if (wifiManager.setWifiEnabled(true)) {
                    int tryAgain = 20; // 4s max, ANR displayed after 5s
                    while (!isEnabled && tryAgain > 0) {
                        try {
                            isEnabled = wifiManager.isWifiEnabled();
                            Log.d(Constants.NAME, "wifi no wet available, we wait 200ms");
                            Thread.sleep(200L); // 200ms
                            tryAgain--;
                        } catch (InterruptedException e) {
                            isEnabled = false;
                            break;
                        }
                    }
                }
            }

            if (isEnabled) {
                Log.i(Constants.NAME, "listening wifi is enabled");
                final ScanResultsAvailable scanResultsAvailable = new ScanResultsAvailable(requestInfo);
                scansResultsAvailable.add(scanResultsAvailable);
                registerReceiver(scanResultsAvailable, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                try {
                    wifiLock.acquire();
                } catch (final UnsupportedOperationException e) {
                    // problème remonté par le HTC Desire HD
                    isWifi = false;
                }
            }
        }

        if (isSignal) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } else {
            cellIdInfo.signalStrength = -1;
        }

        if (isBattery) {
            /* request the level battery */
            registerReceiver(batteryLevel, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } else {
            phoneInfo.level = -1;
        }

        /* if GPS provider */
        if (provider.equals(LocationManager.GPS_PROVIDER) || provider.equals(Constants.SEAMLESS_PROVIDER)) {
            /* we try to catch GpsStatus */
            locationManager.addGpsStatusListener(gpsStatusListener);

            /* we update AGPS if needed */
            boolean forceAGPS = pref.getBoolean(Constants.FORCE_AGPS, Constants.DEFAULT_FORCE_AGPS);
            if (forceAGPS) {
                Log.d(Constants.NAME, "A-GPS update is forced");
                final Bundle bundle = new Bundle();
                locationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_xtra_injection", bundle);
                locationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_time_injection", bundle);
            }
        }

        /* update the widget if it was called from it */
        WidgetProvider.startService(this);

        /* we request the location */
        requestLocation(isWifi, delay, requestInfo);
    }

    private void requestLocation(boolean isWifi, long delay, RequestInfo requestInfo)
    {
        Log.i(Constants.NAME, "location is requested");
        sendBroadcast(new Intent(Constants.LOCATION_REQUESTED));

        if (locationInfo.isDeveryware || isWifi) {
            /* we enable the wifi only if it is necessary */
            if (wifiManager.isWifiEnabled() && wifiManager.startScan()) {
                Log.i(Constants.NAME, "start scanning wifi ...");
            } else {
                Log.e(Constants.NAME, "unable to start the scan. please choose an another provider or disable capture wifi.");
                if (locationInfo.isDeveryware) {
                    stop();
                }
            }
        }

        boolean stopIt = true;
        if (!locationInfo.isDeveryware) {
            final ForceSendMessage forceSendMessage = new ForceSendMessage(requestInfo);
            final RequestLocationUpdates requestLocationUpdates = new RequestLocationUpdates(requestInfo, forceSendMessage);
            if (!locationInfo.isSeamless && locationManager.isProviderEnabled(locationInfo.provider)) {
                requestsLocationUpdates.add(requestLocationUpdates);
                locationManager.requestLocationUpdates(locationInfo.provider, 0, 0, requestLocationUpdates);

                if (requestInfo.isLocationOptional) {
                    Log.i(Constants.NAME, "after " + delay + "ms without position, we will send the message without position");
                    forceSendMessages.add(forceSendMessage);
                    timer.postDelayed(forceSendMessage, delay);
                }
                stopIt = false;
            } else if (locationInfo.isSeamless) {
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isGpsEnabled) {
                    requestsLocationUpdates.add(requestLocationUpdates);
                    if (isNetworkEnabled) {
                        Log.d(Constants.NAME, "seamless mode: request NETWORK provider");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, requestLocationUpdates);
                    }
                    Log.d(Constants.NAME, "seamless mode: request GPS provider");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, requestLocationUpdates);

                    if (requestInfo.isLocationOptional) {
                        Log.i(Constants.NAME, "after " + delay + "ms without position, we will send the message without position");
                        forceSendMessages.add(forceSendMessage);
                        timer.postDelayed(forceSendMessage, delay);
                    }
                    stopIt = false;
                }
            }

            if (stopIt) {
                Toast.makeText(this, R.string.provider_not_currently_enabled, Toast.LENGTH_LONG).show();
                stop();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(Constants.NAME, "update location service is stopped");
        try {
            if (pref.getBoolean(Constants.BATTERY_LEVEL, Constants.DEFAULT_BATTERY_LEVEL)) {
                try {
                    unregisterReceiver(this.batteryLevel);
                } catch (IllegalArgumentException e) {
                    // Silent error
                }
            }

            if (wifiLock.isHeld()) {
                try {
                    synchronized (scansResultsAvailable) {
                        for (ScanResultsAvailable scanResultsAvailable : scansResultsAvailable) {
                            unregisterReceiver(scanResultsAvailable);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Silent error
                } finally {
                    wifiLock.release();
                }
            }
        } finally {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            locationManager.removeGpsStatusListener(gpsStatusListener);
            synchronized (requestsLocationUpdates) {
                for (RequestLocationUpdates requestLocationUpdates : requestsLocationUpdates) {
                    locationManager.removeUpdates(requestLocationUpdates);
                }
            }

            WidgetProvider.stopService(this);

            synchronized (forceSendMessages) {
                for (ForceSendMessage forceSendMessage : forceSendMessages) {
                    timer.removeCallbacks(forceSendMessage);
                }
            }
        }
    }

    private void uploadLocation(Location location, RequestInfo requestInfo)
    {
        sendBroadcast(new Intent(Constants.LOCATION_CHANGED));

        boolean isCellId = pref.getBoolean(Constants.UPLOAD_CELLID, Constants.DEFAULT_UPLOAD_CELLID);
        boolean isUpload = pref.getBoolean(Constants.UPLOAD, Constants.DEFAULT_UPLOAD);
        // boolean isIpInfo = pref.getBoolean(Constants.UPLOAD_IPINFO, Constants.DEFAULT_UPLOAD_IPINFO);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if (wifiLock.isHeld()) {
            if (requestInfo.isWifiAvailable) {
                Log.i(Constants.NAME, "wifi available");
                wifiInfo.wifiInfo = wifiManager.getConnectionInfo();
                wifiInfo.scanResult = wifiManager.getScanResults();
            }
        }

        synchronized (this) {
            if (isCellId) {
                Log.i(Constants.NAME, "cellid available");
                cellIdInfo.cellInfo = telephonyManager.getNeighboringCellInfo();
                cellIdInfo.operator = telephonyManager.getNetworkOperator();
                cellIdInfo.cellLoc = telephonyManager.getCellLocation();
                cellIdInfo.networkType = telephonyManager.getNetworkType();
            }

            phoneInfo.network = info;

            // if (isIpInfo) {
            // Log.i(Constants.NAME, "ipinfo available");
            // try {
            // for (final Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            // final NetworkInterface intf = en.nextElement();
            // for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
            // InetAddress inetAddress = enumIpAddr.nextElement();
            // if (!inetAddress.isLoopbackAddress()) {
            // final IPInfos ip = new IPInfos();
            // ip.setHostname(inetAddress.getHostName());
            // ip.setIpAddress(inetAddress.getHostAddress().toString());
            // ip.setIpGateway("0.0.0.0");
            // ipInfo.ipInfos.add(ip);
            // }
            // }
            // }
            // } catch (final SocketException ex) {
            // Log.e(Constants.NAME, ex.toString(), ex);
            // }
            // }
        }

        final String query = GiftClient.query(locationInfo, location, requestInfo, cellIdInfo, wifiInfo, phoneInfo,
                Constants.APPLICATION_VERSION, Constants.VERSION, ipInfo);

        final ContentResolver cr = getContentResolver();
        final ContentValues values = new ContentValues();
        final long now = System.currentTimeMillis();
        final long elapsed = SystemClock.elapsedRealtime() - requestInfo.startedAt;
        values.put(EmitterProvider.TRYLATER, true);
        values.put(EmitterProvider.TIME, now);
        values.put(EmitterProvider.QUERY, query);
        values.put(EmitterProvider.ELAPSED_TIME, elapsed);

        Log.d(Constants.NAME, "elapsed time = " + elapsed + " ms");

        cr.insert(EmitterProvider.CONTENT_URI, values);

        if (isUpload) {
            Log.d(Constants.NAME, "upload queries service is starting");
            sendBroadcast(new Intent(this, UploadOnce.class));
        } else {
            // send broadcast to refresh history activity if it's opened
            sendBroadcast(new Intent(Constants.HISTORY_NEEDS_REFRESH));
        }

        stop();
    }

    @Override
    protected boolean isForce(Intent intent)
    {
        return intent != null && intent.getBooleanExtra(FORCE, false);
    }

    public static boolean isStarted()
    {
        return isStarted;
    }
}
