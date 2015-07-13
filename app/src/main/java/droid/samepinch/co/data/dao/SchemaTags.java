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
    String COLUMN_PHOTO_URL = "photo_url";


    String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL UNIQUE, "
            + COLUMN_PHOTO_URL + " TEXT"
            + ")";

    String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    String[] POST_COLUMNS = new String[]{
            _ID, COLUMN_NAME
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
