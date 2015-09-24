package co.samepinch.android.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;

import com.squareup.otto.Subscribe;

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

public class FavPostListFragment extends Fragment implements FragmentLifecycle {
    public static final String LOG_TAG = "FavPostListFragment";
    public static final String ARG_PAGE = "ARG_PAGE";

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mRefreshLayout;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    PostCursorRecyclerViewAdapter mViewAdapter;
    private LinearLayoutManager mLayoutManager;

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

//        // custom recycler
//        RecyclerView rv = new RecyclerView(getActivity().getApplicationContext()) {
//            @Override
//            public void scrollBy(int x, int y) {
//                try {
//                    super.scrollBy(x, y);
//                } catch (NullPointerException nlp) {
//                    // muted
//                }
//            }
//        };

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

        mRecyclerView.setLayoutManager(mLayoutManager);
        setupRecyclerView(mRecyclerView);

        return view;
    }

    private void setupRecyclerView(RecyclerView rv) {
        rv.setHasFixedSize(true);
        Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);

        // ANIMATIONS
        ScaleInAnimationAdapter wrapperAdapter = new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(mViewAdapter));
        wrapperAdapter.setInterpolator(new AnticipateOvershootInterpolator());
        wrapperAdapter.setDuration(300);
        wrapperAdapter.setFirstOnly(Boolean.FALSE);
        rv.setAdapter(wrapperAdapter);
    }

    private void callForRemotePosts() {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
        Map<String, String> pPosts = pref.getValueAsMap(AppConstants.API.PREF_POSTS_LIST.getValue());
        for (Map.Entry<String, String> e : pPosts.entrySet()) {
            iArgs.putString(e.getKey(), e.getValue());
        }

        // call for intent
        Intent mServiceIntent =
                new Intent(getActivity(), PostsPullService.class);
        mServiceIntent.putExtras(iArgs);
        getActivity().startService(mServiceIntent);
    }

    @Subscribe
    public void onPostsRefreshedEvent(final Events.PostsRefreshedEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
//                pref.setValue(AppConstants.API.PREF_POSTS_LIST.getValue(), event.getMetaData());
                mRefreshLayout.setRefreshing(false);
                try {
                    Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
                    mViewAdapter.changeCursor(cursor);
                } catch (Exception e) {
                    //e.printStackTrace();
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
