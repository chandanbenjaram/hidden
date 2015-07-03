package droid.samepinch.co.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by cbenjaram on 7/1/15.
 */
public abstract class DBContentProvider extends ContentProvider {
    public DBHelper mDBHelper;

    public abstract <T> T cursorToEntity(Cursor cursor);

    public int delete(String tableName, String selection,
                      String[] selectionArgs) {
        return mDBHelper.getWritableDatabase().delete(tableName, selection, selectionArgs);
    }

    public long insertOrThrow(String tableName, ContentValues values) {
        return mDBHelper.getWritableDatabase().insertOrThrow(tableName, null, values);
    }

    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String sortOrder) {

        final Cursor cursor = mDBHelper.getReadableDatabase().query(tableName, columns,
                selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String sortOrder,
                        String limit) {

        return mDBHelper.getReadableDatabase().query(tableName, columns, selection,
                selectionArgs, null, null, sortOrder, limit);
    }

    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {

        return mDBHelper.getReadableDatabase().query(tableName, columns, selection,
                selectionArgs, groupBy, having, orderBy, limit);
    }

    public int update(String tableName, ContentValues values,
                      String selection, String[] selectionArgs) {
        return mDBHelper.getWritableDatabase().update(tableName, values, selection,
                selectionArgs);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDBHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext());
        return true;
    }

}
