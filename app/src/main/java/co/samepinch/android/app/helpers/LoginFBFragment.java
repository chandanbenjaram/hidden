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
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.intent.FBAuthService;
import co.samepinch.android.app.helpers.intent.SignOutService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class LoginFBFragment extends android.support.v4.app.Fragment {
    public static final String TAG = "LoginFBFragment";
    private static final int CHOOSE_PINCH_HANDLE = 77;

    @Bind(R.id.fb_login_button)
    LoginButton loginButton;

    CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    public LoginFBFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fallback
        if (!FacebookSdk.isInitialized()) {
            Fresco.initialize(getActivity().getApplicationContext());
        }

        // tracker
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                fetchUserInfo();
            }
        };

        // callback
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_PINCH_HANDLE) {
            String pinchHandle = data.getStringExtra("PINCH_HANDLE");
        }

    }

    private void fetchUserInfo() {
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

                Map<String, String> msg = new HashMap<>();
                msg.put(AppConstants.K.MESSAGE.name(), "facebook log-in successful.\nlogging into SamePinch...hang-on!");
                BusProvider.INSTANCE.getBus().post(new Events.MessageEvent(msg));

                if (true) {
                    // TARGET
                    Bundle args = new Bundle();
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_CHOOSE_HANDLE.name());
                    // intent
                    Intent intent = new Intent(getActivity().getApplicationContext(), ActivityFragment.class);
                    intent.putExtras(args);
                    startActivityForResult(intent, CHOOSE_PINCH_HANDLE);
                } else {
                    // call for intent
                    Intent mServiceIntent =
                            new Intent(getActivity(), FBAuthService.class);
                    mServiceIntent.putExtras(iArgs);
                    getActivity().startService(mServiceIntent);
                }
            }
        });
        request.executeAsync();
    }

    @Subscribe
    public void onAuthFailEvent(final Events.AuthFailEvent event) {
        Map<String, String> eventData = event.getMetaData();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoginManager.getInstance().logOut();
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
