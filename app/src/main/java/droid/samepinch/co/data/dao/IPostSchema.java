package droid.samepinch.co.data.dao;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by cbenjaram on 7/1/15.
 */
public interface IPostSchema extends BaseColumns, IContract {
    // provider related stuff
    String PATH_POST = "post";
    Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POST).build();
    String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;
    String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;

    // DB related stuff
    String POST_TABLE = "posts";
    String COLUMN_UID = "uid";
    String COLUMN_CONTENT = "content";
    String COLUMN_COMMENT_COUNT = "comment_count";
    String COLUMN_UPVOTE_COUNT = "upvote_count";
    String COLUMN_VIEWS = "views";
    String COLUMN_ANONYMOUS = "anonymous";
    String COLUMN_CREATED_AT = "created_at";
    String COLUMN_COMMENTERS = "commenters";
    String COLUMN_TAGS = "tags";

    String POST_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + POST_TABLE
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_UID + " TEXT NOT NULL UNIQUE, "
            + COLUMN_CONTENT + " TEXT DEFAULT '', "
            + COLUMN_COMMENT_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_UPVOTE_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_VIEWS + " INTEGER DEFAULT 0, "
            + COLUMN_ANONYMOUS + " INTEGER DEFAULT 0, "
            + COLUMN_CREATED_AT + " REAL, "
            + COLUMN_COMMENTERS + " TEXT DEFAULT '', "
            + COLUMN_TAGS + " TEXT DEFAULT ''"
            + ")";

    String[] POST_COLUMNS = new String[]{
            _ID, COLUMN_UID, COLUMN_CONTENT, COLUMN_COMMENT_COUNT,
            COLUMN_UPVOTE_COUNT, COLUMN_VIEWS, COLUMN_ANONYMOUS,
            COLUMN_CREATED_AT, COLUMN_COMMENTERS, COLUMN_TAGS
    };
}
