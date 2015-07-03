package droid.samepinch.co.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import droid.samepinch.co.data.dao.IPostSchema;
import droid.samepinch.co.data.dao.IUserSchema;

/**
 * Created by imaginationcoder on 6/24/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = "DBHelper";
    static final String DATABASE_NAME = "samepinchapp.db";
    static final int DATABASE_VERSION = 7;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(IPostSchema.POST_TABLE_CREATE);
        sqLiteDatabase.execSQL(IUserSchema.USER_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version "
                + oldVersion + " to "
                + newVersion + " which destroys all old data");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IPostSchema.POST_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IUserSchema.USER_TABLE);

        // create table(s)
        onCreate(sqLiteDatabase);
    }


}