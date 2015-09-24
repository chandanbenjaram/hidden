package co.samepinch.android.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.intent.FBAuthService;
import co.samepinch.android.app.helpers.intent.SignOutService;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ServerAuthCodeCallbacks {
    public static final String TAG = "LoginActivity";

    private static final int RC_SIGN_IN = 10;

    @Bind(R.id.btn_signin_google)
    SignInButton gSignInButton;

    GoogleApiClient mGoogleApiClient;
    boolean mIsResolving = false;
    boolean mShouldResolve = false;

    ProgressDialog progressDialog;
    Map<String, String> gUserObject;

    private LocalHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(LoginActivity.this);
        BusProvider.INSTANCE.getBus().register(this);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);

        gSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGoogleApiClient =
                new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Plus.API)
                        .addScope(new Scope(Scopes.PROFILE))
                        .addScope(new Scope(Scopes.PLUS_LOGIN))
                        .build();

        mHandler = new LocalHandler(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @OnClick(R.id.btn_signin_google)
    public void onClickGoogleSignIn() {
        onSignInClicked();
    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.clearDefaultAccountAndReconnect();
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        } else if (requestCode == AppConstants.KV.REQUEST_SIGNUP.getIntValue()) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        } else if (requestCode == Integer.parseInt(AppConstants.APP_INTENT.CHOOSE_PINCH_HANDLE.getValue())) {
            try {
                gUserObject.put("pinch_handle", data.getStringExtra("PINCH_HANDLE"));
                Bundle iArgs = new Bundle();
                iArgs.putString(AppConstants.K.provider.name(), AppConstants.K.google.name());
                iArgs.putSerializable("user", (Serializable) gUserObject);
                // call for intent
                Intent mServiceIntent =
                        new Intent(getApplicationContext(), FBAuthService.class);
                mServiceIntent.putExtras(iArgs);
                startService(mServiceIntent);
            } catch (Exception e) {
                Utils.dismissSilently(progressDialog);
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle data) {
        progressDialog.setMessage("google sign-in successful");
        progressDialog.show();
//        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        Person.Name personName = person.getName();
        String fName = personName.getGivenName();
        String lName = personName.getFamilyName();
        Person.Image personImage = person.getImage();
        String imageUrl = personImage.getUrl();

        // clear user object
        if (gUserObject == null) {
            gUserObject = new HashMap<>();
        } else {
            gUserObject.clear();
        }
        gUserObject.put(AppConstants.K.provider.name(), AppConstants.K.google.name());
        gUserObject.put("oauth_uid", person.getId());
        gUserObject.put("fname", fName);
        gUserObject.put("lname", lName);
        gUserObject.put("email", email);
        gUserObject.put("rphoto", imageUrl);

        new CheckExistenceTask().execute(person.getId());
//        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
    }


    /**
     * Callback for suspension of current connection
     */
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
//                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
//            showSignedOutUI();
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.clearDefaultAccountAndReconnect();
            } else {
                mGoogleApiClient.connect();
            }
        }
    }

//    @Override
//    public void onResult(People.LoadPeopleResult peopleData) {
//        switch (peopleData.getStatus().getStatusCode()) {
//            case CommonStatusCodes.SUCCESS:
//                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
//                Log.d(TAG, "\n\n\n****** onResult person located " + person);
//                break;
//
//            case CommonStatusCodes.SIGN_IN_REQUIRED:
//                mGoogleApiClient.disconnect();
//                mGoogleApiClient.connect();
//                break;
//
//            default:
//                Log.e(TAG, "err while logging-in G+ user: " + peopleData.getStatus());
//                break;
//        }
//    }

    @Override
    public CheckResult onCheckServerAuthorization(String idToken, Set<Scope> scopeSet) {
        Log.i(TAG, "checking if server is authorized.");

        HashSet<Scope> serverScopeSet = new HashSet<Scope>();


        // Check if the server has a token.  Since this callback executes in a background
        // thread it is OK to do synchronous network access in this check.
        //boolean serverHasToken = serverHasTokenFor(idToken);
        boolean serverHasToken = false;
        Log.i(TAG, "server has token: " + String.valueOf(serverHasToken));

        if (!serverHasToken) {
            serverScopeSet.add(new Scope(Scopes.PLUS_LOGIN));
            serverScopeSet.add(new Scope(Scopes.PLUS_ME));
            serverScopeSet.add(new Scope(Scopes.PROFILE));
            // Server does not have a valid refresh token, so request a new
            // auth code which can be exchanged for one.  This will cause the user to see the
            // consent dialog and be prompted to grant offline access.

            // Ask the server which scopes it would like to have for offline access.  This
            // can be distinct from the scopes granted to the client.  By getting these values
            // from the server, you can change your server's permissions without needing to
            // recompile the client application.
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(SELECT_SCOPES_URL);
//            HashSet<Scope> serverScopeSet = new HashSet<Scope>();

//            try {
//                HttpResponse httpResponse = httpClient.execute(httpGet);
//                int responseCode = httpResponse.getStatusLine().getStatusCode();
//                String responseBody = EntityUtils.toString(httpResponse.getEntity());
//
//                // Convert the response to set of Scope objects.
//                if (responseCode == 200) {
//                    String[] scopeStrings = responseBody.split(" ");
//                    for (String scope : scopeStrings) {
//                        Log.i(TAG, "Server Scope: " + scope);
//                        serverScopeSet.add(new Scope(scope));
//                    }
//                } else {
//                    Log.e(TAG, "Error in getting server scopes: " + responseCode);
//                }
//
//            } catch (ClientProtocolException e) {
//                Log.e(TAG, "Error in getting server scopes.", e);
//            } catch (IOException e) {
//                Log.e(TAG, "Error in getting server scopes.", e);
//            }

            // This tells GoogleApiClient that the server needs a new serverAuthCode with
            // access to the scopes in serverScopeSet.  Note that we are not asking the server
            // if it already has such a token because this is a sample application.  In reality,
            // you should only do this on the first user sign-in or if the server loses or deletes
            // the refresh token.
            return CheckResult.newAuthRequiredResult(serverScopeSet);
        } else {
            // Server already has a valid refresh token with the correct scopes, no need to
            // ask the user for offline access again.
            return CheckResult.newAuthNotRequiredResult();
        }
    }

    @Override
    public boolean onUploadServerAuthCode(String idToken, String serverAuthCode) {
        Log.i(TAG, String.format("idToken=%s serverAuthCode=%s", idToken, serverAuthCode));

        long i = 0;
        i = System.currentTimeMillis();
        System.out.println("send over to server..." + i);
        // Upload the serverAuthCode to the server, which will attempt to exchange it for
        // a refresh token.  This callback occurs on a background thread, so it is OK
        // to perform synchronous network access.  Returning 'false' will fail the
        // GoogleApiClient.connect() call so if you would like the client to ignore
        // server failures, always return true.
//        HttpClient httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost(EXCHANGE_TOKEN_URL);
//
//        try {
//            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//            nameValuePairs.add(new BasicNameValuePair("idToken", idToken));
//            nameValuePairs.add(new BasicNameValuePair("serverAuthCode", serverAuthCode));
//            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//            HttpResponse response = httpClient.execute(httpPost);
//            int statusCode = response.getStatusLine().getStatusCode();
//            final String responseBody = EntityUtils.toString(response.getEntity());
//            Log.i(TAG, "Code: " + statusCode);
//            Log.i(TAG, "Resp: " + responseBody);
//
//            // [START_EXCLUDE]
//            // Show Toast on UI Thread
//            mActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mActivity, responseBody, Toast.LENGTH_LONG).show();
//                }
//            });
//            // [END_EXCLUDE]
//            return (statusCode == 200);
//        } catch (ClientProtocolException e) {
//            Log.e(TAG, "Error in auth code exchange.", e);
//            return false;
//        } catch (IOException e) {
//            Log.e(TAG, "Error in auth code exchange.", e);
//            return false;
//        }
//
        return true;
    }

    @Subscribe
    public void onMessageEvent(final Events.MessageEvent event) {
        if (event.getMetaData() != null && event.getMetaData().containsKey(AppConstants.K.MESSAGE.name()))
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(findViewById(R.id.login_layout), event.getMetaData().get(AppConstants.K.MESSAGE.name()), Snackbar.LENGTH_SHORT).show();
                }
            });
    }

    @Subscribe
    public void onAuthFailEvent(final Events.AuthFailEvent event) {
        if (event != null && event.getMetaData() != null && event.getMetaData().get(AppConstants.K.MESSAGE.name()) != null) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(findViewById(R.id.login_layout), event.getMetaData().get(AppConstants.K.MESSAGE.name()), Snackbar.LENGTH_SHORT).show();
                }
            });
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

        if (StringUtils.equals(provider, AppConstants.K.google.name())) {
            Utils.dismissSilently(progressDialog);
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
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
            body.put(AppConstants.K.provider.name(), AppConstants.K.google.name());
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
            Intent resultIntent = new Intent();
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
        private final WeakReference<LoginActivity> mActivity;

        public LocalHandler(LoginActivity parent) {
            mActivity = new WeakReference<LoginActivity>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // call for intent
            Intent signOutIntent =
                    new Intent(mActivity.get(), SignOutService.class);
            switch (msg.what) {
                case 0:
                    mActivity.get().progressDialog.setMessage("setting up your account...");
                    Bundle args = new Bundle();
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_CHOOSE_HANDLE.name());
                    try {
                        args.putString("fname", mActivity.get().gUserObject.get("fname"));
                        args.putString("lname", mActivity.get().gUserObject.get("lname"));
                        args.putString("image", mActivity.get().gUserObject.get("rphoto"));
                    } catch (Exception e) {
                        // muted
                    }
                    // intent
                    Intent intent = new Intent(mActivity.get().getApplicationContext(), ActivityFragment.class);
                    intent.putExtras(args);
                    mActivity.get().startActivityForResult(intent, Integer.parseInt(AppConstants.APP_INTENT.CHOOSE_PINCH_HANDLE.getValue()));
                    break;
                case 1:
                    Bundle iArgs = new Bundle();
                    if (mActivity.get().gUserObject != null) {
                        iArgs.putString(AppConstants.K.provider.name(), AppConstants.K.google.name());
                        try {
                            iArgs.putSerializable("user", (Serializable) mActivity.get().gUserObject);
                        } catch (Exception e) {
                            // muted
                        }
                    } else {
                        mActivity.get().progressDialog.setMessage("sign-in failed\ntry again");
                        Utils.dismissSilently(mActivity.get().progressDialog);
                        // call for intent
                        mActivity.get().startService(signOutIntent);
                        return;
                    }
                    // call for intent
                    Intent mServiceIntent =
                            new Intent(mActivity.get().getApplicationContext(), FBAuthService.class);
                    mServiceIntent.putExtras(iArgs);
                    mActivity.get().startService(mServiceIntent);
                    break;
                default:
                    mActivity.get().progressDialog.setMessage("sign-in failed\ntry again");
                    Utils.dismissSilently(mActivity.get().progressDialog);
                    mActivity.get().startService(signOutIntent);
                    break;
            }
        }
    }
}
