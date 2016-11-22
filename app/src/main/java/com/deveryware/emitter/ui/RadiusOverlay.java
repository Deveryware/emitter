/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 28 mars 2011
 *
 */
package com.deveryware.emitter.ui;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.core.GeoPoint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/**
 * @author sylvek
 * 
 */
public class RadiusOverlay extends Overlay {

    private final MapView map;

    private float radius;

    private final Path thePath;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            this.setColor(Color.BLUE);
            this.setAlpha(70);
            this.setStyle(Style.FILL_AND_STROKE);
        }
    };

    private Point out;

    /**
     * @param fillPaint
     * @param outlinePaint
     */
    public RadiusOverlay(MapView map)
    {
        super();
        this.map = map;
        this.thePath = new Path();
    }
    
    public void setRadius(float radius)
    {
        this.radius = radius;
    }

    @Override
    protected void drawOverlayBitmap(Canvas canvas, Point drawPosition, Projection projection, byte drawZoomLevel)
    {
        if (map != null) {
            final GeoPoint in = map.getMapPosition().getMapCenter();
            if (in != null) {
                out = projection.toPoint(in, out, drawZoomLevel);
                float r = projection.metersToPixels(radius, drawZoomLevel);
                this.thePath.reset();
                this.thePath.addCircle(out.x - drawPosition.x, out.y - drawPosition.y, r, Path.Direction.CCW);
                canvas.drawPath(thePath, paint);
            }
        }
    }
}
