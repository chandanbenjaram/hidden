package co.samepinch.android.app;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.intent.SignOutService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class LogoutActivity extends AppCompatActivity  {

    // For communicating with Google APIs
    private GoogleApiClient mGoogleApiClient;

    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout);

        BusProvider.INSTANCE.getBus().register(this);

        // call for intent
        Intent mServiceIntent =
                new Intent(this, SignOutService.class);
        startService(mServiceIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @Subscribe
    public void onAuthOutEvent(final Events.AuthOutEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

}
