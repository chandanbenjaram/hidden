package co.samepinch.android.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    @Bind(R.id.input_name)
    EditText _nameText;

    @Bind(R.id.input_email)
    EditText _emailText;

    @Bind(R.id.input_password)
    EditText _passwordText;

    @Bind(R.id.btn_signup)
    Button _signupButton;

    @Bind(R.id.link_login)
    TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(SignupActivity.this);

//        _loginLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Finish the registration screen and return to the Login activity
//                finish();
//            }
//        });
    }

    @OnClick(R.id.link_login)
    public void signin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @OnClick(R.id.btn_signup)
    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("authenticating...");
        progressDialog.show();


        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own signup logic here.

//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onSignupSuccess or onSignupFailed
//                        // depending on success
//                        onSignupSuccess();
//                        // onSignupFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 3000);
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            _passwordText.setError("minimum 6 characters long");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}