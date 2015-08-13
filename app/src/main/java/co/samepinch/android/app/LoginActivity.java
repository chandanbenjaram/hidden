package co.samepinch.android.app;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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

    // For communicating with Google APIs
    private GoogleApiClient mGoogleApiClient;

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
//                        .addScope(Plus.SCOPE_PLUS_LOGIN)
                        .addScope(Plus.SCOPE_PLUS_PROFILE)
                        .build();
    }


    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    @OnClick(R.id.btn_signin_google)
    public void onClickGoogleSignIn(){
        onSignInClicked();
    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();

        // Show a message to the user that we are signing in.
//        mStatusTextView.setText(R.string.signing_in);
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
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
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
//                PersonBuffer personBuffer = peopleData.getPersonBuffer();
//                try {
//                    int count = personBuffer.getCount();
//                    for (int i = 0; i < count; i++) {
//                        mListItems.add(personBuffer.get(i).getDisplayName());
//                    }
//                } finally {
//                    personBuffer.close();
//                }
//
//                mListAdapter.notifyDataSetChanged();
                Person gPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                System.out.println("gPerson..." + gPerson);
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

    /**
     * Handle results for your startActivityForResult() calls. Use requestCode
     * to differentiate.
     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == SIGN_IN_REQUEST_CODE) {
//            if (resultCode != RESULT_OK) {
//                mSignInClicked = false;
//            }
//            mIntentInProgress = false;
//            if (!mGoogleApiClient.isConnecting()) {
//                mGoogleApiClient.connect();
//            }
//        } else if (requestCode == AppConstants.KV.REQUEST_SIGNUP.getIntValue()) {
//            if (resultCode == RESULT_OK) {
//                BusProvider.INSTANCE.getBus().post(new Events.AuthSuccessEvent(null));
//                finish();
//            }
//        }
//    }


//
//    //    @OnClick(R.id.sign_out_button)
//    public void processSignOut() {
//        if (mGoogleApiClient.isConnected()) {
//            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//            mGoogleApiClient.reconnect();
//            processUIUpdate(false);
//        }
//    }
//
//    @OnClick(R.id.btn_signin_google)
//    public void processSignIn() {
//        if (!mGoogleApiClient.isConnecting()) {
//            processSignInError();
//            mSignInClicked = true;
//        }
//    }

//    private void processSignInError() {
//        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
//            try {
//                mIntentInProgress = true;
//                mConnectionResult.startResolutionForResult(LoginActivity.this,
//                        SIGN_IN_REQUEST_CODE);
//            } catch (IntentSender.SendIntentException e) {
//                mIntentInProgress = false;
//                mGoogleApiClient.connect();
//            }
//        }
//    }

    /**
     * Callback for GoogleApiClient connection failure
     */
//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        if (!result.hasResolution()) {
//            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
//                    ERROR_DIALOG_REQUEST_CODE).show();
//            return;
//        }
//        if (!mIntentInProgress) {
//            mConnectionResult = result;
//            if (mSignInClicked) {
//                processSignInError();
//            }
//        }
//    }

    /**
     * Callback for GoogleApiClient connection success
     */
//    @Override
//    public void onConnected(Bundle connectionHint) {
//        mSignInClicked = false;
//
//        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
//    }
//
//    /**
//     * Callback for suspension of current connection
//     */
//    @Override
//    public void onConnectionSuspended(int cause) {
//        mGoogleApiClient.connect();
//    }
}