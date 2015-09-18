package co.samepinch.android.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.Postprocessor;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.app.helpers.intent.DotDetailsService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dto.User;

import static co.samepinch.android.app.helpers.AppConstants.K;

public class DotWallFragment extends Fragment {
    public static final String LOG_TAG = "DotWallFragment";

    PostCursorRecyclerViewAdapter mViewAdapter;
    LinearLayoutManager mLayoutManager;

    @Bind(R.id.dot_wall_image)
    SIMView mDotImage;

    @Bind(R.id.dot_wall_image_txt)
    TextView mDotImageText;

    @Bind(R.id.dot_wall_name)
    TextView mDotName;

    @Bind(R.id.dot_wall_handle)
    TextView mDotHandle;

    @Bind(R.id.dot_wall_about)
    TextView mDotAbout;

    @Bind(R.id.dot_wall_followers_count)
    TextView mDotFollowersCnt;

    @Bind(R.id.dot_wall_posts_count)
    TextView mDotPostsCnt;

    @Bind(R.id.dot_wall_blog)
    ImageView mDotBlog;

    @Bind(R.id.dot_wall_backdrop)
    ImageView mDotBackdrop;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Bind(R.id.dot_wall_switch)
    ViewSwitcher mVS;

    @Bind(R.id.holder_recyclerview)
    LinearLayout mRVHolder;

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
        // rendering
        View view = inflater.inflate(R.layout.dot_wall, container, false);
        ButterKnife.bind(this, view);

        // grab user
        String dotUid = getArguments().getString(K.KEY_DOT.name());
        Cursor cursor = getActivity().getContentResolver().query(SchemaDots.CONTENT_URI, null, SchemaDots.COLUMN_UID + "=?", new String[]{dotUid}, null);
        if (!cursor.moveToFirst()) {
            getActivity().finish();
        }
        User user = Utils.cursorToUserEntity(cursor);
        cursor.close();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });

        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.TransparentText);

        // about user
        setUpMetaData(user);

        // user posts
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
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
        rv.setLayoutManager(mLayoutManager);
        mRVHolder.addView(rv);
        setupRecyclerView(rv);

        //update user details
        Bundle iArgs = new Bundle();
        iArgs.putString(AppConstants.K.DOT.name(), dotUid);
        Intent intent =
                new Intent(view.getContext(), DotDetailsService.class);
        intent.putExtras(iArgs);
        getActivity().startService(intent);

        return view;
    }

    private void setUpMetaData(User user) {
        String pinchHandle = String.format(getActivity().getApplicationContext().getString(R.string.pinch_handle), user.getPinchHandle());

        mCollapsingToolbarLayout.setTitle(pinchHandle);

        String fName = user.getFname();
        String lName = user.getLname();
        // tag map
        if (StringUtils.isBlank(user.getPhoto())) {
            mVS.setDisplayedChild(1);
            String initials = StringUtils.join(StringUtils.substring(fName, 0, 1), StringUtils.substring(lName, 0, 1));
            mDotImageText.setText(initials);
        } else {
            mVS.setDisplayedChild(0);

            Postprocessor postprocessor = new BasePostprocessor() {
                @Override
                public String getName() {
                    return "redMeshPostprocessor";
                }

                @Override
                public void process(Bitmap bitmap) {


                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            applyPalette(palette);
                        }
                    });
                }
            };

            mDotImage.populateImageViewWithAdjustedAspect(user.getPhoto(), postprocessor);
        }

        mDotName.setText(StringUtils.join(new String[]{fName, lName}, " "));
        mDotHandle.setText(pinchHandle);
        if (user.getFollowersCount() != null) {
            mDotFollowersCnt.setText(user.getFollowersCount() + "");
        }

        if (user.getFollowersCount() != null) {
            mDotFollowersCnt.setText(user.getFollowersCount() + "");
        }

        if (user.getPostsCount() != null) {
            mDotPostsCnt.setText(user.getPostsCount() + "");
        }

        if (user.getBlog() != null) {
            mDotBlog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(getView(), "blog clicked", Snackbar.LENGTH_LONG).show();
                }
            });
            mDotBlog.setVisibility(View.VISIBLE);
        }

        // conditional show about user section
        if (StringUtils.isNotBlank(user.getSummary())) {
            mDotAbout.setVisibility(View.VISIBLE);
            mDotAbout.setText(user.getSummary());
        }

    }

    private void setupRecyclerView(RecyclerView rv) {
        String dotUid = getArguments().getString(K.KEY_DOT.name());
        rv.setHasFixedSize(true);
        Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, SchemaPosts.COLUMN_OWNER + "=?", new String[]{dotUid}, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        rv.setAdapter(mViewAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    private void applyPalette(Palette palette) {
        int primaryDark = getResources().getColor(R.color.primary_dark);
        int primary = getResources().getColor(R.color.primary);
        mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
        mCollapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        updateBackground(palette);
    }

    private void updateBackground(Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.red_400));

        mFab.setRippleColor(lightVibrantColor);
        mFab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
    }

    @Subscribe
    public void onDotDetailsRefreshEvent(final Events.DotDetailsRefreshEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String dotUid = getArguments().getString(K.KEY_DOT.name());
                    Cursor cursor = getActivity().getContentResolver().query(SchemaDots.CONTENT_URI, null, SchemaDots.COLUMN_UID + "=?", new String[]{dotUid}, null);
                    if (!cursor.moveToFirst()) {
                        getActivity().finish();
                    }
                    final User user = Utils.cursorToUserEntity(cursor);
                    cursor.close();
                    setUpMetaData(user);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
    }
}