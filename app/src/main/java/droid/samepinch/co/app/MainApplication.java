package droid.samepinch.co.app;

import android.app.Application;

import java.sql.SQLException;

import droid.samepinch.co.data.DB;

/**
 * Created by cbenjaram on 7/1/15.
 */
public class MainApplication extends Application {
    public static final String LOG_TAG = MainApplication.class.getSimpleName();
    public static DB mDb;


    @Override
    public void onCreate() {
        super.onCreate();
        mDb = new DB(this);
        try {
            mDb.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTerminate() {
        mDb.close();
        super.onTerminate();
    }
}
