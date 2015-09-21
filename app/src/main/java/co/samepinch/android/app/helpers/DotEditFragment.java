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
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aviary.android.feather.headless.utils.MegaPixels;
import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestBase;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.USERS;

public class DotEditFragment extends Fragment {
    public static final String TAG = "DotEditFragment";
    private static Uri outputFileUri;

    @Bind(R.id.view_avatar)
    SimpleDraweeView mAvatarView;

    @Bind(R.id.input_fname)
    EditText mFNameText;

    @Bind(R.id.input_lname)
    EditText mLNameText;

    @Bind(R.id.input_pinchHandle)
    EditText mPinchHandle;

    @Bind(R.id.input_email)
    EditText mEmailText;

    @Bind(R.id.input_password)
    EditText mPasswordText;

    @Bind(R.id.input_aboutMe)
    EditText mAboutMe;

    @Bind(R.id.input_blogUrl)
    EditText mBlogUrl;

    ProgressDialog progressDialog;

    String uploadedImageKey;
    boolean uploadPending;

    User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);
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
                    InputStream localImageIS = getActivity().getContentResolver().openInputStream(Uri.parse(processedImageUri.toString()));
                    byte[] localImageBytes = Utils.getBytes(localImageIS);
                    String localImageEnc = Base64.encodeToString(localImageBytes, Base64.DEFAULT);

                    this.uploadPending = Boolean.TRUE;

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

        try {
            Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
            Gson gson = new Gson();
            String userInfoStr = gson.toJson(userInfo);
            mUser = gson.fromJson(userInfoStr, User.class);
        } catch (Exception e) {
            // muted
            getActivity().finish();
        }

        setUpData(mUser);

        return view;
    }

    private void setUpData(User user) {
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

    @OnClick(R.id.btn_update)
    public void onUpdateEvent() {
        mUser.setFname(mFNameText.getText().toString());
        new DotUpdateTask().execute(mUser);
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
                RestBase<User> req = new RestBase<User>() {
                };
                // set base args
                req.setToken(Utils.getNonBlankAppToken());
                req.setCmd("update");
                req.setBody(users[0]);

                //headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<RestBase<User>> payloadEntity = new HttpEntity<>(req, headers);

                ResponseEntity<String> resp = RestClient.INSTANCE.handle().exchange(USERS.getValue(), HttpMethod.POST, payloadEntity, String.class);
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
}