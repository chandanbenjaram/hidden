package co.samepinch.android.app;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;

import co.samepinch.android.app.helpers.Utils;

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
