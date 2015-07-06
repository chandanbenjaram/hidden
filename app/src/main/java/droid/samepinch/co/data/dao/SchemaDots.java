package droid.samepinch.co.data.dao;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import droid.samepinch.co.app.helpers.AppConstants;

/**
 * Created by cbenjaram on 7/1/15.
 */
public interface SchemaDots extends BaseColumns {
    // DB related stuff
    String TABLE_NAME = "dots";
    String COLUMN_UID = "uid";
    String COLUMN_FNAME = "first_name";
    String COLUMN_LNAME = "last_name";
    String COLUMN_PREF_NAME = "pref_name";
    String COLUMN_PINCH_HANDLE = "pinch_handle";
    String COLUMN_PHOTO_URL = "photo_url";

    String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_UID + " TEXT NOT NULL UNIQUE, "
            + COLUMN_FNAME + " TEXT DEFAULT '', "
            + COLUMN_LNAME + " TEXT DEFAULT '', "
            + COLUMN_PREF_NAME + " TEXT DEFAULT '', "
            + COLUMN_PINCH_HANDLE + " TEXT DEFAULT '', "
            + COLUMN_PHOTO_URL + " TEXT"
            + ")";
    String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    String[] DOTS_COLUMNS = new String[]{
            _ID, COLUMN_UID, COLUMN_FNAME, COLUMN_LNAME,
            COLUMN_PREF_NAME, COLUMN_PINCH_HANDLE, COLUMN_PHOTO_URL
    };

    // provider related stuff
    String PATH_DOTS = "dots";
    String CONTENT_AUTHORITY = AppConstants.API.CONTENT_AUTHORITY.getValue();
    String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOTS;
    String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOTS;

    Uri CONTENT_URI = Uri.withAppendedPath(SPContentProvider.CONTENT_URI, TABLE_NAME);
}
