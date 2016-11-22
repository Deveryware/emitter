/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import android.os.Build;

public abstract class Constants {

    static final String NAME = "dw-android-library";

    private static final int LIB_VERSION = 35;

    private static final String GIFT_VERSION = "2.0k-mars.2011";

    /*
     * [(m)odel]:[device_model]/[(a)ndroid]:[release_version]/[(l)ibrary(r)elease]:[library_release_number]/[(g)ift:[release_gift]
     */
    public static final String LIBRARY_VERSION = "/m:" + Build.MODEL + "/a:" + Build.VERSION.RELEASE + "/lr:"
            + Constants.LIB_VERSION + "/g:" + Constants.GIFT_VERSION;

    public final static String MESSAGE = "com.deveryware.emitter.broadcast.COMMAND_RECEIVED";

    public final static String FROM = "from";
    
    public static final String PROVIDER = "deveryware";

    public static final String LASTPOS_ACTION = "com.deveryware.emitter.broadcast.LASTPOS";
}
