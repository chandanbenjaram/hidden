package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.aviary.android.feather.headless.utils.MegaPixels;
import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.app.helpers.adapters.TagsRVAdapter;
import co.samepinch.android.app.helpers.intent.TagsPullService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dao.SchemaTags;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostCreateFragment extends Fragment {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_create, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });
        toolbar.setTitle(StringUtils.EMPTY);

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
                Log.d(TAG, "s..." + s);
            }
        };
        mTagsListAdapter = new TagsRVAdapter(getActivity(), cursor, listenr);
        rv.setAdapter(mTagsListAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    @Subscribe
    public void onTagsRefreshedEvent(Events.TagsRefreshedEvent event) {
        Log.d(TAG, "refreshed...");
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

                int lastIndex = mListViewAdapter.getCount() - 1;
                ImageOrTextViewAdapter.ImageOrText lastItem = mListViewAdapter.getItem(lastIndex);
                if (StringUtils.isBlank(lastItem.getText()) && lastItem.getImageUri() == null) {
                    mListViewAdapter.remove(lastItem);
                }

                mListViewAdapter.add(new ImageOrTextViewAdapter.ImageOrText(processedImageUri, null));
                mListViewAdapter.add(new ImageOrTextViewAdapter.ImageOrText(null, ""));

                Bundle extra = intent.getExtras();
                if (null != extra) {
                    // image has been changed by the user?
                    boolean changed = extra.getBoolean(Constants.EXTRA_OUT_BITMAP_CHANGED);
                }

                try {
                    InputStream imgStream = getActivity().getContentResolver().openInputStream(processedImageUri);
                    Drawable drawable = Drawable.createFromStream(imgStream, processedImageUri.toString());
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                    ImageSpan imgSpan = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BASELINE);

//                    SpannableStringBuilder bldr = new SpannableStringBuilder();
//                    bldr.append(mPostText.getText())

//                    int start = mPostText.getEditableText().length();
                    // S3 uploaded id
                    String imgPlaceholder = "\n" + "::dbd0aba09451143cf0e59055f7d44d99.jpg::" + "\n";
//                    mPostText.getEditableText().append(imgPlaceholder);
//                    int end = mPostText.getEditableText().length();

//                    mPostText.getEditableText().setSpan(imgSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                } catch (Exception e) {
                    Log.e(TAG, "error adding image.", e);
                }
            }
        }
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