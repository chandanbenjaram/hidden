package droid.samepinch.co.data.dao;

import android.provider.BaseColumns;

/**
 * Created by cbenjaram on 7/1/15.
 */
public interface IUserSchema extends BaseColumns {
    String USER_TABLE = "users";
    String COLUMN_UID = "uid";
    String COLUMN_FNAME = "first_name";
    String COLUMN_LNAME = "last_name";
    String COLUMN_PREF_NAME = "pref_name";
    String COLUMN_PINCH_HANDLE = "pinch_handle";

    String USER_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + USER_TABLE
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_UID + " TEXT NOT NULL UNIQUE, "
            + COLUMN_FNAME + " TEXT DEFAULT '', "
            + COLUMN_LNAME + " TEXT DEFAULT '', "
            + COLUMN_PREF_NAME + " TEXT DEFAULT '', "
            + COLUMN_PINCH_HANDLE + " TEXT DEFAULT ''"
            + ")";

    String[] USER_COLUMNS = new String[]{
            _ID, COLUMN_UID, COLUMN_FNAME, COLUMN_LNAME,
            COLUMN_PREF_NAME, COLUMN_PINCH_HANDLE
    };
}
