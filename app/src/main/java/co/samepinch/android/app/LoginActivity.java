package co.samepinch.android.app;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int SIGN_IN_REQUEST_CODE = 10;
    private static final int ERROR_DIALOG_REQUEST_CODE = 11;

    @Bind(R.id.sign_in_button)
    SignInButton signInButton;

    @Bind(R.id.sign_out_button)
    Button signOutButton;

    // For communicating with Google APIs
    private GoogleApiClient mGoogleApiClient;
    private boolean mSignInClicked;
    private boolean mIntentInProgress;
    // contains all possible error codes for when a client fails to connect to
    // Google Play services
    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        ButterKnife.bind(this);

        // Initializing google plus api client
        mGoogleApiClient = buildGoogleAPIClient();
    }

    /**
     * API to return GoogleApiClient Make sure to create new after revoking
     * access or for first time sign in
     *
     * @return
     */
    private GoogleApiClient buildGoogleAPIClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // make sure to initiate connection
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // disconnect api if it is connected
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }


    /**
     * API to update layout views based upon user signed in status
     *
     * @param isUserSignedIn
     */
    private void processUIUpdate(boolean isUserSignedIn) {
        if (isUserSignedIn) {
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
        }
    }

    /**
     * Handle results for your startActivityForResult() calls. Use requestCode
     * to differentiate.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }

    }



    @OnClick(R.id.sign_out_button)
    public void processSignOut() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            processUIUpdate(false);
        }

    }

    @OnClick(R.id.sign_in_button)
    public void processSignIn() {
        if (!mGoogleApiClient.isConnecting()) {
            processSignInError();
            mSignInClicked = true;
        }
    }

    /**
     * API to process sign in error Handle error based on ConnectionResult
     */
    private void processSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this,
                        SIGN_IN_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Callback for GoogleApiClient connection failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    ERROR_DIALOG_REQUEST_CODE).show();
            return;
        }
        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (mSignInClicked) {
                processSignInError();
            }
        }

    }

    /**
     * Callback for GoogleApiClient connection success
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        mSignInClicked = false;
        Toast.makeText(getApplicationContext(), "Signed In Successfully",
                Toast.LENGTH_LONG).show();

        processUserInfoAndUpdateUI();

        processUIUpdate(true);

    }

    /**
     * Callback for suspension of current connection
     */
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();

    }

    /**
     * API to update signed in user information
     */
    private void processUserInfoAndUpdateUI() {
        Person signedInUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (signedInUser != null) {
//
//            if (signedInUser.hasDisplayName()) {
//                String userName = signedInUser.getDisplayName();
//                this.userName.setText("Name: " + userName);
//            }
//
//            if (signedInUser.hasTagline()) {
//                String tagLine = signedInUser.getTagline();
//                this.userTagLine.setText("TagLine: " + tagLine);
//                this.userTagLine.setVisibility(View.VISIBLE);
//            }
//
//            if (signedInUser.hasAboutMe()) {
//                String aboutMe = signedInUser.getAboutMe();
//                this.userAboutMe.setText("About Me: " + aboutMe);
//                this.userAboutMe.setVisibility(View.VISIBLE);
//            }
//
//            if (signedInUser.hasBirthday()) {
//                String birthday = signedInUser.getBirthday();
//                this.userBirthday.setText("DOB " + birthday);
//                this.userBirthday.setVisibility(View.VISIBLE);
//            }
//
//            if (signedInUser.hasCurrentLocation()) {
//                String userLocation = signedInUser.getCurrentLocation();
//                this.userLocation.setText("Location: " + userLocation);
//                this.userLocation.setVisibility(View.VISIBLE);
//            }
//
//            String userEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
//            this.userEmail.setText("Email: " + userEmail);
//
//            if (signedInUser.hasImage()) {
//                String userProfilePicUrl = signedInUser.getImage().getUrl();
//                // default size is 50x50 in pixels.changes it to desired size
//                int profilePicRequestSize = 250;
//
//                userProfilePicUrl = userProfilePicUrl.substring(0,
//                        userProfilePicUrl.length() - 2) + profilePicRequestSize;
//                new UpdateProfilePicTask(userProfilePic)
//                        .execute(userProfilePicUrl);
//            }

        }
    }
}
