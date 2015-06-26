package droid.samepinch.co.data;
import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
/**
 * Created by imaginationcoder on 6/24/15.
 */
public class WallProvider extends ContentProvider {

        // The URI Matcher used by this content provider.
        private static final UriMatcher sUriMatcher = buildUriMatcher();
        private WallDbHelper mOpenHelper;

        static final int POSTS = 108;

        private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

        static{
            sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        }

        static UriMatcher buildUriMatcher() {

            final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
            final String authority = WallContract.CONTENT_AUTHORITY;

            // For each type of URI you want to add, create a corresponding code.
            matcher.addURI(authority, WallContract.PATH_POSTS, POSTS);
            return matcher;
        }

        /*
            Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
            here.
         */
        @Override
        public boolean onCreate() {
            mOpenHelper = new WallDbHelper(getContext());
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
                    return WallContract.Posts.CONTENT_TYPE;
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
                            WallContract.Posts.TABLE_NAME,
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

        /*
            Student: Add the ability to insert Locations to the implementation of this function.
         */
        @Override
        public Uri insert(Uri uri, ContentValues values) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            Uri returnUri;

            switch (match) {
                case POSTS: {
                    long _id = db.insert(WallContract.Posts.TABLE_NAME, null, values);
                    if ( _id <= 0 )
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return null;
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