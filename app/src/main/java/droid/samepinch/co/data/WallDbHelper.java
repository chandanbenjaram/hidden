package droid.samepinch.co.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by imaginationcoder on 6/24/15.
 */
public class WallDbHelper extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "samepinch.db";

    public WallDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_POSTS_TABLE = "CREATE TABLE " + WallContract.Posts.TABLE_NAME + " (" +
                WallContract.Posts._ID + " INTEGER PRIMARY KEY, " +
                WallContract.Posts.COLUMN_POST_ID + " INTEGER NOT NULL, " +
                WallContract.Posts.COLUMN_CONTENT + " TEXT NOT NULL" +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WallContract.Posts.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}