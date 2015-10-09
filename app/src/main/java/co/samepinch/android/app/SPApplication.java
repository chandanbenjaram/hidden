package co.samepinch.android.app;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.parse.Parse;
import com.parse.ParseInstallation;

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

        Parse.initialize(this, "kx7vlz5uxqBOWZ2aJ5FOoSMQayTYHw3Gf6QWmm9R", "iRdEkkVb2t299QiSkRhcdoZmZm6LLhGSrtgqiEtz");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static Context getContext() {
        return mContext;
    }
}
