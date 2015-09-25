package co.samepinch.android.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.EndlessRecyclerOnScrollListener;
import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.app.helpers.intent.PostsPullService;
import co.samepinch.android.app.helpers.intent.TagDetailsService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dao.SchemaTags;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_BY;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_KEY;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_NAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POSTS_FAV;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POSTS_TAG;
import static co.samepinch.android.app.helpers.AppConstants.K;

public class TagWallFragment extends Fragment {
    public static final String TAG = "TagWallFragment";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mToolbarLayout;

    @Bind(R.id.backdrop)
    SimpleDraweeView mBackdrop;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mRefreshLayout;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private String mTag;
    private PostCursorRecyclerViewAdapter mViewAdapter;
    private LinearLayoutManager mLayoutManager;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.tags_wall_view, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                getActivity().onBackPressed();
            }
        });
        // tag name
        mTag = getArguments().getString(K.KEY_TAG.name());
        mToolbarLayout.setTitle(mTag);

        Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{mTag}, null);
        if (cursor.moveToFirst()) {
            int imgIdx = cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE);
            String imgStr = imgIdx > -1 ? cursor.getString(imgIdx) : null;
            if (StringUtils.isNotBlank(imgStr)) {
                Utils.setupLoadingImageHolder(mBackdrop, imgStr);
            }
        }
        cursor.close();

        // recyclers
        // custom recycler
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

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager, 5) {
            @Override
            public void onLoadMore(RecyclerView rv, int current_page) {
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callForRemotePosts(mTag);
            }
        });

        setUpPostsRecyclerView(new String[]{"%" + mTag + "%"});
        callForRemoteTagData(mTag);
        callForRemotePosts(mTag);
        return view;
    }


    private void setUpPostsRecyclerView(String[] tags) {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, "tags LIKE ?", tags, null);
        if (cursor.getCount() < 1) {
            callForRemotePosts(mTag);
        }
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);

        // ANIMATIONS
        ScaleInAnimationAdapter wrapperAdapter = new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(mViewAdapter));
        wrapperAdapter.setInterpolator(new AnticipateOvershootInterpolator());
        wrapperAdapter.setDuration(300);
        wrapperAdapter.setFirstOnly(Boolean.FALSE);
        mRecyclerView.setAdapter(wrapperAdapter);
    }

    private void callForRemoteTagData(String tag) {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_NAME.getValue(), tag);

        // call for intent
        Intent intent =
                new Intent(getActivity(), TagDetailsService.class);
        intent.putExtras(iArgs);
        getActivity().startService(intent);
    }

    private void callForRemotePosts(String tag) {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
//        Map<String, String> pPosts = pref.getValueAsMap(AppConstants.API.PREF_POSTS_LIST_FAV.getValue());
//        for (Map.Entry<String, String> e : pPosts.entrySet()) {
//            iArgs.putString(e.getKey(), e.getValue().toString());
//        }
        iArgs.putString(KEY_KEY.getValue(), StringUtils.removeStart(tag, "#"));
        iArgs.putString(KEY_BY.getValue(), KEY_POSTS_TAG.getValue());

        // call for intent
        Intent mServiceIntent =
                new Intent(getActivity(), PostsPullService.class);
        mServiceIntent.putExtras(iArgs);
        getActivity().startService(mServiceIntent);
    }


    @Subscribe
    public void onTagRefreshedEvent(Events.TagRefreshedEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update preferences metadata
                Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{mTag}, null);
                try {
                    if (cursor.moveToFirst()) {
                        int imgIdx = cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE);
                        String imgStr = imgIdx > -1 ? cursor.getString(imgIdx) : null;
                        if (StringUtils.isNotBlank(imgStr)) {
                            Utils.setupLoadingImageHolder(mBackdrop, imgStr);
                        }
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        });
    }

    @Subscribe
    public void onPostsRefreshedEvent(final Events.PostsRefreshedEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.setRefreshing(false);
                    }

                    setUpPostsRecyclerView(new String[]{"%" + mTag + "%"});
                } catch (Exception e) {
                    // muted
                }
            }
        });
    }

}