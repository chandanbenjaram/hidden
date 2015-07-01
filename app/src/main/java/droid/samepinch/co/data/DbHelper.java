package droid.samepinch.co.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by imaginationcoder on 6/24/15.
 */
public class DbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "samepinchapp.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_POSTS_TABLE = "CREATE TABLE " + WallContract.Post.TABLE_NAME + " (" +
                WallContract.Post._ID + " INTEGER PRIMARY KEY, " +
                WallContract.Post.COLUMN_POST_ID + " INTEGER NOT NULL, " +
                WallContract.Post.COLUMN_CONTENT + " TEXT NOT NULL" +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WallContract.Post.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}