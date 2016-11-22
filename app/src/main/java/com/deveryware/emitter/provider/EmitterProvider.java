/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.provider;

import com.deveryware.emitter.Constants;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class EmitterProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://" + Constants.PROVIDER_NAME);

    public static final String ID = "_id";

    public static final String TIME = "time";

    public static final String TRYLATER = "trylater";

    public static final String QUERY = "query";

    public static final String RESULT = "result";

    public static final String UPLOADED = "uploaded";

    public static final String ELAPSED_TIME = "elapsed";

    private SQLiteDatabase sqlDB;

    private DatabaseHelper dbHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context)
        {
            super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("Create table " + Constants.TABLE_NAME + "( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TIME
                    + " LONG, " + TRYLATER + " BOOL, " + QUERY + " TEXT, " + RESULT + " TEXT, " + UPLOADED + " LONG,"
                    + ELAPSED_TIME + " LONG);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("ALTER TABLE " + Constants.TABLE_NAME + " ADD COLUMN " + UPLOADED + " LONG");
                db.execSQL("ALTER TABLE " + Constants.TABLE_NAME + " ADD COLUMN " + RESULT + " TEXT");
            } else if (oldVersion == 2 && newVersion == 3) {
                db.execSQL("ALTER TABLE " + Constants.TABLE_NAME + " ADD COLUMN " + ELAPSED_TIME + " LONG");
            } else {
                db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
                onCreate(db);
            }
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        sqlDB = dbHelper.getWritableDatabase();
        try {
            long rowId = sqlDB.insert(Constants.TABLE_NAME, "", values);
            if (rowId > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(EmitterProvider.CONTENT_URI, rowId);
            }
        } catch (Exception e) {
            Log.e(Constants.NAME,
                    "error : " + e.getMessage() + ". Please downgrade the rating. unable to store : " + values.toString());
        }
        return null;
    }

    @Override
    public boolean onCreate()
    {
        dbHelper = new DatabaseHelper(getContext());
        return (dbHelper == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Constants.TABLE_NAME);
        if (uri.getPathSegments().size() == 1) { // y'a un _ID
            qb.appendWhere(EmitterProvider.ID + "=" + uri.getPathSegments().get(0));
        }
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int result = db.delete(Constants.TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public String getType(Uri uri)
    {
        if (uri.getPathSegments().size() == 1) {
            return "vnd.android.cursor.item/vnd.deveryware.history";
        }
        return "vnd.android.cursor.item/vnd.deveryware.histories";
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int result = db.update(Constants.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

}
