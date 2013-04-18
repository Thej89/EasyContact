
package com.thej.fyp.easycontact.db;

import java.util.HashMap;

import com.thej.fyp.easycontact.db.User.Users;

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
import android.util.Log;

/**
 * @author Thejanee walgamage
 * 
 */
public class EasyContactContentProvider extends ContentProvider {

    private static final String TAG = "UserContentProvider";

    private static final String DATABASE_NAME = "user.db";

    private static final int DATABASE_VERSION = 1;

    private static final String USER_TABLE_NAME = "user";

    public static final String AUTHORITY = "com.thej.fyp.easycontact.db.EasyContactContentProvider";

    private static final UriMatcher sUriMatcher;

    private static final int USERS = 1;

    private static final int User_ID = 2;

    private static HashMap<String, String> UserProjectionMap;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + USER_TABLE_NAME + " (" + Users.USER_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," + Users.USER_NAME + " VARCHAR(255)," + Users.USER_EMAIL + " LONGTEXT" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper dbHelper;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case USERS:
                break;
            case User_ID:
                where = where + "_id = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        int count = db.delete(USER_TABLE_NAME, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case USERS:
                return Users.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != USERS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(USER_TABLE_NAME, Users.USER_NAME, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Users.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(USER_TABLE_NAME);
        qb.setProjectionMap(UserProjectionMap);

        switch (sUriMatcher.match(uri)) {    
            case USERS:
                break;
            case User_ID:
                selection = selection + "_id = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case USERS:
                count = db.update(USER_TABLE_NAME, values, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, USER_TABLE_NAME, USERS);
        sUriMatcher.addURI(AUTHORITY, USER_TABLE_NAME + "/#", User_ID);

        UserProjectionMap = new HashMap<String, String>();
        UserProjectionMap.put(Users.USER_ID, Users.USER_ID);
        UserProjectionMap.put(Users.USER_NAME, Users.USER_NAME);
        UserProjectionMap.put(Users.USER_EMAIL, Users.USER_EMAIL);
    }
}

