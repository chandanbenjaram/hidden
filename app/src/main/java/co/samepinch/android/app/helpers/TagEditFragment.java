package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaPostDetails;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

public class TagEditFragment extends Fragment {
    public static final String TAG = "TagEditFragment";

    @Bind(R.id.bg_container)
    FrameLayout mBGContainer;

    @Bind(R.id.tag_edit_save)
    ImageButton mTagEditSaveBtn;

    @Bind(R.id.tag_edit_cancel)
    ImageButton mTagEditCancelBtn;

    //tag_subscription_switch
    @Bind(R.id.tag_subscription_switch)
    MaterialAnimatedSwitch aSubscriptionSwitch;

    ProgressDialog progressDialog;
    private LocalHandler mHandler;
    private boolean initSwitchAsChecked;

    private String mTagName;
    private String mTagUID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);

        // a handler
        mHandler = new LocalHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_edit, container, false);
        ButterKnife.bind(this, view);

        String tagId = getArguments().getString(AppConstants.APP_INTENT.KEY_TAG.getValue());
        Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{tagId}, null);
        if (!cursor.moveToFirst()) {
            getActivity().finish();
        }

        // currUserId
        final String currUserId = getArguments().getString(AppConstants.APP_INTENT.KEY_UID.getValue(), null);
        // checked state
        final String tagUserId = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_USER_ID));
        initSwitchAsChecked = StringUtils.equals(tagUserId, currUserId);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // init state
                if (initSwitchAsChecked) {
                    aSubscriptionSwitch.toggle();
                }
            }
        });

        // hold tag uid
        mTagUID = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_UID));

        // hold tag name
        mTagName = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_NAME));

        // fill image as background
        String imgUrl = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE));
        if (StringUtils.isNotBlank(imgUrl)) {
            // get window HW
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);

            // load background view
            SIMView tagImgView = new SIMView(getActivity().getApplicationContext());
            tagImgView.populateImageViewWithAdjustedAspect(imgUrl, new Integer[]{size.x, size.y});
            mBGContainer.addView(tagImgView);
        }

        return view;
    }


    @OnClick(R.id.tag_edit_save)
    public void onSaveEvent() {
        Utils.dismissSilently(progressDialog);

        // prevent further clicks
        mTagEditSaveBtn.setEnabled(false);
        mTagEditCancelBtn.setEnabled(false);

        String tagId = getArguments().getString(AppConstants.APP_INTENT.KEY_TAG.getValue());
        if (initSwitchAsChecked == aSubscriptionSwitch.isChecked()) {
            // nothing has change...
            getActivity().finish();
        }
        if (aSubscriptionSwitch.isChecked()) {
            // changed
            progressDialog.setMessage(StringUtils.join("subscribing to", mTagName, " "));
            progressDialog.show();
            new FollowTagTask().execute(mTagUID);
        } else {
            progressDialog.setMessage(StringUtils.join("subscribing to", mTagName, " "));
            progressDialog.show();
            new UnFollowTagTask().execute(mTagUID);
        }
    }


    private class FollowTagTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... tags) {
            if (tags == null || tags.length < 1 || StringUtils.isBlank(tags[0])) {
                return null;
            }

            ReqNoBody req = new ReqNoBody();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("follow");

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            try {
                String aUrl = StringUtils.join(new String[]{AppConstants.API.GROUPS.getValue(), tags[0]}, "/");
                HttpEntity<ReqNoBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(aUrl, HttpMethod.POST, payloadEntity, Resp.class);
                return resp.getBody().getStatus() == 200;
            } catch (Exception e) {
                // muted
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            Utils.dismissSilently(progressDialog);
            mTagEditSaveBtn.setEnabled(true);
            mTagEditCancelBtn.setEnabled(true);
            if (status) {
                Snackbar.make(getView(), "successful.", Snackbar.LENGTH_SHORT).show();
//                String postId = getArguments().getString(AppConstants.K.POST.name());
//                int count = getActivity().getContentResolver().delete(SchemaPostDetails.CONTENT_URI, SchemaPostDetails.COLUMN_UID + "=?", new String[]{postId});
//
//                Snackbar.make(getView(), "deleted successfully.", Snackbar.LENGTH_SHORT).show();
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("deleted", true);
//                getActivity().setResult(Activity.RESULT_OK, resultIntent);
//                getActivity().finish();
            } else {
                // prevent further clicks
                Snackbar.make(getView(), AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }


    private class UnFollowTagTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... tags) {
            if (tags == null || tags.length < 1 || StringUtils.isBlank(tags[0])) {
                return null;
            }

            ReqNoBody req = new ReqNoBody();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("unfollow");

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            try {
                String aUrl = StringUtils.join(new String[]{AppConstants.API.GROUPS.getValue(), tags[0]}, "/");
                HttpEntity<ReqNoBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(aUrl, HttpMethod.POST, payloadEntity, Resp.class);
                return resp.getBody().getStatus() == 200;
            } catch (Exception e) {
                // muted
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            Utils.dismissSilently(progressDialog);
            mTagEditSaveBtn.setEnabled(true);
            mTagEditCancelBtn.setEnabled(true);
            if (status) {
                Snackbar.make(getView(), "successful.", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(getView(), AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.tag_edit_cancel)
    public void onCancelEvent() {
        getActivity().finish();
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<TagEditFragment> mActivity;

        public LocalHandler(TagEditFragment parent) {
            mActivity = new WeakReference<TagEditFragment>(parent);
        }
    }
}