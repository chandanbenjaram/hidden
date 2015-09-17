package co.samepinch.android.data.dao;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import co.samepinch.android.app.helpers.AppConstants;

/**
 * Created by cbenjaram on 7/1/15.
 */
public interface SchemaPostDetails extends BaseColumns {
    // DB related stuff
    String TABLE_NAME = "post_content";
    String COLUMN_UID = "uid";
    String COLUMN_CONTENT = "content";
    String COLUMN_IMAGES = "images";
    String COLUMN_LARGE_IMAGES = "large_images";
    String COLUMN_COMMENT_COUNT = "comment_count";
    String COLUMN_UPVOTE_COUNT = "upvote_count";
    String COLUMN_UPVOTED = "upvoted";
    String COLUMN_VIEWS = "views";
    String COLUMN_URL = "url";
    String COLUMN_ANONYMOUS = "anonymous";
    String COLUMN_CREATED_AT = "created_at";
    String COLUMN_TAGS = "tags";
    String COLUMN_PERMISSIONS = "permissions";
    String COLUMN_OWNER = "dot_uid";

    String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_UID + " TEXT NOT NULL UNIQUE, "
            + COLUMN_URL + " TEXT DEFAULT '', "
            + COLUMN_CONTENT + " TEXT DEFAULT '', "
            + COLUMN_IMAGES + " TEXT DEFAULT '', "
            + COLUMN_LARGE_IMAGES + " TEXT DEFAULT '', "
            + COLUMN_COMMENT_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_UPVOTE_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_UPVOTED + " INTEGER DEFAULT 0, "
            + COLUMN_VIEWS + " INTEGER DEFAULT 0, "
            + COLUMN_URL + " TEXT DEFAULT '', "
            + COLUMN_ANONYMOUS + " INTEGER DEFAULT 0, "
            + COLUMN_CREATED_AT + " REAL, "
            + COLUMN_TAGS + " TEXT DEFAULT '', "
            + COLUMN_PERMISSIONS + " TEXT DEFAULT '', "
            + COLUMN_OWNER + " TEXT NOT NULL, "
            + "FOREIGN KEY(dot_uid) REFERENCES dots(uid)"
            + ")";

    String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    String[] POST_DETAILS_COLUMNS = new String[]{
            _ID, COLUMN_UID, COLUMN_CONTENT, COLUMN_COMMENT_COUNT,
            COLUMN_UPVOTE_COUNT, COLUMN_UPVOTED, COLUMN_VIEWS, COLUMN_ANONYMOUS,
            COLUMN_CREATED_AT, COLUMN_TAGS, COLUMN_PERMISSIONS,
            COLUMN_OWNER, COLUMN_IMAGES, COLUMN_LARGE_IMAGES,
            COLUMN_URL
    };


    // provider related stuff
    String PATH_POST_DETAILS = "post_content";
    String CONTENT_AUTHORITY = AppConstants.API.CONTENT_AUTHORITY.getValue();
    String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST_DETAILS;
    String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST_DETAILS;

    Uri CONTENT_URI = Uri.withAppendedPath(SPContentProvider.CONTENT_URI, TABLE_NAME);
}
