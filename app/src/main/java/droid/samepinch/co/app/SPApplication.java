package droid.samepinch.co.app;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;

import droid.samepinch.co.app.helpers.Utils;

/**
 * Created by cbenjaram on 8/6/15.
 */
public class SPApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(getApplicationContext());
        Utils.PreferencesManager.initializeInstance(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
