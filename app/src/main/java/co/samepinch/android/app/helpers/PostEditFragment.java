package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.aviary.android.feather.headless.utils.MegaPixels;
import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.adapters.TagsRVAdapter;
import co.samepinch.android.app.helpers.intent.MultiMediaUploadService;
import co.samepinch.android.app.helpers.intent.PostDetailsService;
import co.samepinch.android.app.helpers.intent.TagsPullService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaPostDetails;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.data.dto.PostDetails;
import co.samepinch.android.rest.ReqGeneric;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.ReqPostCreate;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespPostDetails;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostEditFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    public static final String TAG = "PostEditFragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.list)
    ListView mListView;

    @Bind(R.id.holder_recyclerview)
    FrameLayout frameLayout;

    View mView;

    private static Uri outputFileUri;
    ImageOrTextViewAdapter mListViewAdapter;
    TagsRVAdapter mTagsListAdapter;
    Map<String, String> imageStatusMap;
    String[] choosenTags;
    boolean postAsAnonymous;

    ProgressDialog progressDialog;
    private LocalHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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
//                mListView.requestFocus();
                mListView.setSelection(mListViewAdapter.getCount() - 1);

                Bundle extra = intent.getExtras();
                if (null != extra) {
                    // image has been changed by the user?
                    boolean changed = extra.getBoolean(Constants.EXTRA_OUT_BITMAP_CHANGED);
                }
                Runnable uploadTask = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            uploadMultiMedia(processedImageUri);
                        } catch (Exception e) {
                            // muted
                        }
                    }
                };

                uploadTask.run();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.post_edit_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_post_edit:
                // setup PopUpMenu
                PopupMenu postAsPopUp = new PopupMenu(getActivity(), mView.findViewById(R.id.menuitem_post_edit));
                postAsPopUp.getMenu().add(R.string.post_as_you);
                postAsPopUp.getMenu().add(R.string.post_as_anonymous);
                postAsPopUp.setOnMenuItemClickListener(this);
                postAsPopUp.show();
                break;
            case R.id.menuitem_post_del:
                deletePrompt();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void deletePrompt() {
        AlertDialog.Builder delDialogBldr = new AlertDialog.Builder(
                getActivity());
        delDialogBldr.setTitle(R.string.post_del_pop_title);
        // set dialog message
        delDialogBldr
                .setMessage(R.string.post_del_pop_desc)
                .setCancelable(false)
                .setPositiveButton(R.string.post_del_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePost();
                    }
                })
                .setNegativeButton(R.string.post_del_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog delDialog = delDialogBldr.create();
        delDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.post_create, container, false);
        ButterKnife.bind(this, mView);
        imageStatusMap = new ConcurrentHashMap<>();

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                getActivity().onBackPressed();
            }
        });
        toolbar.setTitle("EDIT");

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.back_arrow);
        ab.setDisplayHomeAsUpEnabled(true);


        // get caller data        String dotUid = getArguments().getString(K.KEY_DOT.name());

        String postId = getArguments().getString(AppConstants.K.POST.name());
        // query for post details
        Cursor cursor = getActivity().getContentResolver().query(SchemaPostDetails.CONTENT_URI, null, SchemaPostDetails.COLUMN_UID + "=?", new String[]{postId}, null);
        PostDetails post = cursor.moveToFirst() ? Utils.cursorToPostDetailsEntity(cursor) : null;
        if (post == null) {
            Snackbar.make(mView, "problem opening post. try again...", Snackbar.LENGTH_SHORT).show();
            getActivity().finish();
        }

        Map<String, String> imageKV = post.getImages();
        for (Map.Entry<String, String> imageKVEntry : imageKV.entrySet()) {
            imageStatusMap.put(imageKVEntry.getValue(), imageKVEntry.getKey());
        }

        List<String> imageKArr = Utils.getImageValues(post.getContent());
        List<ImageOrTextViewAdapter.ImageOrText> listItems = new ArrayList<>();

        String rightContent = post.getContent();
        for (String imgK : imageKArr) {
            // get left of image
            String leftContent = StringUtils.substringBefore(rightContent, imgK).replaceAll("::", "");

            // grab right remaining chunk
            rightContent = StringUtils.substringAfter(rightContent, imgK).replaceAll("::", "");
            if (StringUtils.isNotBlank(leftContent)) {
                listItems.add(new ImageOrTextViewAdapter.ImageOrText(null, leftContent));
            }

            String imgUrl = imageKV.get(imgK);
            listItems.add(new ImageOrTextViewAdapter.ImageOrText(Uri.parse(imgUrl), null));
        }
        if (StringUtils.isNotBlank(rightContent)) {
            listItems.add(new ImageOrTextViewAdapter.ImageOrText(null, rightContent));
        }

        /// newline
        listItems.add(new ImageOrTextViewAdapter.ImageOrText(null, null));

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
        setupRecyclerView(rv, post.getTags());

        // call for intent
        Intent tagRefreshIntent =
                new Intent(getActivity().getApplicationContext(), TagsPullService.class);
        getActivity().startService(tagRefreshIntent);

        return mView;
    }

    private void setupRecyclerView(final RecyclerView rv, List<String> initTags) {
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        rv.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int viewWidth = rv.getMeasuredWidth();
                        float cardViewWidth = getActivity().getResources().getDimension(R.dimen.cardview_layout_width);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        gridLayoutManager.setSpanCount(newSpanCount);
                        gridLayoutManager.requestLayout();
                    }
                });
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

        choosenTags = initTags.toArray(new String[]{});
        mTagsListAdapter = new TagsRVAdapter(getActivity(), cursor, listenr, new HashSet<String>(initTags));
        rv.setAdapter(mTagsListAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        this.postAsAnonymous = item.getItemId() == R.string.post_as_anonymous;
        Utils.dismissSilently(progressDialog);
        progressDialog.setMessage("saving...");
        progressDialog.show();
        savePost();
        return true;
    }

    public void deletePost() {
        Utils.dismissSilently(progressDialog);
        progressDialog.setMessage("deleting post...");
        progressDialog.show();

        String postId = getArguments().getString(AppConstants.K.POST.name());
        new DelPostTask().execute(postId);
    }

    public void savePost() {
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
                    content.append("\n::");
                    content.append(imageStatusMap.get(imgId));
                    content.append("::\n");
                }
            }
        }

        if (hasPendingImageUploads) {
            progressDialog.setMessage("uploading images...");
            // check again in future
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    savePost();
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
                Snackbar.make(mView, errMsg, Snackbar.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("updating post...");
            ReqPostCreate.Body post = new ReqPostCreate.Body();
            post.setUid(getArguments().getString(AppConstants.K.POST.name()));
            post.setContent(content.toString());
            post.setTags(Arrays.asList(choosenTags));
            post.setAnonymous(postAsAnonymous ? "true" : "false");
            if (imgKeys.size() > 0) {
                post.setImgKeys(imgKeys);
            }

            new EditPostTask().execute(post);
        }
    }

    private class EditPostTask extends AsyncTask<ReqPostCreate.Body, Integer, RespPostDetails> {
        @Override
        protected RespPostDetails doInBackground(ReqPostCreate.Body... posts) {
            if (posts == null || posts.length < 1 || StringUtils.isBlank(posts[0].getUid())) {
                return null;
            }

            ReqGeneric<ReqPostCreate.Body> req = new ReqGeneric<>();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("update");
            // just consider single post for now
            req.setBody(posts[0]);

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(RestClient.INSTANCE.jsonMediaType());
            try {
                String updateUrl = StringUtils.join(new String[]{AppConstants.API.POSTS.getValue(), posts[0].getUid()}, "/");
                HttpEntity<ReqGeneric<ReqPostCreate.Body>> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<RespPostDetails> resp = RestClient.INSTANCE.handle().exchange(updateUrl, HttpMethod.POST, payloadEntity, RespPostDetails.class);
                return resp.getBody();
            } catch (Exception e) {
                // muted
            }
            return null;
        }

        @Override
        protected void onPostExecute(RespPostDetails postDetails) {
            Utils.dismissSilently(progressDialog);
            if (postDetails == null || postDetails.getBody() == null) {
                Snackbar.make(mView, "failed to update. try again...", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(mView, "updated successfully.", Snackbar.LENGTH_SHORT).show();
                Utils.PreferencesManager.getInstance().setValue(AppConstants.APP_INTENT.KEY_FRESH_WALL_FLAG.getValue(), Boolean.TRUE.toString());

                ArrayList<ContentProviderOperation> ops = PostDetailsService.parseResponse(postDetails);
                try {
                    ContentProviderResult[] result = getActivity().getContentResolver().
                            applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                } catch (Exception e) {
                    // muted
                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updated", true);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }
        }
    }


    private class DelPostTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... posts) {
            if (posts == null || posts.length < 1 || StringUtils.isBlank(posts[0])) {
                return null;
            }

            ReqNoBody req = new ReqNoBody();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("destroy");

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(RestClient.INSTANCE.jsonMediaType());
            try {
                String delUrl = StringUtils.join(new String[]{AppConstants.API.POSTS.getValue(), posts[0]}, "/");
                HttpEntity<ReqNoBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(delUrl, HttpMethod.POST, payloadEntity, Resp.class);
                return resp.getBody().getStatus() == 200;
            } catch (Exception e) {
                // muted
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            Utils.dismissSilently(progressDialog);
            if (status) {
                Snackbar.make(mView, "deleted successfully.", Snackbar.LENGTH_SHORT).show();
                Utils.PreferencesManager.getInstance().setValue(AppConstants.APP_INTENT.KEY_FRESH_WALL_FLAG.getValue(), Boolean.TRUE.toString());

                String postId = getArguments().getString(AppConstants.K.POST.name());
                // local db clean-up
                getActivity().getContentResolver().delete(SchemaPostDetails.CONTENT_URI, SchemaPostDetails.COLUMN_UID + "=?", new String[]{postId});
                getActivity().getContentResolver().delete(SchemaPosts.CONTENT_URI, SchemaPosts.COLUMN_UID + "=?", new String[]{postId});

                Intent resultIntent = new Intent();
                resultIntent.putExtra("deleted", true);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            } else {
                Snackbar.make(mView, "failed to delete. try again...", Snackbar.LENGTH_SHORT).show();
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
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
        private final WeakReference<PostEditFragment> mActivity;

        public LocalHandler(PostEditFragment parent) {
            mActivity = new WeakReference<PostEditFragment>(parent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.INSTANCE.getBus().register(this);
        setRetainInstance(true);
        Utils.showKeyboard(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
        Utils.hideKeyboard(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
}