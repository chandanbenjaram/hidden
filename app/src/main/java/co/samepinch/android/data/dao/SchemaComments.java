package co.samepinch.android.data.dao;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import co.samepinch.android.app.helpers.AppConstants;

/**
 * Created by imaginationcoder on 7/4/15.
 */
public interface SchemaComments extends BaseColumns {
    // DB related stuff
    String TABLE_NAME = "comments";
    String COLUMN_UID = "uid";
    String COLUMN_TEXT = "text";
    String COLUMN_CREATED_AT = "created_at";
    String COLUMN_ANONYMOUS = "anonymous";
    String COLUMN_UPVOTED = "upvoted";
    String COLUMN_UPVOTE_COUNT = "upvote_count";
    String COLUMN_PERMISSIONS = "permissions";

    String COLUMN_DOT_UID = "dot_uid";
    String COLUMN_DOT_FNAME = "dot_first_name";
    String COLUMN_DOT_LNAME = "dot_last_name";
    String COLUMN_DOT_PREF_NAME = "dot_pref_name";
    String COLUMN_DOT_PHOTO_URL = "dot_photo_url";
    String COLUMN_DOT_PINCH_HANDLE = "dot_pinch_handle";

    // relations
    String COLUMN_POST_DETAILS = "post_details_uid";

    String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_UID + " TEXT NOT NULL UNIQUE, "
            + COLUMN_TEXT + " TEXT DEFAULT '', "
            + COLUMN_CREATED_AT + " REAL, "
            + COLUMN_ANONYMOUS + " INTEGER DEFAULT 0, "
            + COLUMN_UPVOTED + " INTEGER DEFAULT 0, "
            + COLUMN_UPVOTE_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_PERMISSIONS + " TEXT DEFAULT '', "
            + COLUMN_DOT_UID + " TEXT NOT NULL, "
            + COLUMN_DOT_FNAME + " TEXT DEFAULT '', "
            + COLUMN_DOT_LNAME + " TEXT DEFAULT '', "
            + COLUMN_DOT_PINCH_HANDLE + " TEXT DEFAULT '', "
            + COLUMN_DOT_PREF_NAME + " TEXT DEFAULT '', "
            + COLUMN_DOT_PHOTO_URL + " TEXT , "
            + COLUMN_POST_DETAILS + " TEXT NOT NULL, "
            + "FOREIGN KEY(post_details_uid) REFERENCES "
            + String.format("%s(%s)", SchemaPostDetails.TABLE_NAME, SchemaPostDetails.COLUMN_UID)
            + ")";

    String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    String[] TAG_COLUMNS = new String[]{
            _ID, COLUMN_UID, COLUMN_TEXT, COLUMN_CREATED_AT, COLUMN_ANONYMOUS, COLUMN_UPVOTED, COLUMN_UPVOTE_COUNT,
            COLUMN_DOT_UID, COLUMN_DOT_FNAME, COLUMN_DOT_LNAME, COLUMN_DOT_PREF_NAME,  COLUMN_DOT_PHOTO_URL,
            COLUMN_POST_DETAILS, COLUMN_DOT_PINCH_HANDLE, COLUMN_PERMISSIONS
    };

    // provider related stuff
    String PATH_COMMENTS = "comments";
    String CONTENT_AUTHORITY = AppConstants.API.CONTENT_AUTHORITY.getValue();
    String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENTS;
    String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENTS;

    Uri CONTENT_URI = Uri.withAppendedPath(SPContentProvider.CONTENT_URI, TABLE_NAME);
}
