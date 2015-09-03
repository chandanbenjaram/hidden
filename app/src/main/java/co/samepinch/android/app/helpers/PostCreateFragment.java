package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.aviary.android.feather.headless.utils.MegaPixels;
import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
import com.bumptech.glide.util.Util;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.PostDetailActivity;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.app.helpers.adapters.TagsRVAdapter;
import co.samepinch.android.app.helpers.intent.FBAuthService;
import co.samepinch.android.app.helpers.intent.MultiMediaUploadService;
import co.samepinch.android.app.helpers.intent.PostDetailsService;
import co.samepinch.android.app.helpers.intent.PostsPullService;
import co.samepinch.android.app.helpers.intent.SignOutService;
import co.samepinch.android.app.helpers.intent.TagsPullService;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.rest.ReqGeneric;
import co.samepinch.android.rest.ReqPostCreate;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespPostDetails;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostCreateFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    public static final String TAG = "PostCreateFragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.list)
    ListView mListView;

    @Bind(R.id.holder_recyclerview)
    FrameLayout frameLayout;

    private static Uri outputFileUri;

    ProgressDialog progressDialog;

    ImageOrTextViewAdapter mListViewAdapter;
    TagsRVAdapter mTagsListAdapter;

    Map<String, String> imageStatusMap;

    String[] choosenTags;
    boolean mPostAsAnonymous;

    private LocalHandler mHandler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes.
        setRetainInstance(true);
        setHasOptionsMenu(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);

        // a handler
        mHandler = new LocalHandler(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.post_create_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_post_create:
                View menuItemView = getActivity().findViewById(R.id.menuitem_post_create);
                showMenu(menuItemView);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenu().add("as you");
        popup.getMenu().add("as anonymous");

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_create, container, false);
        ButterKnife.bind(this, view);
        imageStatusMap = new ConcurrentHashMap<>();

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });
        toolbar.setTitle("NEW");

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        ab.setHomeAsUpIndicator(R.drawable.back_2x);
        ab.setDisplayHomeAsUpEnabled(true);

        List<ImageOrTextViewAdapter.ImageOrText> listItems = new ArrayList<>();
        listItems.add(new ImageOrTextViewAdapter.ImageOrText(null, ""));

        mListViewAdapter
                = new ImageOrTextViewAdapter(getActivity(), R.layout.post_create_item, listItems);
        mListView.setAdapter(mListViewAdapter);
        // recycler view
        RecyclerView rv = new RecyclerView(getActivity().getApplicationContext()) {
            @Override
            public void scrollBy(int x, int y) {
                try {
                    super.scrollBy(x, y);
                } catch (NullPointerException nlp) {
                    // muted
                }
            }
        };

        frameLayout.addView(rv);
        setupRecyclerView(rv);

        // call for intent
        Intent tagRefreshIntent =
                new Intent(getActivity().getApplicationContext(), TagsPullService.class);
        getActivity().startService(tagRefreshIntent);

        return view;
    }

    private void setupRecyclerView(RecyclerView rv) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        rv.setLayoutManager(gridLayoutManager);
        rv.setHasFixedSize(true);

        Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
        String currUserId = userInfo.get(KEY_UID.getValue());

        Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_USER_ID + "=?", new String[]{currUserId}, null);
        TagsRVAdapter.ItemEventListener<String[]> listenr = new TagsRVAdapter.ItemEventListener<String[]>() {
            @Override
            public void onChange(String[] s) {
                choosenTags = s;
            }
        };
        mTagsListAdapter = new TagsRVAdapter(getActivity(), cursor, listenr, null);
        rv.setAdapter(mTagsListAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    @Subscribe
    public void onMultiMediaUploadEvent(final Events.MultiMediaUploadEvent event) {
        if (event.getMetaData() == null || !StringUtils.equals(TAG, event.getMetaData().get("callback"))) {
            return;
        }
        Map<String, String> eData = event.getMetaData();
        String name = eData.get("name");
        String key = eData.get(AppConstants.APP_INTENT.KEY_KEY.getValue());
        imageStatusMap.put(name, key);
    }

    @Subscribe
    public void onTagsRefreshedEvent(Events.TagsRefreshedEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
                    String currUserId = userInfo.get(KEY_UID.getValue());

                    Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_USER_ID + "=?", new String[]{currUserId}, null);
                    mTagsListAdapter.changeCursor(cursor);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        this.mPostAsAnonymous = !StringUtils.equals(item.toString(), "as you");
        progressDialog.setMessage("posting...");
        progressDialog.show();
        createPost();
        return true;
    }

    public void createPost() {
        final StringBuilder content = new StringBuilder();
        boolean hasPendingImageUploads = false;
        List<String> imgKeys = new ArrayList<>();

        for (final ImageOrTextViewAdapter.ImageOrText contentItem : mListViewAdapter.mItems) {
            if (contentItem.getText() != null) {
                content.append(contentItem.getText());
            }
            if (contentItem
                    .getImageUri() != null) {
                String imgId = contentItem.getImageUri().toString();
                if (imageStatusMap.get(imgId) == null) {
                    hasPendingImageUploads = true;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                uploadMultiMedia(contentItem.getImageUri());
                            } catch (Exception e) {
                                Log.e(TAG, "err uploading: " + contentItem.getImageUri(), e);
                            }
                        }
                    });
                } else {
                    imgKeys.add(imageStatusMap.get(imgId));
                    content.append("\n::" + imageStatusMap.get(imgId) + "::\n");
                }
            }
        }

        if (hasPendingImageUploads) {
            progressDialog.setMessage("uploading images...");
            // check again in future
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    createPost();
                }
            }, 999);
            return;
        } else {

            String errMsg = null;
            if (StringUtils.isBlank(content.toString())) {
                errMsg = "empty posts not allowed";
            } else if (choosenTags == null || choosenTags.length < 1) {
                errMsg = "please choose a tag";
            }

            if (errMsg != null) {
                Utils.dismissSilently(progressDialog);
                Snackbar.make(getView(), errMsg, Snackbar.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("posting...");
            ReqPostCreate.Body post = new ReqPostCreate.Body();
            post.setContent(content.toString());
            post.setTags(Arrays.asList(choosenTags));
            post.setAnonymous(mPostAsAnonymous ? "true" : "false");
            if (imgKeys.size() > 0) {
                post.setImgKeys(imgKeys);
            }

            new CreatePostTask().execute(post);
        }

    }

    private class CreatePostTask extends AsyncTask<ReqPostCreate.Body, Integer, RespPostDetails> {
        @Override
        protected RespPostDetails doInBackground(ReqPostCreate.Body... posts) {
            if (posts == null || posts.length < 1) {
                return null;
            }

            ReqGeneric<ReqPostCreate.Body> req = new ReqGeneric<>();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("create");
            // just consider single post for now
            req.setBody(posts[0]);

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            try {
                HttpEntity<ReqGeneric<ReqPostCreate.Body>> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<RespPostDetails> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, RespPostDetails.class);
                return resp.getBody();
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                Log.e(TAG, resp.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(RespPostDetails postDetails) {
            Utils.dismissSilently(progressDialog);
            if (postDetails == null || postDetails.getBody() == null) {
                Snackbar.make(getView(), "failed to post. try again...", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(getView(), "successfully posted.", Snackbar.LENGTH_SHORT).show();
                ArrayList<ContentProviderOperation> ops = PostDetailsService.parseResponse(postDetails);
                try {
                    ContentProviderResult[] result = getActivity().getContentResolver().
                            applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                } catch (Exception e) {
                    // muted
                }

                Bundle iArgs = new Bundle();
                iArgs.putString(AppConstants.K.POST.name(), postDetails.getBody().getUid());
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                intent.putExtras(iArgs);
                startActivity(intent);

                getActivity().finish();
            }
        }
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
                final Uri processedImageUri = Uri.parse("file://" + intent.getData());

                int lastIndex = mListViewAdapter.getCount() - 1;
                ImageOrTextViewAdapter.ImageOrText lastItem = mListViewAdapter.getItem(lastIndex);
                if (StringUtils.isBlank(lastItem.getText()) && lastItem.getImageUri() == null) {
                    mListViewAdapter.remove(lastItem);
                }

                mListViewAdapter.add(new ImageOrTextViewAdapter.ImageOrText(processedImageUri, null));
                mListViewAdapter.add(new ImageOrTextViewAdapter.ImageOrText(null, ""));
                mListView.requestFocus();
                mListView.setSelection(mListViewAdapter.getCount() - 1);

                Bundle extra = intent.getExtras();
                if (null != extra) {
                    // image has been changed by the user?
                    boolean changed = extra.getBoolean(Constants.EXTRA_OUT_BITMAP_CHANGED);
                }
                Runnable uploadTas = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            uploadMultiMedia(processedImageUri);
                        } catch (Exception e) {
                            // muted
                        }
                    }
                };

                uploadTas.run();
            }
        }
    }

    private void uploadMultiMedia(Uri mUri) throws IOException {
        InputStream imgStream = getActivity().getContentResolver().openInputStream(mUri);

        byte[] localImageBytes = Utils.getBytes(imgStream);
        String localImageEnc = Base64.encodeToString(localImageBytes, Base64.DEFAULT);

        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        iArgs.putString("name", mUri.toString());
        iArgs.putString("callback", TAG);
        iArgs.putString("content_type", "image/jpeg");
        iArgs.putString("content", localImageEnc);

        // call for intent
        Intent mServiceIntent =
                new Intent(getActivity(), MultiMediaUploadService.class);
        mServiceIntent.putExtras(iArgs);
        getActivity().startService(mServiceIntent);
    }

    @OnClick(R.id.fab)
    public void onClickFAB() {
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

    private static final class LocalHandler extends Handler {
        private final WeakReference<PostCreateFragment> mActivity;

        public LocalHandler(PostCreateFragment parent) {
            mActivity = new WeakReference<PostCreateFragment>(parent);
        }
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