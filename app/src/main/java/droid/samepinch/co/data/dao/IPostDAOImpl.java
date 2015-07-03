package droid.samepinch.co.data.dao;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import droid.samepinch.co.data.DBContentProvider;
import droid.samepinch.co.data.dto.Post;

/**
 * Created by cbenjaram on 7/1/15.
 */
public class IPostDAOImpl extends DBContentProvider implements IPostDAO, IPostSchema {
    public static final String LOG_TAG = "IPostDAOImpl";
    static final int POST = 108;
    static final int POST_WITH_ID = 109;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private Cursor cursor;
    private ContentValues initialValues;

    public IPostDAOImpl() {
    }

    static UriMatcher buildUriMatcher() {
        final String authority = IContract.CONTENT_AUTHORITY;
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, IPostSchema.PATH_POST, POST);
        matcher.addURI(authority, IPostSchema.PATH_POST + "/*", POST_WITH_ID);
        return matcher;
    }

    @Override
    public Post cursorToEntity(Cursor cursor) {
        Post post = new Post();
        if (cursor == null) {
            return post;
        }

        int idIndex;
        int contentIndex;
        int commentCountIndex;
        int upvoteCountIndex;
        int viewsIndex;
        int anonymousIndex;
        int createdAtIndex;
        int commentersIndex;
        int tagsIndex;


        if (cursor.getColumnIndex(COLUMN_UID) != -1) {
            idIndex = cursor.getColumnIndexOrThrow(COLUMN_UID);
            post.setUid(cursor.getString(idIndex));
        }

        if (cursor.getColumnIndex(COLUMN_CONTENT) != -1) {
            contentIndex = cursor.getColumnIndexOrThrow(COLUMN_CONTENT);
            post.setContent(cursor.getString(contentIndex));
        }

        if (cursor.getColumnIndex(COLUMN_COMMENT_COUNT) != -1) {
            commentCountIndex = cursor.getColumnIndexOrThrow(COLUMN_COMMENT_COUNT);
            post.setCommentCount(cursor.getInt(commentCountIndex));
        }

        if (cursor.getColumnIndex(COLUMN_UPVOTE_COUNT) != -1) {
            upvoteCountIndex = cursor.getColumnIndexOrThrow(COLUMN_UPVOTE_COUNT);
            post.setUpvoteCount(cursor.getInt(upvoteCountIndex));
        }

        if (cursor.getColumnIndex(COLUMN_VIEWS) != -1) {
            viewsIndex = cursor.getColumnIndexOrThrow(COLUMN_VIEWS);
            post.setViews(cursor.getInt(viewsIndex));
        }

        if (cursor.getColumnIndex(COLUMN_ANONYMOUS) != -1) {
            anonymousIndex = cursor.getColumnIndexOrThrow(COLUMN_ANONYMOUS);
            post.setAnonymous(cursor.getInt(anonymousIndex) == 1);
        }

        if (cursor.getColumnIndex(COLUMN_CREATED_AT) != -1) {
            createdAtIndex = cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT);
            post.setCreatedAt(new Date(cursor.getLong(createdAtIndex)));
        }

        if (cursor.getColumnIndex(COLUMN_COMMENTERS) != -1) {
            commentersIndex = cursor.getColumnIndexOrThrow(COLUMN_COMMENTERS);
            post.setCommentersFromDB(cursor.getString(commentersIndex));
        }

        if (cursor.getColumnIndex(COLUMN_TAGS) != -1) {
            tagsIndex = cursor.getColumnIndexOrThrow(COLUMN_TAGS);
            post.setTagsFromDB(cursor.getString(tagsIndex));
        }

        return post;
    }

    @Override
    public Post fetchPostById(String postId) {
        final String selectionArgs[] = {postId};
        final String selection = _ID + " = ?";

        Post post = new Post();
        cursor = super.query(POST_TABLE, POST_COLUMNS, selection,
                selectionArgs, _ID);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                post = cursorToEntity(cursor);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return post;
    }

    @Override
    public List<Post> fetchAllPosts() {
        List<Post> postList = new ArrayList<>();
        cursor = super.query(POST_TABLE, POST_COLUMNS, null,
                null, _ID);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Post post = cursorToEntity(cursor);
                postList.add(post);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return postList;
    }

    @Override
    public boolean addPost(Post post) {
        // set values
        setContentValue(post);
        try {
            ContentValues contentValues = getContentValue();

            final String selectionArgs[] = {post.getUid()};
            final String selection = COLUMN_UID + " = ?";
            if (super.update(POST_TABLE, getContentValue(), selection, selectionArgs) > 0) {
                return true;
            } else {
                return super.insertOrThrow(POST_TABLE, contentValues) > 0;
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "error inserting post", e);
            return false;
        }
    }

    @Override
    public boolean addPosts(List<Post> posts) {
        SQLiteDatabase writeDB = mDBHelper.getWritableDatabase();
        writeDB.beginTransaction();
        try {
            for (Post post : posts) {
                if (!addPost(post)) {
                    throw new SQLException("Failed to insert row into " + post.getUid());
                }
            }
            writeDB.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.w("Database", ex.getMessage());
            return false;
        } finally {
            writeDB.endTransaction();
        }
        return true;
    }

    @Override
    public boolean deleteAllPosts() {
        return false;
    }

    private ContentValues getContentValue() {
        return initialValues;
    }

    private void setContentValue(Post post) {
        initialValues = new ContentValues();
        initialValues.put(COLUMN_UID, post.getUid());
        initialValues.put(COLUMN_CONTENT, post.getContent());
        initialValues.put(COLUMN_COMMENT_COUNT, post.getCommentCount());
        initialValues.put(COLUMN_UPVOTE_COUNT, post.getUpvoteCount());
        initialValues.put(COLUMN_VIEWS, post.getViews());
        initialValues.put(COLUMN_ANONYMOUS, post.getAnonymous());
        initialValues.put(COLUMN_CREATED_AT, post.getCreatedAt().getTime());
        initialValues.put(COLUMN_COMMENTERS, post.getCommentersForDB());
        initialValues.put(COLUMN_TAGS, post.getTagsForDB());
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case POST: {
                retCursor = super.query(POST_TABLE, projection, selection,
                        selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case POST:
                return IPostSchema.CONTENT_TYPE;
            case POST_WITH_ID:
                return IPostSchema.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues cv) {
        Long rowId = upSert(cv);
        Uri returnUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    public Long upSert(ContentValues cv) {
        String uid = cv.getAsString(COLUMN_UID);
        final String selectionArgs[] = {uid};
        final String selection = COLUMN_UID + " = ?";
        Long rowId = null;
        if (super.update(POST_TABLE, cv, selection, selectionArgs) > 0) {
            Cursor cursor = super.query(POST_TABLE, new String[]{_ID}, selection,
                    selectionArgs, _ID);
            if (cursor != null && cursor.moveToFirst()) {

                int idIndex;
                if ((idIndex = cursor.getColumnIndex(_ID)) != -1) {
                    rowId = cursor.getLong(idIndex);
                }
                cursor.close();
            }
        } else {
            rowId = super.insertOrThrow(POST_TABLE, cv);
        }

        return rowId;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int count = 0;
        ContentResolver resolver = getContext().getContentResolver();
        for (ContentValues cv : values) {
            count += 1;
            Long rowId = upSert(cv);
            Uri itemUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            resolver.notifyChange(itemUri, null);
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
