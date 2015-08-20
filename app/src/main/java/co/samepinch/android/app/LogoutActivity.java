package co.samepinch.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.intent.SignOutService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class LogoutActivity extends AppCompatActivity {
    public static final String LOG_TAG = "LogoutActivity";

    @Bind(R.id.txt_logout_text)
    TextView mLogoutText;

    @Bind(R.id.btn_tryagain)
    Button mTryAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout);

        ButterKnife.bind(LogoutActivity.this);
        BusProvider.INSTANCE.getBus().register(this);

        // call for intent
        Intent mServiceIntent =
                new Intent(LogoutActivity.this, SignOutService.class);
        startService(mServiceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.INSTANCE.getBus().unregister(this);
    }


    @OnClick(R.id.btn_tryagain)
    public void tryAgainListener(){
        mLogoutText.setText("retrying...");

        // call for intent
        Intent mServiceIntent =
                new Intent(LogoutActivity.this, SignOutService.class);
        startService(mServiceIntent);
    }

    @Subscribe
    public void onAuthOutEvent(final Events.AuthOutEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK);
                finish();
            }
        });
    }


    @Subscribe
    public void onAuthOutFailEvent(final Events.AuthOutFailEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogoutText.setText("something went wrong. try again.");
                mTryAgain.setVisibility(View.VISIBLE);
            }
        });
    }
}
