/**
 *
 * Copyright (C) 2012 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 25 janv. 2012
 *
 */
package com.deveryware.emitter.ui.geofencing;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.broadcast.StartOnGeofencing;
import com.deveryware.emitter.ui.GeolocationUtils;
import com.deveryware.emitter.ui.RadiusOverlay;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @author sylvek
 * 
 */
public class Geofencing extends MapActivity {

    private SeekBar radius;

    private TextView displayRadius;

    private RadioButton in;

    private LocationManager locationManager;

    private LocationListener locationListener;

    private MapView mapView;

    private RadiusOverlay radiusOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geofencing);

        in = (RadioButton) findViewById(R.id.geofencing_in);
        radius = (SeekBar) findViewById(R.id.radius);
        displayRadius = (TextView) findViewById(R.id.display_radius);
        displayRadius.setText(radius.getProgress() + "m");

        final FrameLayout map = (FrameLayout) findViewById(R.id.map);
        mapView = new MapView(this);
        map.addView(mapView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = GeolocationUtils.getLocationListener(mapView);

        radiusOverlay = new RadiusOverlay(mapView);
        radiusOverlay.setRadius(radius.getProgress());

        GeolocationUtils.setUpMapView(mapView);
        mapView.getMapZoomControls().setZoomControlsGravity(Gravity.TOP | Gravity.RIGHT);
        mapView.getOverlays().add(radiusOverlay);

        radius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                displayRadius.setText(progress + "m");
                radiusOverlay.setRadius(progress);
                radiusOverlay.requestRedraw();
            }
        });

        final Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
                start(arg0);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (StopGeofencing.isStarted) {
            finish();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        GeolocationUtils.onResume(locationManager, locationListener, mapView, true);
    }

    @Override
    protected void onPause()
    {
        GeolocationUtils.onPause(locationManager, locationListener);
        super.onPause();
    }

    public void start(View view)
    {
        int r = radius.getProgress();
        double latitude = mapView.getMapPosition().getMapCenter().getLatitude();
        double longitude = mapView.getMapPosition().getMapCenter().getLongitude();
        boolean entry = in.isChecked();
        Log.d(Constants.NAME, "geofencing:" + latitude + "," + longitude + ":" + r + "m:" + entry);

        final Intent intent = new Intent(this, StartOnGeofencing.class);
        intent.putExtra(StartOnGeofencing.KEY_PROXIMITY_ENTERING, entry);
        final PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        locationManager.addProximityAlert(latitude, longitude, r, -1, pending);

        final String toNotify = getString(R.string.stop_geofencing);
        final Notification notification = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.icon)
                .setContentText(toNotify)
                .setContentTitle(getText(R.string.app_name))
                .setAutoCancel(true)
                .setContentIntent(StopGeofencing.get(getApplicationContext(), pending))
                .build();

        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(StopGeofencing.GEOFENCING_NOTIFICATION_ID, notification);

        finish();
    }
}
