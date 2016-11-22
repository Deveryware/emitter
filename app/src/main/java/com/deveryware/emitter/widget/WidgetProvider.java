package com.deveryware.emitter.widget;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.ui.histories.Histories;
import com.deveryware.emitter.ui.preferences.Settings;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * http://stackoverflow.com/questions/5655015/appwidget-click-lost-after-system-restarts-my-process
 * 
 * @author sylvek
 * 
 */
public class WidgetProvider extends AppWidgetProvider {

    private static final String PACKAGE = "com.deveryware.emitter";

    private static final String CLASS = "com.deveryware.emitter.widget.WidgetProvider";

    private static final RemoteViews views = new RemoteViews(PACKAGE, R.layout.widget);

    private static final ComponentName cn = new ComponentName(PACKAGE, CLASS);

    private static final int FLAG = PendingIntent.FLAG_UPDATE_CURRENT;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Log.d(Constants.NAME, "widget.onUpdate called");
        context.startService(new Intent(context, WidgetService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(Constants.NAME, "widget.onReceive called");
        super.onReceive(context, intent);
    }

    public static final AppWidgetManager update(Context context)
    {
        return WidgetProvider.update(context, true);
    }

    private static final AppWidgetManager update(Context context, boolean update)
    {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        final Intent start = new Intent(context, SwitchOnOffWidget.class);
        views.setOnClickPendingIntent(R.id.on_off, PendingIntent.getBroadcast(context, 0, start, FLAG));

        final Intent manage = new Intent(context, ManagePrivacyMode.class);
        views.setOnClickPendingIntent(R.id.manage, PendingIntent.getBroadcast(context, 0, manage, FLAG));

        final Intent history = new Intent(context, Histories.class);
        views.setOnClickPendingIntent(R.id.history, PendingIntent.getActivity(context, 0, history, FLAG));

        final Intent settings = new Intent(context, Settings.class);
        views.setOnClickPendingIntent(R.id.settings, PendingIntent.getActivity(context, 0, settings, FLAG));

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref.getBoolean(Constants.GEOLOC_ENABLE, false)) {
            views.setImageViewResource(R.id.manage, R.drawable.ic_action_privacy_green);
        } else {
            views.setImageViewResource(R.id.manage, R.drawable.ic_action_privacy);
        }

        if (update) {
            appWidgetManager.updateAppWidget(cn, views);
        }

        return appWidgetManager;
    }

    public static final void startService(Context context)
    {
        final AppWidgetManager appWidgetManager = WidgetProvider.update(context, false);

        views.setImageViewResource(R.id.on_off, R.drawable.ic_action_locate_green);

        appWidgetManager.updateAppWidget(cn, views);
        SwitchOnOffWidget.setStarted(true);
    }

    public static final void stopService(Context context)
    {
        final AppWidgetManager appWidgetManager = WidgetProvider.update(context, false);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref.getBoolean(Constants.GEOLOC_ENABLE, false)) {
            views.setImageViewResource(R.id.on_off, R.drawable.ic_action_locate);
        } else {
            views.setImageViewResource(R.id.on_off, R.drawable.ic_action_locate_gray);
        }

        appWidgetManager.updateAppWidget(cn, views);
        SwitchOnOffWidget.setStarted(false);
    }
}
