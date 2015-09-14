package co.samepinch.android.app.helpers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.intent.FBAuthService;
import co.samepinch.android.app.helpers.intent.SignOutService;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

public class LoginFBFragment extends android.support.v4.app.Fragment {
    public static final String TAG = "LoginFBFragment";

    @Bind(R.id.fb_login_button)
    LoginButton loginButton;

    CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private ProgressDialog progressDialog;
    private JSONObject fbUserObject;
    private String fbUserImg;

    private LocalHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes.
        setRetainInstance(true);

        // fallback
        if (!FacebookSdk.isInitialized()) {
            Fresco.initialize(getActivity().getApplicationContext());
        }

        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);

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

        mHandler = new LocalHandler(this);
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

        if (requestCode == Integer.parseInt(AppConstants.APP_INTENT.CHOOSE_PINCH_HANDLE.getValue())) {
            try {
                fbUserObject.put("image", fbUserImg);
                fbUserObject.put("pinch_handle", data.getStringExtra("PINCH_HANDLE"));

                Bundle iArgs = new Bundle();
                iArgs.putString(AppConstants.K.provider.name(), AppConstants.K.facebook.name());
                iArgs.putSerializable("user", (Serializable) FBAuthService.toMap(fbUserObject));
                // call for intent
                Intent mServiceIntent =
                        new Intent(getActivity(), FBAuthService.class);
                mServiceIntent.putExtras(iArgs);
                getActivity().startService(mServiceIntent);
            } catch (Exception e) {
                Utils.dismissSilently(progressDialog);
                LoginManager.getInstance().logOut();
            }
        }
    }

    private void fetchUserInfo() {
        Utils.dismissSilently(progressDialog);
        if (AccessToken.getCurrentAccessToken() == null) {
            return;
        }

        final String userId = AccessToken.getCurrentAccessToken().getUserId();
        progressDialog.setMessage("facebook sign-in successful");
        progressDialog.show();

        GraphRequest meReq = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject jsonObject,
                            GraphResponse response) {
                        fbUserObject = jsonObject;
                    }
                });

        String imgQuery = String.format("me", userId);
        Bundle imgReqParams = new Bundle();
        imgReqParams.putString("fields", "picture.width(999)");

        GraphRequest imageReq = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), "me", new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                try {
                    fbUserImg = response.getJSONObject().getJSONObject("picture").getJSONObject("data").getString("url");
                } catch (Exception e) {
                    fbUserImg = "";
                }
            }
        });
        imageReq.setParameters(imgReqParams);

        GraphRequestBatch batch = new GraphRequestBatch(meReq, imageReq);
        batch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequests) {
                // Application code for when the batch finishes
                Log.d(TAG, graphRequests == null ? "true" : "false");
                new CheckExistenceTask().execute(userId);
            }
        });
        batch.executeAsync();
    }

    @Subscribe
    public void onAuthFailEvent(final Events.AuthFailEvent event) {
        Map<String, String> metaData = event.getMetaData();
        if (metaData != null && StringUtils.isNotBlank(metaData.get(AppConstants.K.MESSAGE.name()))) {
            Snackbar.make(loginButton, event.getMetaData().get(AppConstants.K.MESSAGE.name()), Snackbar.LENGTH_SHORT).show();
        }

        socialSignOutLocally(event.getMetaData());
    }

    @Subscribe
    public void onAuthOutEvent(final Events.AuthOutEvent event) {
        socialSignOutLocally(event.getMetaData());
    }

    @Subscribe
    public void onAuthOutFailEvent(final Events.AuthOutFailEvent event) {
        socialSignOutLocally(event.getMetaData());
    }

    private void socialSignOutLocally(Map<String, String> metaData) {
        String provider;
        if (metaData == null || (provider = metaData.get(AppConstants.K.provider.name())) == null) {
            return;
        }

        if (StringUtils.equals(provider, AppConstants.K.facebook.name())) {
            Utils.dismissSilently(progressDialog);
            LoginManager.getInstance().logOut();
        }
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

        Utils.dismissSilently(progressDialog);
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

    private class CheckExistenceTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("locating your account");
        }

        @Override
        protected Boolean doInBackground(String... args) {
            StorageComponent component = DaggerStorageComponent.create();
            ReqSetBody req = component.provideReqSetBody();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("isUserExists");

            Map<String, String> body = new HashMap<>();
            body.put(AppConstants.K.provider.name(), AppConstants.K.facebook.name());
            body.put("oauth_uid", args[0]);
            req.setBody(body);

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            try {
                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);
                if (resp.getBody() != null) {
                    return resp.getBody().getStatus() == 400;
                }
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                if (resp != null && resp.getStatus() == 400) {
                    return Boolean.TRUE;
                } else {
                    Log.e(TAG, "err validating...");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result != null) {
                if (result.booleanValue()) {
                    mHandler.sendEmptyMessage(1);
                } else {
                    mHandler.sendEmptyMessage(0);
                }
            } else {
                mHandler.sendEmptyMessage(-1);
            }
        }
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<LoginFBFragment> mActivity;
        public LocalHandler(LoginFBFragment parent) {
            mActivity = new WeakReference<LoginFBFragment>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // call for intent
            Intent signOutIntent =
                    new Intent(mActivity.get().getActivity(), SignOutService.class);
            switch (msg.what) {
                case 0:
                    mActivity.get().progressDialog.setMessage("setting up your account...");
                    Bundle args = new Bundle();
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_CHOOSE_HANDLE.name());
                    try {
                        args.putString("fname", mActivity.get().fbUserObject.getString("first_name"));
                        args.putString("lname", mActivity.get().fbUserObject.getString("last_name"));
                        args.putString("image", mActivity.get().fbUserImg);
                    } catch (Exception e) {
                        // muted
                    }
                    // intent
                    Intent intent = new Intent(mActivity.get().getActivity().getApplicationContext(), ActivityFragment.class);
                    intent.putExtras(args);
                    mActivity.get().startActivityForResult(intent, Integer.parseInt(AppConstants.APP_INTENT.CHOOSE_PINCH_HANDLE.getValue()));
                    break;
                case 1:
                    mActivity.get().progressDialog.setMessage("found your account.\nsigning-in...");
                    Bundle iArgs = new Bundle();
                    if (mActivity.get().fbUserObject != null) {
                        iArgs.putString(AppConstants.K.provider.name(), AppConstants.K.facebook.name());
                        try {
                            mActivity.get().fbUserObject.put("image", mActivity.get().fbUserImg);
                            iArgs.putSerializable("user", (Serializable) FBAuthService.toMap(mActivity.get().fbUserObject));
                        } catch (Exception e) {
                            // muted
                        }
                    } else {
                        mActivity.get().progressDialog.setMessage("sign-in failed\ntry again");
                        Utils.dismissSilently(mActivity.get().progressDialog);
                        // call for intent
                        mActivity.get().getActivity().startService(signOutIntent);
                        return;
                    }
                    // call for intent
                    Intent mServiceIntent =
                            new Intent(mActivity.get().getActivity(), FBAuthService.class);
                    mServiceIntent.putExtras(iArgs);
                    mActivity.get().getActivity().startService(mServiceIntent);
                    break;
                default:
                    mActivity.get().progressDialog.setMessage("sign-in failed\ntry again");
                    Utils.dismissSilently(mActivity.get().progressDialog);
                    mActivity.get().getActivity().startService(signOutIntent);
                    break;
            }
        }
    };
}
