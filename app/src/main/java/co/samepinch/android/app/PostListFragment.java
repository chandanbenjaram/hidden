package co.samepinch.android.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;

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
import co.samepinch.android.app.helpers.misc.FragmentLifecycle;
import co.samepinch.android.app.helpers.misc.SimpleDividerItemDecoration;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaPosts;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_BY;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FRESH_DATA_FLAG;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POSTS_FAV;

public class PostListFragment extends Fragment implements FragmentLifecycle {
    public static final String TAG = "PostListFragment";
    public static final String ARG_PAGE = "ARG_PAGE";

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mRefreshLayout;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    PostCursorRecyclerViewAdapter mViewAdapter;
    LinearLayoutManager mLayoutManager;

    private boolean mLoadingMore;

    public static PostListFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PostListFragment fragment = new PostListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        // register to event bus
        BusProvider.INSTANCE.getBus().register(this);
        mLoadingMore = Boolean.FALSE;
        if (mRecyclerView != null) {
            Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
            Cursor oldCursor = mViewAdapter.swapCursor(cursor);
            if (oldCursor != null && !oldCursor.isClosed()) {
                oldCursor.close();
            }
            mRecyclerView.getAdapter().notifyItemRangeChanged(0, mRecyclerView.getAdapter().getItemCount());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.posts_wall, container, false);
        ButterKnife.bind(this, view);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager, 5) {
            @Override
            public void onLoadMore(RecyclerView rv, int current_page) {
                Log.d(TAG, "...onLoadMore..." + mLoadingMore);
                if (!mLoadingMore) {
                    mLoadingMore = Boolean.TRUE;
                    callForRemotePosts(Boolean.TRUE);
                }
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callForRemotePosts(false);
            }
        });
        setupRecyclerView();

        // refresh
        callForRemotePosts(false);

        return view;
    }

    private void setupRecyclerView() {
        Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mViewAdapter.setHasStableIds(Boolean.TRUE);
        mRecyclerView.setHasFixedSize(true);

        // STYLE :: ANIMATIONS
        ScaleInAnimationAdapter wrapperAdapter = new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(mViewAdapter));
        wrapperAdapter.setInterpolator(new AnticipateOvershootInterpolator());
        wrapperAdapter.setDuration(300);
        wrapperAdapter.setFirstOnly(Boolean.FALSE);
        mRecyclerView.setAdapter(wrapperAdapter);

        // STYLE :: DIVIDER
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
    }


    private void callForRemotePosts(boolean isPaginating) {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        if (isPaginating) {
            Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
            Map<String, String> pPosts = pref.getValueAsMap(AppConstants.API.PREF_POSTS_LIST.getValue());
            for (Map.Entry<String, String> e : pPosts.entrySet()) {
                iArgs.putString(e.getKey().toString(), e.getValue().toString());
            }
        } else {
            iArgs.putBoolean(KEY_FRESH_DATA_FLAG.getValue(), Boolean.TRUE);
        }


        // call for intent
        Intent mServiceIntent =
                new Intent(getActivity(), PostsPullService.class);
        mServiceIntent.putExtras(iArgs);
        getActivity().startService(mServiceIntent);
    }

    @Subscribe
    public void onPostsRefreshedEvent(final Events.PostsRefreshedEvent event) {
        if (event.getMetaData() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.setRefreshing(false);
                    }

                    Map<String, String> eMData = event.getMetaData();
                    if (eMData != null && StringUtils.equalsIgnoreCase(eMData.get(KEY_BY.getValue()), KEY_POSTS_FAV.getValue())) {
                        return;
                    }

                    Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
                    pref.setValue(AppConstants.API.PREF_POSTS_LIST.getValue(), event.getMetaData());
                    // refresh complete view
                    Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
                    Cursor oldCursor = mViewAdapter.swapCursor(cursor);
                    int oldEnd = oldCursor.getCount();
                    int newEnd = cursor.getCount();
                    if (oldCursor != null && !oldCursor.isClosed()) {
                        oldCursor.close();
                    }
                    if (mLoadingMore) {
                        // no need to refresh full recycler
                        mRecyclerView.getAdapter().notifyItemRangeInserted(oldEnd, newEnd);
                    } else {
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    //muted
                }

                mLoadingMore = false;
            }
        });
    }

    @Override
    public void onPauseFragment() {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onResumeFragment() {
    }

    @Override
    public void onRefreshFragment() {
    }
}
