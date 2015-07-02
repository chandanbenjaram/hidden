package droid.samepinch.co.data;

import android.content.ContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.data.dao.IPostSchema;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class AppContract extends ContentProvider {


    static final int POSTS = 1;
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();


    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AppConstants.API.CONTENT_AUTHORITY.getValue();

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, WallContract.PATH_POST, POSTS);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POSTS:
                return WallContract.Post.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case POSTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IPostSchema.POST_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("UPDATE not supported...");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("DELETE not supported...");
    }
}