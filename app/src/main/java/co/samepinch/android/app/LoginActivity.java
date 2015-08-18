package co.samepinch.android.app;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
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

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ServerAuthCodeCallbacks {
    public static final String TAG = "LoginActivity";

    private static final int RC_SIGN_IN = 10;

    @Bind(R.id.btn_signin_google)
    SignInButton gSignInButton;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(LoginActivity.this);
        BusProvider.INSTANCE.getBus().register(this);

        gSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGoogleApiClient =
                new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Plus.API)
                        .addScope(new Scope(Scopes.PROFILE))
                        .addScope(new Scope(Scopes.PLUS_LOGIN))
                        .build();
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
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        } else if (requestCode == AppConstants.KV.REQUEST_SIGNUP.getIntValue()) {
            if (resultCode == RESULT_OK) {
                BusProvider.INSTANCE.getBus().post(new Events.AuthSuccessEvent(null));
                finish();
            }
        }
    }

    @Override
    public void onConnected(Bundle data) {
//        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
        Log.i(TAG, "data is null?" + (data == null));

        Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
//        Plus.PeopleApi.loadConnected(mGoogleApiClient);
//        if(person != null ){
//            Log.d(TAG, "logged-in successfully...");
//        }


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
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.reconnect();
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
        Log.i(TAG, "Checking if server is authorized.");

        HashSet<Scope> serverScopeSet = new HashSet<Scope>();


        // Check if the server has a token.  Since this callback executes in a background
        // thread it is OK to do synchronous network access in this check.
        //boolean serverHasToken = serverHasTokenFor(idToken);
        boolean serverHasToken = false;
        Log.i(TAG, "Server has token: " + String.valueOf(serverHasToken));

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
}
