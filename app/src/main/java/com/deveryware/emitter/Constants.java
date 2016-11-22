/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter;

import android.net.Uri;

public class Constants extends com.deveryware.library.Constants {

    public static final String VERSION = Constants.NAME + "/ar:" + Constants.APPLICATION_VERSION + Constants.LIBRARY_VERSION;

    public static final String NAME = "dw-android-emitter";

    public static final int APPLICATION_VERSION = 136;

    public static final String PROVIDER_NAME = "com.deveryware.emitter.provider.history";

    public static final String IDENTITY_KEY = "com.deveryware.identity";

    public static final String GEOLOC_ENABLE = "com.deveryware.geoloc_enable";

    public static final String PROVIDER = "com.deveryware.provider";

    public static final String MIN_TIME = "com.deveryware.min_time";

    public static final String START_ON_BOOT = "com.deveryware.start_on_boot";

    public static final String START_ON_WIFI = "com.deveryware.start_on_wifi";

    public static final String START_ON_GPS = "com.deveryware.start_on_gps";

    public static final String START_ON_MOBILE = "com.deveryware.start_on_mobile";

    public static final String WAKE_UP = "com.deveryware.wake_up";

    public static final String STOP_LOW_BATTERY = "com.deveryware.stop_low_battery";

    public static final String RETURN_LAST_POS = "com.deveryware.return_last_pos";

    public static final String VERSION_PREFERENCE = "com.deveryware.version";

    public static final String DATABASE_PREFERENCE = "com.deveryware.database";

    public static final String SYNCHRO_PREFERENCE = "com.deveryware.synchro";

    public static final String START_PREFERENCE = "com.deveryware.start_service";

    public static final String START_ON_GEOFENCING = "com.deveryware.start_on_geofencing";

    public static final String DELETE_AFTER_UPLOAD = "com.deveryware.delete_after_upload";

    public static final String URL = "com.deveryware.url";

    public static final boolean DEFAULT_START_ON_GPS = false;

    public static final boolean DEFAULT_START_ON_BOOT = false;

    public static final boolean DEFAULT_START_ON_WIFI = false;

    public static final boolean DEFAULT_START_ON_MOBILE = false;

    public final static long DEFAULT_MIN_TIME = 900; // seconds

    public final static String DEVERYWARE_WIFI_PROVIDER = "only-wifi-cellid";

    public final static String SEAMLESS_PROVIDER = "auto";

    public final static String DEFAULT_PROVIDER = SEAMLESS_PROVIDER;

    public static final boolean DEFAULT_STOP_LOW_BATTERY = true;

    public static final boolean DEFAULT_GEOLOC_ENABLE = false;

    public static final boolean DEFAULT_WAKE_UP = false;

    public static final boolean DEFAULT_UPLOAD = true;

    public static final boolean DEFAULT_UPLOAD_WIFI = false;

    public static final boolean DEFAULT_UPLOAD_CELLID = true;

    public static final boolean DEFAULT_UPLOAD_IPINFO = false;

    public static final boolean DEFAULT_NOTIF = false;

    public static final boolean DEFAULT_RETURN_LAST_POS = false;

    public static final boolean DEFAULT_DELETE_AFTER_UPLOAD = false;

    public static final String DATABASE_NAME = "DWHistories.db";

    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_NAME = "HISTORIES";

    public static final String LOCATION_CHANGED = "com.deveryware.emitter.broadcast.LOCATION_CHANGED";

    public static final String LOCATION_REQUESTED = "com.deveryware.emitter.broadcast.LOCATION_REQUESTED";

    public static final String PRIVACY_MODE_SWITCH = "com.deveryware.emitter.broadcast.PRIVACY_SWITCH";

    public static final String HISTORY_NEEDS_REFRESH = "com.deveryware.emitter.broadcast.HISTORY_NEEDS_REFRESH";

    public static final String QUERY_UPLOADED = "com.deveryware.emitter.broadcast.QUERY_UPLOADED";

    public static final String UPLOAD_CELLID = "com.deveryware.upload_cellid";

    public static final String UPLOAD_WIFI = "com.deveryware.upload_wifi";

    public static final String UPLOAD_IPINFO = "com.deveryware.upload_ipinfo";

    public static final String UPLOAD = "com.deveryware.upload";

    public static final String UPLOAD_CHOICE = "com.deveryware.upload_choice";

    public static final String NOTIFS = "com.deveryware.events";

    public static final String BATTERY_LEVEL = "com.deveryware.emitter.battery_level";

    public static final boolean DEFAULT_BATTERY_LEVEL = true;

    public static final String TELEPHONY_SIGNAL = "com.deveryware.emitter.telephony_signal";

    public static final boolean DEFAULT_TELEPHONY_SIGNAL = true;

    public static final String DISPLAY_VERSION = "com.deveryware.emitter.display_version";

    public static final boolean DEFAULT_DISPLAY_VERSION = true;

    public static final String TIMEOUT = "com.deveryware.timeout";

    public static final int DEFAULT_TIMEOUT = 60; // 60 seconds

    public static final boolean DEFAULT_FORCE_SEND = false;

    public static final String FORCE_SEND = "com.deveryware.force_send";

    public static final String ITERATION = "com.deveryware.iteration";

    public static final int DEFAULT_ITERATION = -1;

    public static final String CURRENT_ITERATION = "com.deveryware.current_iteration";

    public static final String UPLOAD_ON_CONNECTION = "com.deveryware.upload_on_connection";

    public static final boolean DEFAULT_UPLOAD_ON_CONNECTION = false;

    public static String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

    public static final String COMMAND_BY_SMS = "com.deveryware.command_by_sms";

    public static final boolean DEFAULT_COMMAND_BY_SMS = false;

    public static final String FORCE_AGPS = "com.deveryware.force_agps";

    public static final boolean DEFAULT_FORCE_AGPS = false;

    public static final String ADMIN_PASSWORD = "com.deveryware.admin_password";

    public static final String INFORM_PRIVACY = "com.deveryware.inform_privacy";

    public static final boolean DEFAULT_INFORM_PRIVACY = false;

    public static final String START_ON_GEOLOC_ENABLE = "com.deveryware.start_on_geoloc_enable";

    public static final boolean DEFAULT_START_ON_GEOLOC_ENABLE = false;

    public static final String DISPLAY_ON_OFF = "com.deveryware.display_on_off";

    public static final boolean DEFAULT_DISPLAY_ON_OFF = true;

    public static final String START_ON_POWER_DISCONNECTED = "com.deveryware.start_on_power_disconnected";

    public static final boolean DEFAULT_START_ON_POWER_DISCONNECTED = false;

    public static final String START_ON_POWER_CONNECTED = "com.deveryware.start_on_power_connected";

    public static final boolean DEFAULT_START_ON_POWER_CONNECTED = false;

    public static final Uri PREFIX_WIDGET_ID = Uri.parse("content://com.deveryware.emitter/widget/");
}
