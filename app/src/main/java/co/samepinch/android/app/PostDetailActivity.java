package co.samepinch.android.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.PostDetailsRVAdapter;
import co.samepinch.android.app.helpers.intent.PostDetailsService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaComments;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dao.SchemaPostDetails;
import co.samepinch.android.data.dto.PostDetails;
import co.samepinch.android.data.dto.User;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostDetailActivity extends AppCompatActivity {
    public static final String TAG = "PostDetailActivity";

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

    @Bind(R.id.floating_action_button)
    FloatingActionButton mFAB;

    @Bind(R.id.recyclerView)
    RecyclerView mRV;

    private String mPostId;
    private PostDetails mPostDetails;
    private PostDetailsRVAdapter mViewAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.KV.REQUEST_EDIT_POST.getIntValue()) {
                if (data != null && data.getBooleanExtra("deleted", false)) {
                    this.finish();
                    return;
                }

                Intent refresh = new Intent(this, PostDetailActivity.class);
                refresh.putExtras(getIntent());
                startActivity(refresh);
                this.finish();
            } else if (requestCode == AppConstants.KV.REQUEST_ADD_COMMENT.getIntValue()) {
                Intent intent = getIntent();
                intent.putExtra("isScrollDown", true);
                Intent refresh = new Intent(this, PostDetailActivity.class);
                refresh.putExtras(intent);
                startActivity(refresh);
                this.finish();
            } else if (requestCode == AppConstants.KV.REQUEST_EDIT_COMMENT.getIntValue()) {
                ((MergeCursor) mViewAdapter.getCursor()).requery();
                mViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postdetail);
        ButterKnife.bind(this);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        // get caller data
        Bundle iArgs = getIntent().getExtras();
        mPostId = iArgs.getString(AppConstants.K.POST.name());

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
        // query for post comments
        Cursor currComments = getContentResolver().query(SchemaComments.CONTENT_URI, null, SchemaComments.COLUMN_POST_DETAILS + "=?", new String[]{mPostId}, null);
        MergeCursor mergeCursor = new MergeCursor(new Cursor[]{currPost, currComments});

        // setup data
        setUpMetadata(currPost);

        // recycler view setup
        mRV.setHasFixedSize(true);
        mRV.setLayoutManager(mLayoutManager);

        mViewAdapter = new PostDetailsRVAdapter(this, mergeCursor);
        mRV.setAdapter(mViewAdapter);
        mRV.setItemAnimator(new DefaultItemAnimator());
//        if (iArgs.getBoolean("isScrollDown", false)) {
//            mRV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    mRV.smoothScrollToPosition(mergeCursor.getCount() - 1);
//                }
//            });
//        }


        // prepare to refresh post details
        Bundle iServiceArgs = new Bundle();
        iServiceArgs.putString(KEY_UID.getValue(), mPostId);

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TARGET
                Bundle args = new Bundle();
                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_COMMENT.name());
                // data
                args.putString(AppConstants.K.POST.name(), mPostId);

                // intent
                Intent intent = new Intent(getApplicationContext(), ActivityFragment.class);
                intent.putExtras(args);
                startActivityForResult(intent, AppConstants.KV.REQUEST_ADD_COMMENT.getIntValue());
            }
        });

        // call for intent
        Intent detailsIntent =
                new Intent(getApplicationContext(), PostDetailsService.class);
        detailsIntent.putExtras(iArgs);
        startService(detailsIntent);
    }

    private void setUpMetadata(Cursor currPost) {
        if (!currPost.moveToFirst()) {
            return;
        }

        mPostDetails = Utils.cursorToPostDetailsEntity(currPost);
        if (mPostDetails == null) {
            return;
        }

        mPostViewsCount.setText(String.valueOf(mPostDetails.getViews()));
        mPostVoteCount.setText(String.valueOf(mPostDetails.getUpvoteCount()));
        mPostDate.setText(Utils.dateToString(mPostDetails.getCreatedAt()));

        String ownerUid = null;
        int ownerUidIndex;
        if ((ownerUidIndex = currPost.getColumnIndex(SchemaPostDetails.COLUMN_OWNER)) != -1) {
            ownerUid = currPost.getString(ownerUidIndex);
        }
        // blank check
        if (StringUtils.isBlank(ownerUid)) {
            return;
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

            mPostDot.setOnClickListener(null);
            if (mPostDetails.getAnonymous() == null || !mPostDetails.getAnonymous())
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

        currDot.close();
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
        if (mPostDetails != null) {
            List<String> permissions = mPostDetails.getPermissions();
            if (permissions.contains("edit")) {
                // self
                menu.findItem(R.id.menuitem_post_edit_id).setVisible(Boolean.TRUE);
            } else {
                menu.findItem(R.id.menuitem_post_flag_id).setVisible(Boolean.TRUE);
            }
            if (mPostDetails.getUpvoted() == null || !mPostDetails.getUpvoted()) {
                menu.findItem(R.id.menuitem_post_like_id).setVisible(Boolean.TRUE);
            } else {
                menu.findItem(R.id.menuitem_post_dislike_id).setVisible(Boolean.TRUE);
            }
        }
        return true;
    }

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
        mBottomsheet.showWithSheetView(intentPickerSheet);
    }

    public void doFlagIt(MenuItem item) {
        System.out.println("doFlagIt..." + item);
    }

    public void doEditIt(MenuItem item) {
        // TARGET
        Bundle args = new Bundle();
        args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_EDIT_POST.name());
        // data
        args.putString(AppConstants.K.POST.name(), mPostId);

        // intent
        Intent intent = new Intent(getApplicationContext(), ActivityFragment.class);
        intent.putExtras(args);
        startActivityForResult(intent, AppConstants.KV.REQUEST_EDIT_POST.getIntValue());
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
                    ((MergeCursor) mViewAdapter.getCursor()).requery();
                    mViewAdapter.notifyDataSetChanged();
                    //                     invalidateOptionsMenu();
                } catch (Exception e) {
                    // muted
                }
            }
        });
    }

    @Subscribe
    public void onCommentDetailsRefreshEvent(Events.CommentDetailsRefreshEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ((MergeCursor) mViewAdapter.getCursor()).requery();
                    mViewAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    // muted
                }
            }
        });
    }

    @Subscribe
    public void onCommentDetailsEditEvent(final Events.CommentDetailsEditEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // TARGET
                    Bundle args = new Bundle();
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_COMMENT.name());
                    // data
                    args.putString(AppConstants.K.POST.name(), mPostId);
                    args.putString(AppConstants.K.COMMENT.name(), event.getMetaData().get(AppConstants.K.COMMENT.name()));


                    // intent
                    Intent intent = new Intent(getApplicationContext(), ActivityFragment.class);
                    intent.putExtras(args);
                    startActivityForResult(intent, AppConstants.KV.REQUEST_EDIT_COMMENT.getIntValue());
                } catch (Exception e) {
                    // muted
                }
            }
        });
    }
}