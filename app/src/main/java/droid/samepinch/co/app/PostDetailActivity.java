package droid.samepinch.co.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.app.helpers.CommentsFragment;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.adapters.PostDetailsRVAdapter;
import droid.samepinch.co.app.helpers.intent.PostDetailsService;
import droid.samepinch.co.app.helpers.pubsubs.BusProvider;
import droid.samepinch.co.app.helpers.pubsubs.Events;
import droid.samepinch.co.data.dao.SchemaComments;
import droid.samepinch.co.data.dao.SchemaPostDetails;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dto.PostDetails;

import static droid.samepinch.co.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostDetailActivity extends AppCompatActivity implements CommentsFragment.CommentsFragmentCallbackListener {
    private static final Pattern IMG_PATTERN = Pattern.compile("::(.*?)(::)");
    public static final String LOG_TAG = "PostDetailActivity";

    private Intent mServiceIntent;
    private PostDetailsRVAdapter mViewAdapter;
    private LinearLayoutManager mLayoutManager;

    private String mPostId;


    @Bind(R.id.post_dot_with_handle)
    TextView mPostDotWithHandle;

    @Bind(R.id.post_vote_count)
    TextView mPostVoteCount;

    @Bind(R.id.post_views_count)
    TextView mPostViewsCount;

    @Bind(R.id.post_date)
    TextView mPostDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postdetail);
        ButterKnife.bind(this);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        // get caller data
        Bundle iArgs = getIntent().getExtras();
        mPostId = iArgs.getString(SchemaPosts.COLUMN_UID);

        // set title
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarLayout.setTitle(AppConstants.K.POST.name());

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        final ActionBar ab = getSupportActionBar();
        //  ab.setHomeAsUpIndicator(R.drawable.ic_menu); // set a custom icon for the default home button
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(false); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);


        // query for post details
        Cursor currPost = getContentResolver().query(SchemaPostDetails.CONTENT_URI, null, SchemaPostDetails.COLUMN_UID + "=?", new String[]{mPostId}, null);
        if (currPost.moveToFirst()) {
            PostDetails details = Utils.cursorToPostDetailsEntity(currPost);
            mPostDotWithHandle.setText(details.getOwner().getUid());
            mPostVoteCount.setText(String.valueOf(details.getUpvoteCount() == null ? 0 : details.getUpvoteCount()));
            mPostViewsCount.setText(String.valueOf(details.getViews() == null ? 0 : details.getViews()));
        }
        // query for post comments
        Cursor currComments = getContentResolver().query(SchemaComments.CONTENT_URI, null, SchemaComments.COLUMN_POST_DETAILS + "=?", new String[]{mPostId}, null);


        // recycler view setup
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLayoutManager);

        mViewAdapter = new PostDetailsRVAdapter(this, new MergeCursor(new Cursor[]{currPost, currComments}));
        rv.setAdapter(mViewAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());

        // prepare to refresh post details
        Bundle iServiceArgs = new Bundle();
        iServiceArgs.putString(KEY_UID.getValue(), mPostId);

        // call for intent
        mServiceIntent =
                new Intent(getApplicationContext(), PostDetailsService.class);
        mServiceIntent.putExtras(iArgs);
        startService(mServiceIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public void onCommentClick(int position) {
        Log.i(LOG_TAG, "position=" + position);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Subscribe
    public void onPostDetailsRefreshEvent(Events.PostDetailsRefreshEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // query for post details
                    Cursor currPost = getContentResolver().query(SchemaPostDetails.CONTENT_URI, null, SchemaPostDetails.COLUMN_UID + "=?", new String[]{mPostId}, null);
                    // query for post comments
                    Cursor currComments = getContentResolver().query(SchemaComments.CONTENT_URI, null, SchemaComments.COLUMN_POST_DETAILS + "=?", new String[]{mPostId}, null);
                    mViewAdapter.changeCursor(new MergeCursor(new Cursor[]{currPost, currComments}));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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