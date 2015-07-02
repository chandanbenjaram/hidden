package droid.samepinch.co.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by cbenjaram on 7/1/15.
 */
public abstract class DBContentProvider {
    public SQLiteDatabase mDb;

    public DBContentProvider(SQLiteDatabase db) {
        this.mDb = db;
    }

    protected abstract <T> T cursorToEntity(Cursor cursor);

    public int delete(String tableName, String selection,
                      String[] selectionArgs) {
        return mDb.delete(tableName, selection, selectionArgs);
    }

    public long insertOrThrow(String tableName, ContentValues values) {
        return mDb.insertOrThrow(tableName, null, values);
    }

    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String sortOrder) {

        final Cursor cursor = mDb.query(tableName, columns,
                selection, selectionArgs, null, null, sortOrder);

        return cursor;
    }

    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String sortOrder,
                        String limit) {

        return mDb.query(tableName, columns, selection,
                selectionArgs, null, null, sortOrder, limit);
    }

    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {

        return mDb.query(tableName, columns, selection,
                selectionArgs, groupBy, having, orderBy, limit);
    }

    public int update(String tableName, ContentValues values,
                      String selection, String[] selectionArgs) {
        return mDb.update(tableName, values, selection,
                selectionArgs);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDb.rawQuery(sql, selectionArgs);
    }
}
