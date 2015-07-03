package droid.samepinch.co.data.dao;

import android.net.Uri;

import droid.samepinch.co.app.helpers.AppConstants;

/**
 * Created by cbenjaram on 7/2/15.
 */
public interface IContract {
    String CONTENT_AUTHORITY = AppConstants.API.CONTENT_AUTHORITY.getValue();
    Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
