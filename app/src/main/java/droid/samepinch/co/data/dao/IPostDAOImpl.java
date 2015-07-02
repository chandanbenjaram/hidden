package droid.samepinch.co.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    public static final String LOG_TAG = IPostDAOImpl.class.getSimpleName();

    private Cursor cursor;
    private ContentValues initialValues;

    public IPostDAOImpl(SQLiteDatabase db) {
        super(db);
    }

    @Override
    protected Post cursorToEntity(Cursor cursor) {
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
        mDb.beginTransaction();
        try {
            for (Post post : posts) {
                if (!addPost(post)) {
                    throw new SQLException("Failed to insert row into " + post.getUid());
                }
            }
            mDb.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.w("Database", ex.getMessage());
            return false;
        } finally {
            mDb.endTransaction();
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
}
