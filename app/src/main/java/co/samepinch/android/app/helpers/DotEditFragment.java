package co.samepinch.android.app.helpers;

import android.app.Activity;
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
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aviary.android.feather.headless.utils.MegaPixels;
import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespUserDetails;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.USERS;

public class DotEditFragment extends Fragment {
    public static final String TAG = "DotEditFragment";
    private static Uri outputFileUri;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.view_avatar)
    SimpleDraweeView mAvatarView;

    @Bind(R.id.input_fname)
    EditText mFNameText;

    @Bind(R.id.input_lname)
    EditText mLNameText;

    @Bind(R.id.input_email)
    EditText mEmailText;

    @Bind(R.id.input_pinchHandle)
    EditText mPinchHandle;
//
//    @Bind(R.id.input_password)
//    EditText mPasswordText;

    @Bind(R.id.input_aboutMe)
    EditText mAboutMe;

    @Bind(R.id.input_blogUrl)
    EditText mBlogUrl;

    ProgressDialog progressDialog;
    private LocalHandler mHandler;

    User mUser;
    Map<String, String> mImageTaskMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);
        mHandler = new LocalHandler(this);

        mImageTaskMap = new HashMap<>();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.KV.REQUEST_CHOOSE_PICTURE.getIntValue()) {
                outputFileUri = (intent == null ? true : MediaStore.ACTION_IMAGE_CAPTURE.equals(intent.getAction())) ? outputFileUri : (intent == null ? null : intent.getData());
                Intent editorIntent = new Intent(getActivity(), FeatherActivity.class);
                editorIntent.setData(outputFileUri);

                editorIntent.putExtra(Constants.EXTRA_IN_API_KEY_SECRET, "df619be610e54ffc");
                editorIntent.putExtra(Constants.EXTRA_IN_HIRES_MEGAPIXELS, MegaPixels.Mp3.ordinal());
                editorIntent.putExtra(Constants.EXTRA_TOOLS_DISABLE_VIBRATION, "any");
                editorIntent.putExtra(Constants.EXTRA_OUTPUT_FORMAT, Bitmap.CompressFormat.JPEG.name());
                editorIntent.putExtra(Constants.EXTRA_OUTPUT_QUALITY, 55);
                startActivityForResult(editorIntent, AppConstants.KV.REQUEST_EDIT_PICTURE.getIntValue());
            } else if (requestCode == AppConstants.KV.REQUEST_EDIT_PICTURE.getIntValue()) {
                Uri processedImageUri = Uri.parse("file://" + intent.getData());

                Bundle extra = intent.getExtras();
                if (null != extra) {
                    // image has been changed by the user?
                    boolean changed = extra.getBoolean(Constants.EXTRA_OUT_BITMAP_CHANGED);
                }
                try {
                    InputStream localImageIS = getActivity().getContentResolver().openInputStream(Uri.parse(processedImageUri.toString()));
                    byte[] localImageBytes = Utils.getBytes(localImageIS);
                    String localImageEnc = Base64.encodeToString(localImageBytes, Base64.DEFAULT);

                    mImageTaskMap.clear();
                    mImageTaskMap.put(processedImageUri.toString(), null);
                    new ImageUploadTask().execute(new String[]{"droid.jpeg", localImageEnc, processedImageUri.toString()});

                    mAvatarView.setImageURI(processedImageUri);
                    mAvatarView.refreshDrawableState();
                } catch (Exception e) {
                    // muted
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!Utils.isLoggedIn()) {
            getActivity().finish();
        }

        View view = inflater.inflate(R.layout.dot_edit, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
            Gson gson = new Gson();
            String userInfoStr = gson.toJson(userInfo);
            mUser = gson.fromJson(userInfoStr, User.class);
        } catch (Exception e) {
            // muted
            getActivity().finish();
        }

        setupData(mUser);


        toolbar.setTitle(StringUtils.EMPTY);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                (getActivity()).onBackPressed();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dot_edit_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                (getActivity()).onBackPressed();
                return true;

            case R.id.menuitem_update:
                saveAction(299);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupData(User user) {
        if (StringUtils.isNotBlank(user.getPhoto())) {
            mAvatarView.setImageURI(Uri.parse(user.getPhoto()));
        }

        mFNameText.setText(user.getFname());
        mLNameText.setText(user.getLname());
        mEmailText.setText(user.getEmail());
        mPinchHandle.setText(user.getPinchHandle());
        mAboutMe.setText(user.getSummary());
        mBlogUrl.setText(user.getBlog());
    }

    private class UpdateDotTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... tags) {
            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (isRemoving()) {
                return;
            }
            Utils.dismissSilently(progressDialog);
        }
    }

    public void saveAction(final int delay) {
        if (!validate()) {
            return;
        }
        User user = new User();
        boolean hasChanges = false;
        String fName = mFNameText.getText().toString();
        if (!StringUtils.equals(fName, mUser.getFname())) {
            hasChanges = true;
            user.setFname(fName);
        }

        String lName = mLNameText.getText().toString();
        if (!StringUtils.equals(lName, mUser.getLname())) {
            hasChanges = true;
            user.setLname(lName);
        }

        String email = mEmailText.getText().toString();
        if (!StringUtils.equals(email, mUser.getEmail())) {
            hasChanges = true;
            user.setEmail(email);
        }

        String aboutMe = mAboutMe.getText().toString();
        if (!StringUtils.equals(aboutMe, mUser.getSummary())) {
            hasChanges = true;
            user.setSummary(aboutMe);
        }

        String blog = mBlogUrl.getText().toString();
        if (!StringUtils.equals(blog, mUser.getBlog())) {
            hasChanges = true;
            user.setBlog(blog);
        }

        if (!mImageTaskMap.isEmpty()) {
            hasChanges = true;
            String imgVal = null;
            for (Map.Entry<String, String> entry : mImageTaskMap.entrySet()) {
                imgVal = entry.getValue();
                break;
            }
            if (imgVal == null && delay > 1) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveAction((int) delay / 2);
                    }
                }, delay);
            } else {
                user.setImageKey(imgVal);
            }
        }

        // check if has changes
        if (!hasChanges) {
            Snackbar.make(getView(), "no changes detected...", Snackbar.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }

        progressDialog.setMessage("updating...");
        progressDialog.show();

        new DotUpdateTask().execute(user);
    }

    public boolean validate() {
        boolean valid = true;

        String fName = mFNameText.getText().toString();
        String lName = mLNameText.getText().toString();
        String email = mEmailText.getText().toString();
        String blogUrl = mBlogUrl.getText().toString();

        if (fName.isEmpty() || fName.length() < 1) {
            mFNameText.setError("at least 1 character");
            valid = false;
        } else {
            mFNameText.setError(null);
        }

        if (lName.isEmpty() || lName.length() < 1) {
            mLNameText.setError("at least 1 character");
            valid = false;
        } else {
            mLNameText.setError(null);
        }

        if (email.isEmpty() || !Utils.isValidEmail(email)) {
            mEmailText.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailText.setError(null);
        }


        if (StringUtils.isNotBlank(blogUrl) && !Utils.isValidUri(blogUrl)) {
            mBlogUrl.setError("must be a valid url");
            valid = false;
        } else {
            mBlogUrl.setError(null);
        }

        return valid;
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<DotEditFragment> mActivity;

        public LocalHandler(DotEditFragment parent) {
            mActivity = new WeakReference<DotEditFragment>(parent);
        }
    }

    private class DotUpdateTask extends AsyncTask<User, Integer, User> {
        @Override
        protected User doInBackground(User... users) {
            if (users == null || users.length < 1) {
                return null;
            }

            try {
                ReqSetBody req = new ReqSetBody();
                // set base args
                req.setToken(Utils.getNonBlankAppToken());
                req.setCmd("update");

                Map<String, String> args = new HashMap<>();
                Gson gson = new Gson();
                String userStr = gson.toJson(users[0]);

                Type mapType = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> body = gson.fromJson(userStr, mapType);

                // set body
                req.setBody(body);

                //headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);

                ResponseEntity<RespUserDetails> resp = RestClient.INSTANCE.handle().exchange(USERS.getValue(), HttpMethod.POST, payloadEntity, RespUserDetails.class);
                User updated;
                if (resp != null && resp.getBody() != null && (updated = resp.getBody().getBody()) != null) {
                    return updated;
                }
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                Log.e(TAG, resp == null ? "null" : resp.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            Utils.dismissSilently(progressDialog);
            try {
                if (user != null) {
                    Gson gson = new Gson();
                    String userStr = gson.toJson(user);
                    Type mapType = new TypeToken<Map<String, String>>() {
                    }.getType();
                    Map<String, String> userNew = gson.fromJson(userStr, mapType);

                    Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
                    userInfo.putAll(userNew);
                    Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_AUTH_USER.getValue(), userInfo);

                    String userInfoStr = gson.toJson(user);
                    mUser = gson.fromJson(userInfoStr, User.class);
                    setupData(mUser);
                    Snackbar.make(getView(), "updated successfully.", Snackbar.LENGTH_SHORT).show();
                    // finish
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    }, 99);

                    return;
                }
            } catch (Exception e) {
                // muted
            }
            Snackbar.make(getView(), AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue(), Snackbar.LENGTH_LONG).show();
        }
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
        final PackageManager packageManager = getActivity().getPackageManager();
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

    class ImageUploadTask extends AsyncTask<String, Integer, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bundle doInBackground(String... args) {
            Bundle respBundle = new Bundle();

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
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);
                if (resp.getBody() != null) {
                    Map<String, Object> respBody = resp.getBody().getBody();
                    if (respBody != null) {
                        String v = (String) respBody.get(AppConstants.APP_INTENT.KEY_KEY.getValue());
                        respBundle.putString(args[2], v);
                        return respBundle;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "err uploading...");

            }
            return respBundle;
        }

        @Override
        protected void onPostExecute(Bundle result) {
            if (result == null || mImageTaskMap.isEmpty()) {
                return;
            }
            for (String k : result.keySet()) {
                mImageTaskMap.put(k, result.getString(k));
            }
        }
    }
}