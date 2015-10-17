package co.samepinch.android.app;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseInstallation;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;

/**
 * Created by cbenjaram on 8/6/15.
 */
public class SPApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // others
        Utils.PreferencesManager.initializeInstance(mContext);
        Fresco.initialize(mContext);
        FacebookSdk.sdkInitialize(mContext);

        // parse hash
        Parse.initialize(this, AppConstants.API.PARSE_APPLICATION_ID.getValue(), AppConstants.API.PARSE_CLIENT_KEY.getValue());
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static Context getContext() {
        return mContext;
    }
}
