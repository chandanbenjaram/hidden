package co.samepinch.android.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import com.squareup.otto.Subscribe;

import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.EndlessRecyclerOnScrollListener;
import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.app.helpers.intent.PostsPullService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaPosts;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.SlideInLeftAnimationAdapter;

public class PostListFragment extends Fragment {
    public static final String LOG_TAG = "PostListFragment";

    PostCursorRecyclerViewAdapter mViewAdapter;

    private LinearLayoutManager mLayoutManager;
    FragmentActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (FragmentActivity) activity;
        Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
        pref.remove(AppConstants.API.PREF_POSTS_LIST.getValue());
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
        mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());

        // custom recycler
        RecyclerView rv = new RecyclerView(activity.getApplicationContext()) {
            @Override
            public void scrollBy(int x, int y) {
                try {
                    super.scrollBy(x, y);
                } catch (NullPointerException nlp) {
                    // muted
                }
            }
        };

        rv.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager, 5) {
            @Override
            public void onLoadMore(RecyclerView rv, int current_page) {
            }
        });

        rv.setLayoutManager(mLayoutManager);
        setupRecyclerView(rv);

        callForRemotePosts();
        return rv;
    }

    private void setupRecyclerView(RecyclerView rv) {
        rv.setHasFixedSize(true);
        Cursor cursor = activity.getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
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
                new Intent(activity, PostsPullService.class);
        mServiceIntent.putExtras(iArgs);
        activity.startService(mServiceIntent);
    }

    @Subscribe
    public void onPostsRefreshedEvent(final Events.PostsRefreshedEvent event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
                pref.setValue(AppConstants.API.PREF_POSTS_LIST.getValue(), event.getMetaData());

                try {
                    Cursor cursor = activity.getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
                    mViewAdapter.changeCursor(cursor);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
    }
}
