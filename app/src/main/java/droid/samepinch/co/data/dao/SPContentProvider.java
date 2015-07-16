package droid.samepinch.co.data.dao;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

import droid.samepinch.co.app.helpers.AppConstants;

/**
 * Created by imaginationcoder on 7/4/15.
 */
public class SPContentProvider extends ContentProvider {
    public static final String LOG_TAG = "SPContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AppConstants.API.CONTENT_AUTHORITY.getValue() + "/");

    // uri patterns
    // posts
    private static final int PATH_POSTS = 108;
    private static final int PATH_POSTS_ITEM = 109;

    // dots
    private static final int PATH_DOTS = 208;
    private static final int PATH_DOTS_ITEM = 209;

    // tags
    private static final int PATH_TAGS = 308;
    private static final int PATH_TAGS_ITEM = 309;

    // uri patterns matcher
    private static final UriMatcher sUriMatcher;

    static {
        String authority = AppConstants.API.CONTENT_AUTHORITY.getValue();
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // posts handler
        sUriMatcher.addURI(authority, SchemaPosts.PATH_POSTS, PATH_POSTS);
        sUriMatcher.addURI(authority, SchemaPosts.PATH_POSTS + "/#", PATH_POSTS_ITEM);

        // dots handler
        sUriMatcher.addURI(authority, SchemaDots.PATH_DOTS, PATH_DOTS);
        sUriMatcher.addURI(authority, SchemaDots.PATH_DOTS + "/#", PATH_DOTS_ITEM);

        // tags handler
        sUriMatcher.addURI(authority, SchemaTags.PATH_TAGS, PATH_TAGS);
        sUriMatcher.addURI(authority, SchemaTags.PATH_TAGS + "/#", PATH_TAGS_ITEM);
    }

    DBHelper mHelper;

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PATH_POSTS:
            case PATH_POSTS_ITEM:
                return SchemaPosts.PATH_POSTS;
            case PATH_DOTS:
            case PATH_DOTS_ITEM:
                return SchemaDots.PATH_DOTS;
            case PATH_TAGS:
            case PATH_TAGS_ITEM:
                return SchemaTags.PATH_TAGS;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case PATH_POSTS:
            case PATH_POSTS_ITEM:
                qb.setTables(SchemaPosts.VIEW_CREATE_POST_WITH_DOT_NAME);
                break;
            case PATH_DOTS:
            case PATH_DOTS_ITEM:
                qb.setTables(SchemaDots.TABLE_NAME);
                break;
            case PATH_TAGS:
            case PATH_TAGS_ITEM:
                qb.setTables(SchemaTags.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        return qb.query(mHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db;
        Long rowId = null;
        switch (sUriMatcher.match(uri)) {
            case PATH_POSTS:
            case PATH_POSTS_ITEM:
                db = mHelper.getWritableDatabase();
                rowId = db.replaceOrThrow(SchemaPosts.TABLE_NAME, "", values);
                break;
            case PATH_DOTS:
            case PATH_DOTS_ITEM:
                db = mHelper.getWritableDatabase();
                rowId = db.replaceOrThrow(SchemaDots.TABLE_NAME, "", values);
                break;
            case PATH_TAGS:
            case PATH_TAGS_ITEM:
                db = mHelper.getWritableDatabase();
                rowId = db.replaceOrThrow(SchemaTags.TABLE_NAME, "", values);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        validateInsert(rowId);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, Long.toString(rowId));
    }

    private void validateInsert(long rowId) {
        if (rowId > 0) {
            return;
        }

        throw new IllegalStateException("failed to insert.");
    }

    /**
     * Performs the work provided in a single transaction
     */
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations) {
        Long rowId = null;

        ContentProviderResult[] result = new ContentProviderResult[operations
                .size()];
        int i = 0;
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mHelper.getWritableDatabase();
        // Begin a transaction
        db.beginTransaction();
        try {
            for (ContentProviderOperation operation : operations) {
                // Chain the result for back references
                result[i++] = operation.apply(this, result, i);
            }

            db.setTransactionSuccessful();
        } catch (OperationApplicationException e) {
            Log.d(LOG_TAG, "batch failed: " + e.getLocalizedMessage());
        } finally {
            db.endTransaction();
        }

        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static class DBHelper extends SQLiteOpenHelper {
        public static final String LOG_TAG = "DBHelper";
        static final String DATABASE_NAME = "droid.samepinch.co.app.db";
        static final int DATABASE_VERSION = 50;

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);

        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SchemaPosts.TABLE_CREATE);
            sqLiteDatabase.execSQL(SchemaDots.TABLE_CREATE);
            sqLiteDatabase.execSQL(SchemaTags.TABLE_CREATE);


            // VIEWS
            sqLiteDatabase.execSQL(SchemaPosts.VIEW_CREATE_POST_WITH_DOT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version "
                    + oldVersion + " to "
                    + newVersion + " which destroys all old data");

            sqLiteDatabase.execSQL(SchemaPosts.TABLE_DROP);
            sqLiteDatabase.execSQL(SchemaDots.TABLE_DROP);
            sqLiteDatabase.execSQL(SchemaTags.TABLE_DROP);

            // VIEWS
            sqLiteDatabase.execSQL(SchemaPosts.VIEW_DROP_POST_WITH_DOT);

            // create table(s)
            onCreate(sqLiteDatabase);
        }

    }
}