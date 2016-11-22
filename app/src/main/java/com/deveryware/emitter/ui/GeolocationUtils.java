/**
 *
 * Copyright (C) 2012 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 30 janv. 2012
 *
 */
package com.deveryware.emitter.ui;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorFactory;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorInternal;
import org.mapsforge.core.GeoPoint;

import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * 
 * @author sylvek
 * 
 */
public class GeolocationUtils {

    public static final int DEFAULT_ZOOM = 10;

    public static final int POSITION_FOUND_ZOOM = 14;

    public static final GeoPoint PARIS = new GeoPoint(48.833, 2.333);

    public static LocationListener getLocationListener(final MapView mapView)
    {
        return new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
            }

            @Override
            public void onProviderEnabled(String provider)
            {
            }

            @Override
            public void onProviderDisabled(String provider)
            {
            }

            @Override
            public void onLocationChanged(Location location)
            {
                displayLocation(mapView, location, false);
            }
        };
    }

    public static Criteria getCriteria()
    {
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setSpeedRequired(false);

        return criteria;
    }

    public static void onResume(LocationManager locationManager, LocationListener listener, MapView mapView, boolean refresh)
    {
        final String currentProvider = locationManager.getBestProvider(GeolocationUtils.getCriteria(), true);
        if (currentProvider != null) {
            final Location location = locationManager.getLastKnownLocation(currentProvider);

            if (location != null) {
                displayLocation(mapView, location, true);
            }

            if (refresh) {
                locationManager.requestLocationUpdates(currentProvider, 60000 /* ms */, 50 /* m */, listener);
            }
        }
    }

    public static void displayLocation(MapView mapView, Location location, boolean updateZoom)
    {
        if (updateZoom) {
            mapView.getController().setZoom(GeolocationUtils.POSITION_FOUND_ZOOM);
        }

        mapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
    }

    public static void onPause(LocationManager locationManager, LocationListener locationListener)
    {
        locationManager.removeUpdates(locationListener);
    }

    public static void setUpMapView(MapView mapView)
    {
        mapView.getMapScaleBar().setShowMapScaleBar(true);
        mapView.setMapGenerator(MapGeneratorFactory.createMapGenerator(MapGeneratorInternal.MAPNIK));
        mapView.setClickable(true);
        mapView.setFocusable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(GeolocationUtils.DEFAULT_ZOOM);
        mapView.getController().setCenter(GeolocationUtils.PARIS);
    }

    public static void setUpDisabledMapView(MapView mapView)
    {
        mapView.getMapScaleBar().setShowMapScaleBar(true);
        mapView.setMapGenerator(MapGeneratorFactory.createMapGenerator(MapGeneratorInternal.MAPNIK));
        mapView.setClickable(false);
        mapView.setFocusable(false);
        mapView.setBuiltInZoomControls(false);
    }

    public static boolean zoomAndPan(MapView mapView, int minLatE6, int maxLatE6, int minLngE6, int maxLngE6)
    {
        int width = mapView.getWidth();
        int heigth = mapView.getHeight();
        if (width <= 0 || heigth <= 0) {
            return false;
        }
        int cntLat = (maxLatE6 + minLatE6) / 2;
        int cntLng = (maxLngE6 + minLngE6) / 2;

        mapView.getController().setCenter(new GeoPoint(cntLat, cntLng));

        GeoPoint pointSouthWest = new GeoPoint(minLatE6, minLngE6);
        GeoPoint pointNorthEast = new GeoPoint(maxLatE6, maxLngE6);

        Projection projection = mapView.getProjection();
        Point pointSW = new Point();
        Point pointNE = new Point();
        byte maxLvl = (byte) mapView.getMapGenerator().getZoomLevelMax();
        byte zoomLevel = 0;
        for (; zoomLevel < maxLvl;) {
            byte tmpZoomLevel = (byte) (zoomLevel + 1);
            projection.toPoint(pointSouthWest, pointSW, tmpZoomLevel);
            projection.toPoint(pointNorthEast, pointNE, tmpZoomLevel);
            if (pointNE.x - pointSW.x > width) {
                break;
            }
            if (pointSW.y - pointNE.y > heigth) {
                break;
            }
            zoomLevel = tmpZoomLevel;
        }
        mapView.getController().setZoom(zoomLevel);
        return true;
    }
}
