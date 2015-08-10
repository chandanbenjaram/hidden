package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.otto.Subscribe;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.intent.AuthService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_EMAIL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PASSWORD;

public class LoginEMailFragment extends android.support.v4.app.Fragment {
    public static final String LOG_TAG = "DotWallFragment";
    AppCompatActivity activity;
    View mView;

    @Bind(R.id.email_id)
    EditText mEmailIdView;

    @Bind(R.id.password_id)
    EditText mPasswordView;


    @Bind(R.id.button_login)
    Button mLoginButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.login_email, container, false);
        ButterKnife.bind(this, mView);

        return mView;
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @OnClick(R.id.button_login)
    public void onLogin() {
        //call to authenticate
        mEmailIdView.setEnabled(false);
        mPasswordView.setEnabled(false);
        callForAuth();
    }

    private void callForAuth() {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_EMAIL.getValue(), mEmailIdView.getText().toString());
        iArgs.putString(KEY_PASSWORD.getValue(), mPasswordView.getText().toString());

        // call for intent
        Intent mServiceIntent =
                new Intent(activity, AuthService.class);
        mServiceIntent.putExtras(iArgs);
        activity.startService(mServiceIntent);
    }

    @Subscribe
    public void onAuthSuccessEvent(final Events.AuthSuccessEvent event) {
        Map<String, String> eventData = event.getMetaData();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoginButton.setText("logout");
            }
        });
    }

    @Subscribe
    public void onAuthFailEvent(final Events.AuthFailEvent event) {
        Map<String, String> eventData = event.getMetaData();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmailIdView.setEnabled(true);
                mPasswordView.setEnabled(true);
                mLoginButton.setText("login");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.INSTANCE.getBus().register(this);

        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
