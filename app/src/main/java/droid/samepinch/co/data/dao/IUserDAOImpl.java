package droid.samepinch.co.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import droid.samepinch.co.data.DBContentProvider;
import droid.samepinch.co.data.dto.User;

/**
 * Created by cbenjaram on 7/1/15.
 */
public class IUserDAOImpl extends DBContentProvider implements IUserDAO, IUserSchema {
    private Cursor cursor;
    private ContentValues initialValues;

    public IUserDAOImpl(SQLiteDatabase db) {
        super(db);
    }

    @Override
    protected User cursorToEntity(Cursor cursor) {
        User user = new User();
        if (cursor == null) {
            return user;
        }

        int idIndex;
        int fNameIndex;
        int lNameIndex;
        int prefNameIndex;
        int pinchHandleIndex;
        if (cursor.getColumnIndex(COLUMN_UID) != -1) {
            idIndex = cursor.getColumnIndexOrThrow(COLUMN_UID);
            user.setUid(cursor.getString(idIndex));
        }
        if (cursor.getColumnIndex(COLUMN_FNAME) != -1) {
            fNameIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_FNAME);
            user.setFname(cursor.getString(fNameIndex));
        }
        if (cursor.getColumnIndex(COLUMN_LNAME) != -1) {
            lNameIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_LNAME);
            user.setLname(cursor.getString(lNameIndex));
        }
        if (cursor.getColumnIndex(COLUMN_PREF_NAME) != -1) {
            prefNameIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_LNAME);
            user.setPrefName(cursor.getString(prefNameIndex));
        }
        if (cursor.getColumnIndex(COLUMN_PINCH_HANDLE) != -1) {
            pinchHandleIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_LNAME);
            user.setPrefName(cursor.getString(pinchHandleIndex));
        }
        return user;
    }

    @Override
    public User fetchUserById(String userId) {
        final String selectionArgs[] = {userId};
        final String selection = _ID + " = ?";

        User user = new User();
        cursor = super.query(USER_TABLE, USER_COLUMNS, selection,
                selectionArgs, _ID);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                user = cursorToEntity(cursor);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return user;
    }

    @Override
    public List<User> fetchAllUsers() {
        List<User> userList = new ArrayList<>();
        cursor = super.query(USER_TABLE, USER_COLUMNS, null,
                null, _ID);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                User user = cursorToEntity(cursor);
                userList.add(user);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return userList;
    }

    @Override
    public boolean addUser(User user) {
        // set values
        setContentValue(user);
        try {
            return super.insertOrThrow(USER_TABLE, getContentValue()) > 0;
        } catch (SQLiteConstraintException ex) {
            Log.w("Database", ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean addUsers(List<User> users) {
        return false;
    }

    @Override
    public boolean deleteAllUsers() {
        return false;
    }

    private ContentValues getContentValue() {
        return initialValues;
    }

    private void setContentValue(User user) {
        initialValues = new ContentValues();
        initialValues.put(COLUMN_UID, user.getUid());
        initialValues.put(COLUMN_FNAME, user.getFname());
        initialValues.put(COLUMN_LNAME, user.getLname());
        initialValues.put(COLUMN_PREF_NAME, user.getPrefName());
        initialValues.put(COLUMN_PINCH_HANDLE, user.getPinchHandle());
    }
}
