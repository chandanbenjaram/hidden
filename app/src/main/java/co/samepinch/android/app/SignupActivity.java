package co.samepinch.android.app;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.intent.SignUpService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_EMAIL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_LNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PASSWORD;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PINCH_HANDLE;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private static Uri outputFileUri;

    @Bind(R.id.view_avatar)
    SimpleDraweeView _avatarView;

    @Bind(R.id.input_fname)
    EditText _fNameText;

    @Bind(R.id.input_lname)
    EditText _lNameText;

    @Bind(R.id.input_pinchHandle)
    EditText _pinchHandle;

    @Bind(R.id.input_email)
    EditText _emailText;

    @Bind(R.id.input_password)
    EditText _passwordText;

    @Bind(R.id.btn_signup)
    Button _signupButton;

    @Bind(R.id.link_login)
    TextView _loginLink;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.INSTANCE.getBus().register(this);

        setContentView(R.layout.activity_signup);
        ButterKnife.bind(SignupActivity.this);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Theme_AppCompat_Dialog);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("email")) {
                _emailText.setText(extras.getString("email"));
            }

            if (extras.containsKey("fname")) {
                _fNameText.setText(extras.getString("fname"));
            }

            if (extras.containsKey("lname")) {
                _lNameText.setText(extras.getString("lname"));
            }

            if (extras.containsKey("pinch_handle")) {
                _pinchHandle.setText(extras.getString("pinch_handle"));
            }
        }
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
        _signupButton.setEnabled(Boolean.FALSE);
        if (!validate()) {
            onSignUpFailEvent(null);
            return;
        }

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("creating your account...");
        progressDialog.show();

        String fName = _fNameText.getText().toString();
        String lName = _lNameText.getText().toString();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        String pinchHandle = _pinchHandle.getText().toString();

        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_FNAME.getValue(), fName);
        iArgs.putString(KEY_LNAME.getValue(), lName);
        iArgs.putString(KEY_EMAIL.getValue(), email);
        iArgs.putString(KEY_PASSWORD.getValue(), password);
        iArgs.putString(KEY_PINCH_HANDLE.getValue(), pinchHandle);

        // call for intent
        Intent mServiceIntent =
                new Intent(getApplicationContext(), SignUpService.class);
        mServiceIntent.putExtras(iArgs);
        startService(mServiceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @Subscribe
    public void onSignUpFailEvent(final Events.SignUpFailEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                _signupButton.setEnabled(Boolean.TRUE);
                Map<String, String> eventData = event == null ? null : event.getMetaData();
                if (eventData != null && eventData.containsKey(AppConstants.K.MESSAGE.name())) {
                    Snackbar.make(findViewById(R.id.bottomsheet), eventData.get(AppConstants.K.MESSAGE.name()), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.bottomsheet), "try again", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Subscribe
    public void onSignUpSuccessEvent(final Events.SignUpSuccessEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                setResult(RESULT_OK, null);
                finish();
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        String fName = _fNameText.getText().toString();
        String lName = _lNameText.getText().toString();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        String pinchHandle = _pinchHandle.getText().toString();


        if (fName.isEmpty() || fName.length() < 3) {
            _fNameText.setError("at least 3 characters");
            valid = false;
        } else {
            _fNameText.setError(null);
        }

        if (lName.isEmpty() || lName.length() < 3) {
            _lNameText.setError("at least 3 characters");
            valid = false;
        } else {
            _lNameText.setError(null);
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


        if (pinchHandle.isEmpty()) {
            _pinchHandle.setError("choose your pinch handle");
            valid = false;
        } else {
            _pinchHandle.setError(null);
        }

        return valid;
    }

    @OnClick(R.id.view_avatar)
    public void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "SamePinch" + File.separator);
        root.mkdirs();
        final String fname = Utils.getUniqueImageFilename();
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose Picture...");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, AppConstants.KV.REQUEST_CHOOSE_PICTURE.getIntValue());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.KV.REQUEST_CHOOSE_PICTURE.getIntValue()) {
                outputFileUri = (intent == null ? true : MediaStore.ACTION_IMAGE_CAPTURE.equals(intent.getAction())) ? outputFileUri : (intent == null ? null : intent.getData());
                _avatarView.setImageURI(outputFileUri);
                _avatarView.refreshDrawableState();
            }
        }
    }
}
