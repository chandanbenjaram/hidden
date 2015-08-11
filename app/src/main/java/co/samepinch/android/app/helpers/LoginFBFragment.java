package co.samepinch.android.app.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.intent.FBAuthService;
import co.samepinch.android.app.helpers.intent.SignOutService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class LoginFBFragment extends android.support.v4.app.Fragment {
    public static final String LOG_TAG = "LoginFBFragment";
    private static final String PROVIDER = "facebook";

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
                fetchUserInfo();
            }
        };

        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            fetchUserInfo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fb, container, false);
        ButterKnife.bind(LoginFBFragment.this, view);

        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.setFragment(LoginFBFragment.this);

        return view;
    }

    private void fetchUserInfo() {
        System.out.println("fetching fb user...");
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            // call for intent
            Intent mServiceIntent =
                    new Intent(getActivity(), SignOutService.class);
            getActivity().startService(mServiceIntent);
            return;
        }
        // login user
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                Bundle iArgs = new Bundle();
                if (user != null) {
                    iArgs.putString("user", user.toString());
                }
                // call for intent
                Intent mServiceIntent =
                        new Intent(getActivity(), FBAuthService.class);
                mServiceIntent.putExtras(iArgs);
                getActivity().startService(mServiceIntent);
            }
        });
        request.executeAsync();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Subscribe
    public void onAuthSuccessEvent(final Events.AuthSuccessEvent event) {
        Map<String, String> eventData = event.getMetaData();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Subscribe
    public void onAuthFailEvent(final Events.AuthFailEvent event) {
        Map<String, String> eventData = event.getMetaData();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Subscribe
    public void onAuthOutEvent(final Events.AuthOutEvent event) {
        Map<String, String> eventData = event.getMetaData();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
    public void onDestroy() {
        super.onDestroy();
        if (accessTokenTracker.isTracking()) {
            accessTokenTracker.stopTracking();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
    }
}
