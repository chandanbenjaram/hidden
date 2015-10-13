package co.samepinch.android.data.dao;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import co.samepinch.android.app.helpers.AppConstants;

/**
 * Created by cbenjaram on 7/1/15.
 */
public interface SchemaPosts extends BaseColumns {
    // DB related stuff
    String TABLE_NAME = "posts";
    String COLUMN_UID = "uid";
    String COLUMN_WALL_CONTENT = "wall_content";
    String COLUMN_WALL_IMAGES = "wall_images";
    String COLUMN_COMMENT_COUNT = "comment_count";
    String COLUMN_UPVOTE_COUNT = "upvote_count";
    String COLUMN_VIEWS = "views";
    String COLUMN_ANONYMOUS = "anonymous";
    String COLUMN_CREATED_AT = "created_at";
    String COLUMN_COMMENTERS = "commenters";
    String COLUMN_TAGS = "tags";
    String COLUMN_OWNER = "dot_uid";
    String COLUMN_SRC_WALL = "src_wall";
    String COLUMN_SRC_TAG = "src_tag";
    String COLUMN_SRC_SEARCH = "src_search";
    String COLUMN_SRC_FAV = "src_fav";

    String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_UID + " TEXT NOT NULL UNIQUE, "
            + COLUMN_WALL_CONTENT + " TEXT, "
            + COLUMN_WALL_IMAGES + " TEXT, "
            + COLUMN_COMMENT_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_UPVOTE_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_VIEWS + " INTEGER DEFAULT 0, "
            + COLUMN_ANONYMOUS + " INTEGER DEFAULT 0, "
            + COLUMN_CREATED_AT + " INTEGER, "
            + COLUMN_COMMENTERS + " TEXT DEFAULT '', "
            + COLUMN_TAGS + " TEXT DEFAULT '', "
            + COLUMN_OWNER + " TEXT NOT NULL, "
            + COLUMN_SRC_WALL + " INTEGER DEFAULT 0, "
            + COLUMN_SRC_TAG + " INTEGER DEFAULT 0, "
            + COLUMN_SRC_SEARCH + " INTEGER DEFAULT 0, "
            + COLUMN_SRC_FAV + " INTEGER DEFAULT 0, "
            + "FOREIGN KEY(dot_uid) REFERENCES dots(uid)"
            + ")";

    String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

//    String TRGR_DEL_POST_WT_USER = "CREATE TRIGGER del_post_with_user BEFORE DELETE ON user "
//            + " FOR EACH ROW BEGIN"
//            + " DELETE FROM post WHERE post.owner_uid = user.uid "
//            + " END;";

    String[] POST_COLUMNS = new String[]{
            _ID, COLUMN_UID, COLUMN_WALL_CONTENT, COLUMN_COMMENT_COUNT,
            COLUMN_UPVOTE_COUNT, COLUMN_VIEWS, COLUMN_ANONYMOUS,
            COLUMN_CREATED_AT, COLUMN_COMMENTERS, COLUMN_TAGS,
            COLUMN_OWNER, COLUMN_WALL_IMAGES, COLUMN_WALL_CONTENT,
            COLUMN_WALL_IMAGES, COLUMN_SRC_WALL, COLUMN_SRC_TAG, COLUMN_SRC_SEARCH, COLUMN_SRC_FAV
    };

    String VIEW_CREATE_POST_WITH_DOT_NAME = "VIEW_POST_WITH_DOT";
    String VIEW_CREATE_POST_WITH_DOT = "CREATE VIEW IF NOT EXISTS "
            + VIEW_CREATE_POST_WITH_DOT_NAME
            + " AS SELECT"
            + " p." + _ID
            + ", p." + COLUMN_UID
            + ", p." + COLUMN_WALL_CONTENT
            + ", p." + COLUMN_COMMENT_COUNT
            + ", p." + COLUMN_UPVOTE_COUNT
            + ", p." + COLUMN_VIEWS
            + ", p." + COLUMN_ANONYMOUS
            + ", p." + COLUMN_COMMENTERS
            + ", p." + COLUMN_TAGS
            + ", p." + COLUMN_OWNER
            + ", p." + COLUMN_WALL_IMAGES
            + ", p." + COLUMN_CREATED_AT
            + ", p." + COLUMN_SRC_WALL
            + ", p." + COLUMN_SRC_TAG
            + ", p." + COLUMN_SRC_SEARCH
            + ", p." + COLUMN_SRC_FAV
            + ", d." + SchemaDots.COLUMN_PINCH_HANDLE
            + ", d." + SchemaDots.COLUMN_FNAME
            + ", d." + SchemaDots.COLUMN_LNAME
            + ", d." + SchemaDots.COLUMN_PREF_NAME
            + ", d." + SchemaDots.COLUMN_PHOTO_URL
            + " FROM " + TABLE_NAME + " p, " + SchemaDots.TABLE_NAME + " d "
            + " WHERE " + " p." + COLUMN_OWNER + "=" + "d." + SchemaDots.COLUMN_UID;
    String VIEW_DROP_POST_WITH_DOT = "DROP VIEW IF EXISTS " + VIEW_CREATE_POST_WITH_DOT_NAME;

    // provider related stuff
    String PATH_POSTS = "posts";
    String CONTENT_AUTHORITY = AppConstants.API.CONTENT_AUTHORITY.getValue();
    String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;
    String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;

    Uri CONTENT_URI = Uri.withAppendedPath(SPContentProvider.CONTENT_URI, TABLE_NAME);
}
