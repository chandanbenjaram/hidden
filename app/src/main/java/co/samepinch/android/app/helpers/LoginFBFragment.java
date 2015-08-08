package co.samepinch.android.app.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;

public class LoginFBFragment extends android.support.v4.app.Fragment {
    public static final String LOG_TAG = "DotWallFragment";

    @Bind(R.id.fb_login_button)
    LoginButton loginButton;

    CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    public LoginFBFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
//                fetchUserInfo();
//                updateUI();
            }
        };

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.v(LOG_TAG, "success");
                    }

                    @Override
                    public void onCancel() {
                        Log.v(LOG_TAG, "cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.v(LOG_TAG, "error");
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fb, container, false);
        ButterKnife.bind(LoginFBFragment.this, view);

        loginButton.setReadPermissions("user_friends");
        // If using in a fragment
        loginButton.setFragment(LoginFBFragment.this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(LOG_TAG, "success");
            }

            @Override
            public void onCancel() {
                Log.v(LOG_TAG, "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.v(LOG_TAG, "error");
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        accessTokenTracker.stopTracking();

        ButterKnife.unbind(this);
    }
}
