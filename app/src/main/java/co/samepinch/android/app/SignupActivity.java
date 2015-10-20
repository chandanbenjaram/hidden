package co.samepinch.android.app;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aviary.android.feather.headless.utils.MegaPixels;
import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.intent.ParseSyncService;
import co.samepinch.android.app.helpers.intent.SignUpService;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_APP_ACCESS_STATE;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_EMAIL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_KEY;
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

    String uploadedImageKey;
    boolean uploadPending;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.INSTANCE.getBus().register(this);

        setContentView(R.layout.activity_signup);
        ButterKnife.bind(SignupActivity.this);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);
        progressDialog.setIndeterminate(true);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.KV.REQUEST_CHOOSE_PICTURE.getIntValue()) {
                outputFileUri = (intent == null ? true : MediaStore.ACTION_IMAGE_CAPTURE.equals(intent.getAction())) ? outputFileUri : (intent == null ? null : intent.getData());
                Intent editorIntent = new Intent(this, FeatherActivity.class);
                editorIntent.setData(outputFileUri);

                editorIntent.putExtra(Constants.EXTRA_IN_API_KEY_SECRET, "df619be610e54ffc");
                editorIntent.putExtra(Constants.EXTRA_IN_HIRES_MEGAPIXELS, MegaPixels.Mp3.ordinal());
                editorIntent.putExtra(Constants.EXTRA_TOOLS_DISABLE_VIBRATION, "any");
                editorIntent.putExtra(Constants.EXTRA_OUTPUT_FORMAT, Bitmap.CompressFormat.JPEG.name());
                editorIntent.putExtra(Constants.EXTRA_OUTPUT_QUALITY, 55);
//                editorIntent.putExtra( Constants.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(editorIntent, AppConstants.KV.REQUEST_EDIT_PICTURE.getIntValue());
            } else if (requestCode == AppConstants.KV.REQUEST_EDIT_PICTURE.getIntValue()) {
                Uri processedImageUri = Uri.parse("file://" + intent.getData());

                Bundle extra = intent.getExtras();
                if (null != extra) {
                    // image has been changed by the user?
                    boolean changed = extra.getBoolean(Constants.EXTRA_OUT_BITMAP_CHANGED);
                }

                try {
                    InputStream localImageIS = getContentResolver().openInputStream(Uri.parse(processedImageUri.toString()));
                    byte[] localImageBytes = Utils.getBytes(localImageIS);
                    String localImageEnc = Base64.encodeToString(localImageBytes, Base64.DEFAULT);
                    new ImageUploadTask().execute("droid.jpeg", localImageEnc);

                    this.uploadPending = Boolean.TRUE;

                    _avatarView.setImageURI(processedImageUri);
                    _avatarView.refreshDrawableState();
                } catch (Exception e) {
                    // muted
                }
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
    public void onClickSignUp() {
        Utils.dismissSilently(progressDialog);

        if (uploadPending) {
            _signupButton.setEnabled(Boolean.FALSE);
            if (!validate()) {
                onSignUpFailEvent(null);
                return;
            }

            progressDialog.setMessage("uploading image...");
            progressDialog.show();
        } else {
            signup();
        }
    }

    public void signup() {
        Log.d(TAG, "Signup");
        _signupButton.setEnabled(Boolean.FALSE);
        if (!validate()) {
            onSignUpFailEvent(null);
            return;
        }

        progressDialog.setMessage("creating your account...");
        progressDialog.show();

        String fName = StringUtils.defaultString(_fNameText.getText().toString(), StringUtils.EMPTY);
        String lName = StringUtils.defaultString(_lNameText.getText().toString(), StringUtils.EMPTY);
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String pinchHandle = _pinchHandle.getText().toString();

        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_FNAME.getValue(), fName);
        iArgs.putString(KEY_LNAME.getValue(), lName);
        iArgs.putString(KEY_EMAIL.getValue(), email);
        iArgs.putString(KEY_PASSWORD.getValue(), password);
        iArgs.putString(KEY_PINCH_HANDLE.getValue(), pinchHandle);

        if (StringUtils.isNotBlank(uploadedImageKey)) {
            iArgs.putString(KEY_KEY.getValue(), uploadedImageKey);
        }

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
        // call for intent
        Intent intent =
                new Intent(getApplicationContext(), ParseSyncService.class);
        Bundle iArgs = new Bundle();
        iArgs.putInt(KEY_APP_ACCESS_STATE.getValue(), 1);
        intent.putExtras(iArgs);
        startService(intent);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                setResult(RESULT_OK);
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

//
//        if (fName.isEmpty() || fName.length() < 1) {
//            _fNameText.setError("at least 1 character");
//            valid = false;
//        } else {
//            _fNameText.setError(null);
//        }
//
//        if (lName.isEmpty() || lName.length() < 1) {
//            _lNameText.setError("at least 1 character");
//            valid = false;
//        } else {
//            _lNameText.setError(null);
//        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            _passwordText.setError("minimum 6 characters required");
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

    @Subscribe
    public void onSignUpImageUploadEvent(final Events.SignUpImageUploadEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadPending = Boolean.FALSE;
                if (event.getMetaData() != null) {
                    uploadedImageKey = event.getMetaData().get(KEY_KEY.getValue());
                }
                if (progressDialog.isShowing()) {
                    signup();
                }
            }
        });
    }

    static class ImageUploadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                StorageComponent component = DaggerStorageComponent.create();
                ReqSetBody req = component.provideReqSetBody();
                // set base args
                req.setToken(Utils.getNonBlankAppToken());
                req.setCmd("s3upload");

                Map<String, String> body = new HashMap<>();
                body.put("name", args[0]);
                body.put("content", args[1]);
                body.put("content_type", "image/jpeg");

                req.setBody(body);

                //headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(RestClient.INSTANCE.jsonMediaType());

                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);
                if (resp.getBody() != null) {
                    Map<String, Object> respBody = resp.getBody().getBody();
                    return respBody == null ? null : (String) respBody.get(AppConstants.APP_INTENT.KEY_KEY.getValue());
                }
            } catch (Exception e) {
                Log.e(TAG, "err uploading...");

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Map<String, String> eventData = new HashMap<>();
            eventData.put(AppConstants.APP_INTENT.KEY_KEY.getValue(), result);

            BusProvider.INSTANCE.getBus().post(new Events.SignUpImageUploadEvent(eventData));
        }
    }
}
