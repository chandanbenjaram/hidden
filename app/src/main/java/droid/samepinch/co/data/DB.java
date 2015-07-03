package droid.samepinch.co.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

import droid.samepinch.co.data.dao.IPostDAOImpl;
import droid.samepinch.co.data.dao.IUserDAOImpl;

/**
 * Created by cbenjaram on 7/1/15.
 */
public class DB {
    static final String DATABASE_NAME = "samepinchapp.db";
    static final int DATABASE_VERSION = 7;
    public static IUserDAOImpl mUserDao;
    public static IPostDAOImpl mPostDAO;
    public static DBHelper mDBHelper;

    private final Context mContext;
    public DB(Context context) {
        this.mContext = context;
    }

    public DB open() throws SQLException {
        mDBHelper = new DBHelper(mContext);
        SQLiteDatabase mDB = mDBHelper.getWritableDatabase();

        // app DAOs
        mUserDao = new IUserDAOImpl();
        mUserDao.onCreate();

        mPostDAO = new IPostDAOImpl();
        mPostDAO.onCreate();

        return this;
    }

    public void close() {
        mDBHelper.close();
    }
}
