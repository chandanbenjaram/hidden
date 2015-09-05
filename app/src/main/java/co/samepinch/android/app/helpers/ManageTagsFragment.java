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
import android.view.animation.AnticipateOvershootInterpolator;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.PostDetailActivity;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.adapters.TagsRVAdapter;
import co.samepinch.android.app.helpers.adapters.TagsToManageRVAdapter;
import co.samepinch.android.app.helpers.intent.MultiMediaUploadService;
import co.samepinch.android.app.helpers.intent.PostDetailsService;
import co.samepinch.android.app.helpers.intent.TagsPullService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.rest.ReqGeneric;
import co.samepinch.android.rest.ReqPostCreate;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespPostDetails;
import co.samepinch.android.rest.RestClient;
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class ManageTagsFragment extends Fragment {
    public static final String TAG = "ManageTagsFragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.holder_recyclerview)
    FrameLayout frameLayout;

    ProgressDialog progressDialog;
    TagsToManageRVAdapter mTagsToManageRVAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes.
        setRetainInstance(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_tags, container, false);
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
        toolbar.setTitle("MANAGE TAGS");

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        ab.setHomeAsUpIndicator(R.drawable.back_2x);
        ab.setDisplayHomeAsUpEnabled(true);

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
        return view;
    }

    private void setupRecyclerView(final RecyclerView rv) {
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        rv.setLayoutManager(gridLayoutManager);
        rv.setHasFixedSize(true);

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

        Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
        String currUserId = userInfo.get(KEY_UID.getValue());

        Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_USER_ID + "=?", new String[]{currUserId}, null);
        TagsToManageRVAdapter.ItemEventListener itemEventListener = new TagsToManageRVAdapter.ItemEventListener() {
            @Override
            public void onClick(Object o) {
                Log.d(TAG, "clicked...");
            }
        };

        mTagsToManageRVAdapter = new TagsToManageRVAdapter(getActivity(), cursor, itemEventListener);

        // ANIMATIONS
        ScaleInAnimationAdapter wrapperAdapter = new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(mTagsToManageRVAdapter));
        wrapperAdapter.setInterpolator(new AnticipateOvershootInterpolator());
        wrapperAdapter.setDuration(300);
        wrapperAdapter.setFirstOnly(Boolean.FALSE);
        rv.setAdapter(wrapperAdapter);
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
                    mTagsToManageRVAdapter.changeCursor(cursor);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
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