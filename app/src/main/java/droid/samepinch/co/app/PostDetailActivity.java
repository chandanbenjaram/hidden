package droid.samepinch.co.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.IntentPickerSheetView;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.adapters.PostDetailsRVAdapter;
import droid.samepinch.co.app.helpers.intent.PostDetailsService;
import droid.samepinch.co.app.helpers.pubsubs.BusProvider;
import droid.samepinch.co.app.helpers.pubsubs.Events;
import droid.samepinch.co.data.dao.SchemaComments;
import droid.samepinch.co.data.dao.SchemaDots;
import droid.samepinch.co.data.dao.SchemaPostDetails;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dto.User;

import static droid.samepinch.co.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostDetailActivity extends AppCompatActivity {
    private static final Pattern IMG_PATTERN = Pattern.compile("::(.*?)(::)");
    public static final String LOG_TAG = "PostDetailActivity";

    private Intent mServiceIntent;
    private PostDetailsRVAdapter mViewAdapter;
    private LinearLayoutManager mLayoutManager;
    private String mPostId;

    private ShareActionProvider mShareActionProvider;

    @Bind(R.id.bottomsheet)
    BottomSheetLayout mBottomsheet;

    @Bind(R.id.post_dot)
    ViewGroup mPostDot;

    @Bind(R.id.post_dot_name)
    TextView mPostDotName;

    @Bind(R.id.post_dot_handle)
    TextView mPostDotHandle;

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
        setUpMetadata(currPost);

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

    private void setUpMetadata(Cursor currPost) {
        if (!currPost.moveToFirst()) {
            return;
        }


        int viewsIndex;
        if ((viewsIndex = currPost.getColumnIndex(SchemaPostDetails.COLUMN_VIEWS)) != -1) {
            mPostViewsCount.setText(Integer.toString(currPost.getInt(viewsIndex)));
        }

        int upvoteCountIndex;
        if ((upvoteCountIndex = currPost.getColumnIndex(SchemaPostDetails.COLUMN_UPVOTE_COUNT)) != -1) {
            mPostVoteCount.setText(Integer.toString(currPost.getInt(upvoteCountIndex)));
        }

        int createdAtIndex;
        if ((createdAtIndex = currPost.getColumnIndex(SchemaPostDetails.COLUMN_CREATED_AT)) != -1) {
            mPostDate.setText(Utils.dateToString(currPost.getLong(createdAtIndex)));
        }

        String ownerUid = null;
        int ownerUidIndex;
        if ((ownerUidIndex = currPost.getColumnIndex(SchemaPostDetails.COLUMN_OWNER)) != -1) {
            ownerUid = currPost.getString(ownerUidIndex);
        }
        // get user info
        Cursor currDot = getContentResolver().query(SchemaDots.CONTENT_URI, null, SchemaDots.COLUMN_UID + "=?", new String[]{ownerUid}, null);
        if (currDot.moveToFirst()) {
            final User user = Utils.cursorToUserEntity(currDot);

            String dotName = null;
            if (StringUtils.isBlank(user.getPrefName())) {
                String fName = StringUtils.defaultString(user.getFname(), "");
                String lName = StringUtils.defaultString(user.getLname(), "");
                dotName = fName + " " + lName;
            } else {
                dotName = user.getPrefName();
            }
            mPostDotName.setText(dotName);
            String pinchHandle = String.format(getApplicationContext().getString(R.string.pinch_handle), user.getPinchHandle());
            mPostDotHandle.setText(pinchHandle);

            // onclick take to dot view
            mPostDot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TARGET
                    Bundle args = new Bundle();
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_DOTWALL.name());
                    // data
                    args.putString(AppConstants.K.KEY_DOT.name(), user.getUid());

                    // intent
                    Intent intent = new Intent(getApplicationContext(), ActivityFragment.class);
                    intent.putExtras(args);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menuitem_post_edit_id:
                doEditIt(item);
                return true;

            case R.id.menuitem_post_flag_id:
                doFlagIt(item);
                return true;

            case R.id.menuitem_post_share_id:
                doShareIt(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_detail_menu, menu);
//        MenuItem menuItemShare = menu.findItem(R.id.menuitem_post_share_id);
//        // Set history different from the default before getting the action
//        // view since a call to MenuItemCompat.getActionView() calls
//        // ActionProvider.onCreateActionView() which uses the backing file name. Omit this
//        // line if using the default share history file is desired.
//        mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItemShare);
//        doShareIt();
        return true;
    }

//    public void doShareIt() {
//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, mPostId);
//        sendIntent.setType("text/plain");
//
//        // When you want to share set the share intent.
//        mShareActionProvider.setShareIntent(sendIntent);
//    }

    public void doShareIt(MenuItem item) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mPostId);
        shareIntent.setType("text/plain");

        IntentPickerSheetView intentPickerSheet = new IntentPickerSheetView(PostDetailActivity.this, shareIntent, "Share with...", new IntentPickerSheetView.OnIntentPickedListener() {
            @Override
            public void onIntentPicked(Intent intent) {
                mBottomsheet.dismissSheet();
                startActivity(intent);
            }
        });
//        intentPickerSheet.setFilter(new IntentPickerSheetView.Filter() {
//            @Override
//            public boolean include(IntentPickerSheetView.ActivityInfo info) {
//                return !info.componentName.getPackageName().startsWith("com.android");
//            }
//        });
//// Sort activities in reverse order for no good reason
//        intentPickerSheet.setSortMethod(new Comparator<IntentPickerSheetView.ActivityInfo>() {
//            @Override
//            public int compare(IntentPickerSheetView.ActivityInfo lhs, IntentPickerSheetView.ActivityInfo rhs) {
//                return rhs.label.compareTo(lhs.label);
//            }
//        });
        mBottomsheet.showWithSheetView(intentPickerSheet);
    }

    public void doFlagIt(MenuItem item) {
        System.out.println("doFlagIt..." + item);

    }

    public void doEditIt(MenuItem item) {
        System.out.println("doEditIt..." + item);
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
}