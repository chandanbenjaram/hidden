package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.intent.TagDetailsService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_NAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class TagEditFragment extends Fragment {
    public static final String TAG = "TagEditFragment";

    View mView;

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
        this.mView = inflater.inflate(R.layout.tag_edit, container, false);
        ButterKnife.bind(this, this.mView);

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
        if (StringUtils.isBlank(imgUrl)) {
        } else {
            // get window HW
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);

            // load background view
            SIMView tagImgView = new SIMView(getActivity().getApplicationContext());
            tagImgView.populateImageViewWithAdjustedAspect(imgUrl, new Integer[]{size.x, size.y});
            mBGContainer.addView(tagImgView);
        }

        callForRemoteTagData(mTagName);
        return this.mView;
    }

    private void callForRemoteTagData(String tag) {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_NAME.getValue(), tag);

        // call for intent
        Intent intent =
                new Intent(getActivity().getApplicationContext(), TagDetailsService.class);
        intent.putExtras(iArgs);
        getActivity().startService(intent);
    }

    @Subscribe
    public void onTagRefreshedEvent(Events.TagRefreshedEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update preferences metadata
                Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{mTagName}, null);
                if (!cursor.moveToFirst()) {
                    return;
                }
                String imgUrl = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE));
                if (StringUtils.isNotBlank(imgUrl)) {
                    // get window HW
                    Point size = new Point();
                    getActivity().getWindowManager().getDefaultDisplay().getSize(size);

                    // load background view
                    SIMView tagImgView = new SIMView(getActivity().getApplicationContext());
                    tagImgView.populateImageViewWithAdjustedAspect(imgUrl, new Integer[]{size.x, size.y});

                    mBGContainer.removeAllViewsInLayout();
                    mBGContainer.addView(tagImgView);
                }
            }
        });
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
        } else if (aSubscriptionSwitch.isChecked()) {
            // changed
            progressDialog.setMessage(StringUtils.join("subscribing to ", mTagName, " "));
            progressDialog.show();
            new FollowTagTask().execute(mTagUID);
        } else {
            progressDialog.setMessage(StringUtils.join("un-subscribing from ", mTagName, " "));
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
            if (isRemoving()) {
                return;
            }
            Utils.dismissSilently(progressDialog);
            mTagEditSaveBtn.setEnabled(true);
            mTagEditCancelBtn.setEnabled(true);
            if (status != null && status) {
                Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
                String currUserId = userInfo.get(KEY_UID.getValue());

                ContentValues values = new ContentValues();
                values.put(SchemaTags.COLUMN_USER_ID, currUserId);
                int result = getActivity().getContentResolver().update(SchemaTags.CONTENT_URI, values, SchemaTags.COLUMN_UID + "=?", new String[]{mTagUID});
                if (result > 0) {
                    Snackbar.make(mView, "successful.", Snackbar.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    getActivity().finish();
                } else {
                    Snackbar.make(mView, AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue(), Snackbar.LENGTH_SHORT).show();
                }
            } else {
                // prevent further clicks
                Snackbar.make(mView, AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private ArrayList<ContentProviderOperation> updateSubscription(boolean subscribe) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        if (subscribe) {
            Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
            String currUserId = userInfo.get(KEY_UID.getValue());

            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withValue(SchemaTags.COLUMN_UID, mTagUID)
                    .withValue(SchemaTags.COLUMN_USER_ID, currUserId)
                    .build());
        } else {
            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withSelection(SchemaTags.COLUMN_UID + "= ?", new String[]{mTagUID})
                    .withValue(SchemaTags.COLUMN_USER_ID, null)
                    .build());
        }

        return ops;
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
            if (isRemoving()) {
                return;
            }
            Utils.dismissSilently(progressDialog);
            mTagEditSaveBtn.setEnabled(true);
            mTagEditCancelBtn.setEnabled(true);
            if (status != null && status) {
                ContentValues values = new ContentValues();
                values.putNull(SchemaTags.COLUMN_USER_ID);
                int result = getActivity().getContentResolver().update(SchemaTags.CONTENT_URI, values, SchemaTags.COLUMN_UID + "=?", new String[]{mTagUID});
                if (result > 0) {
                    Snackbar.make(mView, "successful.", Snackbar.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    getActivity().finish();
                }
            } else {
                Snackbar.make(mView, AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue(), Snackbar.LENGTH_SHORT).show();
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


    @Override
    public void onResume() {
        super.onResume();
        // register to event bus
        BusProvider.INSTANCE.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
    }
}