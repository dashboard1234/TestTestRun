package com.example.gan.testtestrun;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

/**
 * Created by gan on 2/21/17.
 */

public class SignalProvider extends ContentProvider {
    private static final UriMatcher uriMatch = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String PROVIDER_NAME = "com.example.gan.testtestrun.SignalProvider";
    private static final int SIGNALS = 1;
    private static final int SIGNAL_BY_ID =2;
    private static final int SIGNAL_RANGE = 3;
    static final String URL = "content://" + PROVIDER_NAME + "/signals";
    static final Uri CONTENT_URI = Uri.parse(URL);
    static final String TAG = SignalProvider.class.getName();

    // database definitions
    private SQLiteDatabase db;
    static final String COL_ID = "_id";
    static final String COL_TICK = "tick";
    static final String COL_SIGNAL = "signal";
    static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "Reminder";
    static final String DB_TABLE_SIGNAL = "Signal";
    static final String DB_CREATE_SCRIPT =
            " CREATE TABLE IF NOT EXISTS " + DB_TABLE_SIGNAL +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " tick INTEGER NOT NULL, " +
                    " signal REAL NOT NULL);";

    // db helper class
    private class DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_SCRIPT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  DB_TABLE_SIGNAL);
            onCreate(db);
        }
    }


    static {
        uriMatch.addURI(PROVIDER_NAME, "signals", SIGNALS);
        uriMatch.addURI(PROVIDER_NAME, "signals/#", SIGNAL_BY_ID);
        uriMatch.addURI(PROVIDER_NAME, "signals/#/#", SIGNAL_RANGE);
    }
    @Override
    public boolean onCreate() {
        DatabaseHelper helper = new DatabaseHelper(getContext());

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = helper.getWritableDatabase();
        helper.onCreate(db);
        return db == null ? false : true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DB_TABLE_SIGNAL);
        switch(uriMatch.match(uri))
        {
            case SIGNAL_BY_ID:
                qb.appendWhere(COL_ID + "=" + uri.getLastPathSegment());
                break;
            case SIGNALS:
                break;
            case SIGNAL_RANGE:
                String fromId = uri.getPathSegments().get(1);
                String toId = uri.getPathSegments().get(2);
                qb.appendWhere(COL_ID + ">=" + fromId + " AND " + COL_ID + "<=" + toId);
                break;
            default:
                Log.e(TAG, "Unspported uri: " + uri);
                throw new IllegalArgumentException("Unsupported uri:" + uri);
        }
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(uriMatch.match(uri))
        {
            case SIGNAL_BY_ID:
                return "vnd.android.cursor.item/vnd.com.example.gan.testtestrun.signal";
            case SIGNALS:
            case SIGNAL_RANGE:
                return "vnd.android.cursor.dir/vnd.com.example.gan.testtestrun.signal";
            default:
                Log.e(TAG, "Unspported uri: " + uri);
                throw new IllegalArgumentException("Unsupported uri:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = db.insert(DB_TABLE_SIGNAL, null, values);
        if(rowId > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Uri newData = ContentUris.withAppendedId(CONTENT_URI, rowId);
            return newData;
        }
        throw new SQLException("Invalid uri: cannot insert " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatch.match(uri)) {
            case SIGNAL_BY_ID:
                String id = uri.getLastPathSegment();
                count = db.delete(DB_TABLE_SIGNAL, COL_ID + "=" + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
                break;
            case SIGNALS:
                count = db.delete(DB_TABLE_SIGNAL, selection, selectionArgs);
                break;
            case SIGNAL_RANGE:
                String fromId = uri.getPathSegments().get(1);
                String toId = uri.getPathSegments().get(2);
                count = db.delete(DB_TABLE_SIGNAL, COL_ID + ">=" + fromId + " AND " + COL_ID + "<=" + toId +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized uri in delete: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatch.match(uri)) {
            case SIGNAL_BY_ID:
                String id = uri.getLastPathSegment();
                count = db.update(DB_TABLE_SIGNAL, values, COL_ID + "=" + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
                break;
            case SIGNALS:
                count = db.update(DB_TABLE_SIGNAL, values, selection, selectionArgs);
                break;
            case SIGNAL_RANGE:
                String fromId = uri.getPathSegments().get(1);
                String toId = uri.getPathSegments().get(2);
                count = db.update(DB_TABLE_SIGNAL, values, COL_ID + ">=" + fromId + " AND " + COL_ID + "<=" + toId +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized uri in delete: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
