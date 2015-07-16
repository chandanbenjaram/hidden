package droid.samepinch.co.data.dao;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import droid.samepinch.co.app.helpers.AppConstants;

/**
 * Created by imaginationcoder on 7/4/15.
 */
public interface SchemaTags extends BaseColumns {
    // DB related stuff
    String TABLE_NAME = "tags";
    String COLUMN_NAME = "name";
    String COLUMN_UID = "uid";
    String COLUMN_POSTS_COUNT = "posts_count";
    String COLUMN_FOLLOWERS_COUNT = "followers_count";
    String COLUMN_IMAGE = "image";



    String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL UNIQUE, "
            + COLUMN_UID + " TEXT DEFAULT '', "
            + COLUMN_POSTS_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_FOLLOWERS_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_IMAGE + " TEXT "
            + ")";

    String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    String[] TAG_COLUMNS = new String[]{
            _ID, COLUMN_NAME, COLUMN_UID, COLUMN_POSTS_COUNT, COLUMN_FOLLOWERS_COUNT, COLUMN_IMAGE
    };

    // provider related stuff
    String PATH_TAGS = "tags";
    String CONTENT_AUTHORITY = AppConstants.API.CONTENT_AUTHORITY.getValue();
    String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;
    String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;

    Uri CONTENT_URI = Uri.withAppendedPath(SPContentProvider.CONTENT_URI, TABLE_NAME);
}
