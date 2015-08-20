package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import co.samepinch.android.app.R;
import co.samepinch.android.app.SignupActivity;
import co.samepinch.android.app.helpers.intent.AuthService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_EMAIL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PASSWORD;

public class LoginEMailFragment extends android.support.v4.app.Fragment {
    public static final String LOG_TAG = "LoginEMailFragment";
    View mView;

    @Bind(R.id.input_email)
    EditText mEmailIdView;

    @Bind(R.id.input_password)
    EditText mPasswordView;

    @Bind(R.id.btn_login)
    Button mLoginButton;


    @Bind(R.id.btn_signup)
    Button mSignUpButton;

    @Bind(R.id.btn_forgot)
    Button mForgotButton;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    @OnClick(R.id.btn_login)
    public void onLogin() {
        //call to authenticate
        if (!validate()) {
            onLogInFail();
            return;
        }

        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("authenticating...");
        progressDialog.show();

        mLoginButton.setEnabled(Boolean.FALSE);
//        mSignUpButton.setEnabled(Boolean.FALSE);
//        mForgotButton.setEnabled(Boolean.FALSE);

        callForAuth();
    }

    @OnEditorAction(R.id.input_email)
    public boolean onEmailEditorAction() {
        onLogin();

        return true;
    }

    @OnClick(R.id.btn_signup)
    public void onSignup() {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_EMAIL.getValue(), mEmailIdView.getText().toString());

        Intent intent = new Intent(getActivity().getApplicationContext(), SignupActivity.class);
        intent.putExtras(iArgs);
        getActivity().startActivityForResult(intent, AppConstants.KV.REQUEST_SIGNUP.getIntValue());
    }

    private void callForAuth() {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
//        iArgs.putBoolean(KEY_CHECK_EMAIL_EXISTENCE.getValue(), Boolean.TRUE);
        iArgs.putString(KEY_EMAIL.getValue(), mEmailIdView.getText().toString());
        iArgs.putString(KEY_PASSWORD.getValue(), mPasswordView.getText().toString());

        // call for intent
        Intent mServiceIntent =
                new Intent(getActivity(), AuthService.class);
        mServiceIntent.putExtras(iArgs);
        getActivity().startService(mServiceIntent);
    }

    @Subscribe
    public void onAuthAccExistsEvent(final Events.AuthAccExistsEvent event) {
        Map<String, String> eventData = event.getMetaData();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Subscribe
    public void onAuthAccNotExistsEvent(final Events.AuthAccNotExistsEvent event) {
        Map<String, String> eventData = event.getMetaData();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_EMAIL.getValue(), mEmailIdView.getText().toString());

        Intent intent = new Intent(getActivity().getApplicationContext(), SignupActivity.class);
        intent.putExtras(iArgs);
        startActivityForResult(intent, AppConstants.KV.REQUEST_SIGNUP.getIntValue());
    }


    @Subscribe
    public void onAuthSuccessEvent(final Events.AuthSuccessEvent event) {
        Map<String, String> eventData = event.getMetaData();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.dismissSilently(progressDialog);

                getActivity().setResult(Activity.RESULT_OK, null);
//                getActivity().finish();
            }
        });
    }

    @Subscribe
    public void onAuthFailEvent(final Events.AuthFailEvent event) {
        final Map<String, String> eventData = event.getMetaData();
        if (eventData == null) {
            return;
        }

        if (StringUtils.equals(eventData.get(AppConstants.K.provider.name()), AppConstants.K.via_email_password.name())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.dismissSilently(progressDialog);
                    mLoginButton.setEnabled(Boolean.TRUE);

                    if (eventData.containsKey(AppConstants.K.MESSAGE.name())) {
                        Snackbar.make(mView, eventData.get(AppConstants.K.MESSAGE.name()), Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(mView, "try again", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void onLogInFail() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        mLoginButton.setEnabled(Boolean.TRUE);
    }

    public boolean validate() {
        boolean valid = true;

        String email = mEmailIdView.getText().toString();
        String password = "";

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailIdView.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailIdView.setError(null);
        }
//
//        if (password.isEmpty() || password.length() < 6) {
////            mPasswordView.setError("minimum 6 characters long");
//            valid = false;
//        } else {
////            mPasswordView.setError(null);
//        }

        return valid;
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
