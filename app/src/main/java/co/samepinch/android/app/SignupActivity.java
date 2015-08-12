package co.samepinch.android.app;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.IntentPickerSheetView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(SignupActivity.this);

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
//         body.put("fname", Utils.emptyIfNull(arg0.getString("first_name")));
//        body.put("lname", Utils.emptyIfNull(arg0.getString("last_name")));
//        body.put("email", Utils.emptyIfNull(arg0.getString("email")));
//        body.put("pinch_handle", StringUtils.deleteWhitespace(arg0.getString("name")));
//        body.put("rphoto", "http://harrogatearchsoc.org/wp-content/uploads/2013/12/Active-Imagination-.jpg");
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


        String fname = _fNameText.getText().toString();
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

        String name = _fNameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _fNameText.setError("at least 3 characters");
            valid = false;
        } else {
            _fNameText.setError(null);
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

    private static Uri outputFileUri;

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
