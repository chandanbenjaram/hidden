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
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaPosts;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_BY;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POSTS_FAV;

public class FavPostListFragment extends Fragment implements FragmentLifecycle {
    public static final String LOG_TAG = "FavPostListFragment";
    public static final String ARG_PAGE = "ARG_PAGE";

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mRefreshLayout;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    PostCursorRecyclerViewAdapter mViewAdapter;
    LinearLayoutManager mLayoutManager;

    public static FavPostListFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        FavPostListFragment fragment = new FavPostListFragment();
        fragment.setArguments(args);
        return fragment;
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
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callForRemotePosts();
            }
        });
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
        if(cursor.getCount() < 1){
            callForRemotePosts();
        }
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);

        // ANIMATIONS
        ScaleInAnimationAdapter wrapperAdapter = new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(mViewAdapter));
        wrapperAdapter.setInterpolator(new AnticipateOvershootInterpolator());
        wrapperAdapter.setDuration(300);
        wrapperAdapter.setFirstOnly(Boolean.FALSE);
        mRecyclerView.setAdapter(wrapperAdapter);
    }

    private void callForRemotePosts() {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
        Map<String, String> pPosts = pref.getValueAsMap(AppConstants.API.PREF_POSTS_LIST_FAV.getValue());
        for (Map.Entry<String, String> e : pPosts.entrySet()) {
            iArgs.putString(e.getKey(), e.getValue().toString());
        }
        iArgs.putString(KEY_BY.getValue(), KEY_POSTS_FAV.getValue());

        // call for intent
        Intent mServiceIntent =
                new Intent(getActivity(), PostsPullService.class);
        mServiceIntent.putExtras(iArgs);
        getActivity().startService(mServiceIntent);
    }

    @Subscribe
    public void onPostsRefreshedEvent(final Events.PostsRefreshedEvent event) {
        if(mRefreshLayout.isRefreshing()){
            mRefreshLayout.setRefreshing(false);
        }

        Map<String, String> eMData = event.getMetaData();
        if ((eMData = event.getMetaData()) == null || !StringUtils.equalsIgnoreCase(eMData.get(KEY_BY.getValue()), KEY_POSTS_FAV.getValue())) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
                    pref.setValue(AppConstants.API.PREF_POSTS_LIST_FAV.getValue(), event.getMetaData());

                   setupRecyclerView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPauseFragment() {
//        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @Override
    public void onResumeFragment() {
//        BusProvider.INSTANCE.getBus().register(this);
    }
}
