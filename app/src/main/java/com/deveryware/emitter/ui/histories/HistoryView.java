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
import com.deveryware.emitter.ui.GeolocationUtils;
import com.deveryware.emitter.ui.RadiusOverlay;
import com.deveryware.gift.GiftQuery;
import com.deveryware.gift.data.GpsLocation;
import com.deveryware.gift.data.Query;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.GeoPoint;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryView extends MapActivity {

    private static final int POSITION_GPS_FOUND_ZOOM = 15;

    private static final int POSITION_NETWORK_FOUND_ZOOM = 12;

    private static final int GPS_ACCURACY = 150; /* meters */

    private Query gift;

    private TextView query;

    private MapView mapView;

    private RadiusOverlay radiusOverlay;

    static final GiftQuery GIFT = new GiftQuery();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_view);

        query = (TextView) findViewById(R.id.query);

        final FrameLayout map = (FrameLayout) findViewById(R.id.map);
        mapView = new MapView(this);
        map.addView(mapView);

        radiusOverlay = new RadiusOverlay(mapView);
        GeolocationUtils.setUpMapView(mapView);
        mapView.getOverlays().add(radiusOverlay);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        /* http://stackoverflow.com/questions/6830291/how-to-force-restart-activity-from-intent */
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        final Uri uri = getIntent().getData();
        Log.d(Constants.NAME, "display: " + uri);

        final String queryString = HistoryView.getQuery(getApplicationContext(), uri);
        query.setText(queryString);

        setTitle(DateFormat.format(Constants.DATE_FORMAT, HistoryView.getTime(getApplicationContext(), uri)).toString());

        try {
            gift = GIFT.parse(queryString);

            GpsLocation location = gift.getGpsLocation();
            if (location != null) {
                TextView watchOnMap = (TextView) findViewById(R.id.latlon);
                watchOnMap.setText(location.getLatitude() + "," + location.getLongitude());
                TextView altitude = (TextView) findViewById(R.id.altitude);
                altitude.setText(location.getAltitude() + " m");
                TextView speed = (TextView) findViewById(R.id.speed);
                speed.setText(location.getSpeed() + " m/s - [" + (location.getSpeed() * 3.6f) + " km/h]");
                TextView accuracy = (TextView) findViewById(R.id.accuracy);
                accuracy.setText(location.getAccuracy() + " m");
                TextView numberOfSat = (TextView) findViewById(R.id.number_of_sat);
                numberOfSat.setText("" + location.getNbSatellites());
                TextView time = (TextView) findViewById(R.id.time);
                time.setText(DateFormat.format(Constants.DATE_FORMAT, location.getTime()));

                if (location.getAccuracy() < GPS_ACCURACY) {
                    mapView.getController().setZoom(POSITION_GPS_FOUND_ZOOM);
                } else {
                    mapView.getController().setZoom(POSITION_NETWORK_FOUND_ZOOM);
                }

                GeolocationUtils.setUpDisabledMapView(mapView);
                mapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                radiusOverlay.setRadius(location.getAccuracy());
                radiusOverlay.requestRedraw();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static final Long getTime(final Context context, final Uri uri)
    {
        long result = 0L;
        final Cursor cursor = context.getContentResolver().query(uri, new String[] { EmitterProvider.TIME }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int t = cursor.getColumnIndex(EmitterProvider.TIME);
            result = cursor.getLong(t);
            cursor.close();
        }
        return result;
    }

    public static final String getQuery(final Context context, final Uri uri)
    {
        String result = "";
        final Cursor cursor = context.getContentResolver().query(uri, new String[] { EmitterProvider.QUERY }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int q = cursor.getColumnIndex(EmitterProvider.QUERY);
            result = cursor.getString(q);
            cursor.close();
        }
        return result;
    }

    public void showOnMap(View view)
    {
        if (gift != null) {
            startMap(this, gift);
        }
    }

    public static final void startMap(final Context context, final Query q)
    {
        final GpsLocation gps = q.getGpsLocation();
        if (gps != null) {
            String date = DateFormat.format(Constants.DATE_FORMAT, gps.getTime()).toString();
            Uri position = Uri.parse("geo:0,0?q=" + gps.getLatitude() + "," + gps.getLongitude() + "(" + date + ")");
            Intent map = new Intent(android.content.Intent.ACTION_VIEW, position);
            context.startActivity(map);
        } else {
            Toast.makeText(context, R.string.no_position, Toast.LENGTH_SHORT).show();
        }
    }
}
