package co.samepinch.android.app;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, ResultCallback<People.LoadPeopleResult>, GoogleApiClient.OnConnectionFailedListener {
    public static final String LOG_TAG = "LoginActivity";

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

        gSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGoogleApiClient =
                new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Plus.API, Plus.PlusOptions.builder().build())
                        .addScope(Plus.SCOPE_PLUS_LOGIN)
                        .build();
    }


    @OnClick(R.id.btn_signin_google)
    public void onClickGoogleSignIn() {
        onSignInClicked();
    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(LOG_TAG, "Could not resolve ConnectionResult.", e);
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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
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
    public void onConnected(Bundle connectionHint) {
//        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
        Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if(person != null ){
            Log.d(LOG_TAG, "logged-in successfully");
        }


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
    public void onResult(People.LoadPeopleResult peopleData) {
        switch (peopleData.getStatus().getStatusCode()) {
            case CommonStatusCodes.SUCCESS:
                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                Log.d(LOG_TAG, "\n\n\n****** onResult person located " + person);
                break;

            case CommonStatusCodes.SIGN_IN_REQUIRED:
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
                break;

            default:
                Log.e(LOG_TAG, "err while logging-in G+ user: " + peopleData.getStatus());
                break;
        }
    }
}
