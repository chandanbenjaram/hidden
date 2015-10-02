package co.samepinch.android.data.dao;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;

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
    private static final int PATH_POST_DETAILS = 1080;
    private static final int PATH_POST_DETAILS_ITEM = 1090;

    // dots
    private static final int PATH_DOTS = 208;
    private static final int PATH_DOTS_ITEM = 209;

    // tags
    private static final int PATH_TAGS = 308;
    private static final int PATH_TAGS_ITEM = 309;

    private static final int PATH_COMMENTS = 408;
    private static final int PATH_COMMENTS_ITEM = 409;

    // uri patterns matcher
    private static final UriMatcher sUriMatcher;

    static {
        String authority = AppConstants.API.CONTENT_AUTHORITY.getValue();
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // posts handler
        sUriMatcher.addURI(authority, SchemaPosts.PATH_POSTS, PATH_POSTS);
        sUriMatcher.addURI(authority, SchemaPosts.PATH_POSTS + "/#", PATH_POSTS_ITEM);

        // post details handler
        sUriMatcher.addURI(authority, SchemaPostDetails.PATH_POST_DETAILS, PATH_POST_DETAILS);
        sUriMatcher.addURI(authority, SchemaPostDetails.PATH_POST_DETAILS + "/#", PATH_POST_DETAILS_ITEM);

        // dots handler
        sUriMatcher.addURI(authority, SchemaDots.PATH_DOTS, PATH_DOTS);
        sUriMatcher.addURI(authority, SchemaDots.PATH_DOTS + "/#", PATH_DOTS_ITEM);

        // tags handler
        sUriMatcher.addURI(authority, SchemaTags.PATH_TAGS, PATH_TAGS);
        sUriMatcher.addURI(authority, SchemaTags.PATH_TAGS + "/#", PATH_TAGS_ITEM);

        // comments handler
        sUriMatcher.addURI(authority, SchemaComments.PATH_COMMENTS, PATH_COMMENTS);
        sUriMatcher.addURI(authority, SchemaComments.PATH_COMMENTS + "/#", PATH_COMMENTS_ITEM);
    }

    DBHelper mHelper;

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PATH_POSTS:
            case PATH_POSTS_ITEM:
                return SchemaPosts.PATH_POSTS;
            case PATH_POST_DETAILS:
            case PATH_POST_DETAILS_ITEM:
                return SchemaPostDetails.PATH_POST_DETAILS;
            case PATH_DOTS:
            case PATH_DOTS_ITEM:
                return SchemaDots.PATH_DOTS;
            case PATH_TAGS:
            case PATH_TAGS_ITEM:
                return SchemaTags.PATH_TAGS;
            case PATH_COMMENTS:
            case PATH_COMMENTS_ITEM:
                return SchemaComments.PATH_COMMENTS;
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
                if (sortOrder == null) {
                    sortOrder = SchemaPosts.COLUMN_CREATED_AT + " desc";
                }
                break;
            case PATH_POST_DETAILS:
            case PATH_POST_DETAILS_ITEM:
                qb.setTables(SchemaPostDetails.TABLE_NAME);
                break;
            case PATH_DOTS:
            case PATH_DOTS_ITEM:
                qb.setTables(SchemaDots.TABLE_NAME);
                break;
            case PATH_TAGS:
            case PATH_TAGS_ITEM:
                qb.setTables(SchemaTags.TABLE_NAME);
                break;
            case PATH_COMMENTS:
            case PATH_COMMENTS_ITEM:
                qb.setTables(SchemaComments.TABLE_NAME);
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
        Map<String, String> selectionArgsMap = new HashMap<>();
        try {
            switch (sUriMatcher.match(uri)) {
                case PATH_POSTS:
                case PATH_POSTS_ITEM:
                    selectionArgsMap.put(SchemaPosts.COLUMN_UID + "=?", values.getAsString(SchemaPosts.COLUMN_UID));
                    db = mHelper.getWritableDatabase();
                    rowId = db.insertWithOnConflict(SchemaPosts.TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_FAIL);
                    break;
                case PATH_POST_DETAILS:
                case PATH_POST_DETAILS_ITEM:
                    selectionArgsMap.put(SchemaPostDetails.COLUMN_UID + "=?", values.getAsString(SchemaPostDetails.COLUMN_UID));
                    db = mHelper.getWritableDatabase();
                    rowId = db.insertWithOnConflict(SchemaPostDetails.TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_FAIL);
                    break;
                case PATH_DOTS:
                case PATH_DOTS_ITEM:
                    selectionArgsMap.put(SchemaDots.COLUMN_UID + "=?", values.getAsString(SchemaDots.COLUMN_UID));
                    db = mHelper.getWritableDatabase();
                    rowId = db.insertWithOnConflict(SchemaDots.TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_FAIL);
                    break;
                case PATH_TAGS:
                case PATH_TAGS_ITEM:
                    selectionArgsMap.put(SchemaTags.COLUMN_NAME + "=?", values.getAsString(SchemaTags.COLUMN_NAME));
                    db = mHelper.getWritableDatabase();
                    rowId = db.insertWithOnConflict(SchemaTags.TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_FAIL);
                    break;
                case PATH_COMMENTS:
                case PATH_COMMENTS_ITEM:
                    selectionArgsMap.put(SchemaComments.COLUMN_UID + "=?", values.getAsString(SchemaComments.COLUMN_UID));
                    db = mHelper.getWritableDatabase();
                    rowId = db.insertWithOnConflict(SchemaComments.TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_FAIL);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown uri: " + uri);
            }
        } catch (SQLiteConstraintException e) {
            String selection = null;
            String selectionArgs = null;
            // try update
            for (Map.Entry<String, String> aEntry : selectionArgsMap.entrySet()) {
                selection = aEntry.getKey();
                selectionArgs = aEntry.getValue();
                break;
            }
            update(uri, values, selection, new String[]{selectionArgs});
            Cursor curr = query(uri, new String[]{BaseColumns._ID}, selection, new String[]{selectionArgs}, null);
            if (curr.moveToFirst()) {
                rowId = curr.getLong(curr.getColumnIndex(BaseColumns._ID));
            }
            curr.close();
        }
        validateInsert(rowId);

        final Uri itemUri= ContentUris.withAppendedId(uri, rowId);
        getContext().getContentResolver().notifyChange(itemUri, null);
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
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int delRowsCount = 0;
        String tableName = null;
        switch (sUriMatcher.match(uri)) {
            case PATH_POSTS:
            case PATH_POSTS_ITEM:
                tableName = SchemaPosts.TABLE_NAME;
                break;
            case PATH_POST_DETAILS:
            case PATH_POST_DETAILS_ITEM:
                tableName = SchemaPostDetails.TABLE_NAME;
                break;
            case PATH_DOTS:
            case PATH_DOTS_ITEM:
                tableName = SchemaDots.TABLE_NAME;
                break;
            case PATH_TAGS:
            case PATH_TAGS_ITEM:
                tableName = SchemaTags.TABLE_NAME;
                break;
            case PATH_COMMENTS:
            case PATH_COMMENTS_ITEM:
                tableName = SchemaComments.TABLE_NAME;
                break;

            default:
                throw new IllegalArgumentException("un-known uri: " + uri);
        }

        delRowsCount = db.delete(tableName, selection, selectionArgs);
        if (delRowsCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return delRowsCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int updatedRowsCount = 0;
        String tableName = null;
        switch (sUriMatcher.match(uri)) {
            case PATH_POSTS:
            case PATH_POSTS_ITEM:
                tableName = SchemaPosts.TABLE_NAME;
                break;
            case PATH_POST_DETAILS:
            case PATH_POST_DETAILS_ITEM:
                tableName = SchemaPostDetails.TABLE_NAME;
                break;
            case PATH_DOTS:
            case PATH_DOTS_ITEM:
                tableName = SchemaDots.TABLE_NAME;
                break;
            case PATH_TAGS:
            case PATH_TAGS_ITEM:
                tableName = SchemaTags.TABLE_NAME;
                break;
            case PATH_COMMENTS:
            case PATH_COMMENTS_ITEM:
                tableName = SchemaComments.TABLE_NAME;
                break;

            default:
                throw new IllegalArgumentException("un-known uri: " + uri);
        }

        updatedRowsCount = db.update(tableName, values, selection, selectionArgs);
        if (updatedRowsCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRowsCount;
    }

    public static class DBHelper extends SQLiteOpenHelper {
        public static final String LOG_TAG = "DBHelper";
        static final String DATABASE_NAME = "co.samepinch.android.app.db";
        static final int DATABASE_VERSION = 103;

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
            sqLiteDatabase.execSQL(SchemaPostDetails.TABLE_CREATE);

            sqLiteDatabase.execSQL(SchemaDots.TABLE_CREATE);
            sqLiteDatabase.execSQL(SchemaTags.TABLE_CREATE);
            sqLiteDatabase.execSQL(SchemaComments.TABLE_CREATE);

            // VIEWS
            sqLiteDatabase.execSQL(SchemaPosts.VIEW_CREATE_POST_WITH_DOT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version "
                    + oldVersion + " to "
                    + newVersion + " which destroys all old data");

            sqLiteDatabase.execSQL(SchemaPosts.TABLE_DROP);
            sqLiteDatabase.execSQL(SchemaPostDetails.TABLE_DROP);

            sqLiteDatabase.execSQL(SchemaDots.TABLE_DROP);
            sqLiteDatabase.execSQL(SchemaTags.TABLE_DROP);

            sqLiteDatabase.execSQL(SchemaComments.TABLE_DROP);

            // VIEWS
            sqLiteDatabase.execSQL(SchemaPosts.VIEW_DROP_POST_WITH_DOT);

            // create table(s)
            onCreate(sqLiteDatabase);
        }

    }
}