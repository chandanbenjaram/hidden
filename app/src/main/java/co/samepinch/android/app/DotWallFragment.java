package co.samepinch.android.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.Postprocessor;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.ImageUtils;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.app.helpers.intent.DotDetailsService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

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

    @Bind(R.id.backdrop)
    ImageView mBackdrop;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Bind(R.id.dot_wall_switch)
    ViewSwitcher mVS;

    @Bind(R.id.holder_recyclerview)
    LinearLayout mRVHolder;

    private LocalHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new LocalHandler(this);
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

    private void setUpMetaData(final User user) {
        String pinchHandle = String.format(getActivity().getApplicationContext().getString(R.string.pinch_handle), user.getPinchHandle());

        mCollapsingToolbarLayout.setTitle(pinchHandle);

        String fName = user.getFname();
        String lName = user.getLname();
        // tag map
        if (StringUtils.isBlank(user.getPhoto())) {
            mVS.setDisplayedChild(1);
            String initials = StringUtils.join(StringUtils.substring(fName, 0, 1), StringUtils.substring(lName, 0, 1));
            mDotImageText.setText(initials);
            applyPalette(null);
        } else {
            mVS.setDisplayedChild(0);


            Postprocessor postprocessor = new BasePostprocessor() {
                @Override
                public String getName() {
                    return "redMeshPostprocessor";
                }

                @Override
                public void process(final Bitmap bitmap) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap blurredBitmap = ImageUtils.blur(getActivity().getApplicationContext(), bitmap);
                            mBackdrop.setImageBitmap(blurredBitmap);

                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    applyPalette(palette);
                                }
                            });
                        }
                    });

                }
            };

            RoundingParams roundingParams = RoundingParams.asCircle();
            roundingParams.setBorder(R.color.light_blue_500, 1.0f);

            mDotImage.setRoundingParams(roundingParams);
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
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(user.getBlog()));
                    startActivity(intent);
                }
            });
            mDotBlog.setVisibility(View.VISIBLE);
        }

        // conditional show about user section
        if (StringUtils.isNotBlank(user.getSummary())) {
            mDotAbout.setVisibility(View.VISIBLE);
            mDotAbout.setText(user.getSummary());
        }

        if (user.getFollow()) {
            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(),
                    R.drawable.icon_like_postview_active_2x);
            mFab.setImageBitmap(bitmap);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(),
                    R.drawable.icon_like_postview_2x);
            mFab.setImageBitmap(bitmap);
        }
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = user.getFollow() == null || !user.getFollow() ? "follow" : "unfollow";
                new FollowActionTask().execute(new String[]{user.getUid(), command});
            }
        });
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
        int primary = getResources().getColor(R.color.colorPrimary);
        int primaryDark = getResources().getColor(R.color.colorPrimaryDark);

        if (palette == null) {
            mCollapsingToolbarLayout.setContentScrimColor(primary);
            mCollapsingToolbarLayout.setStatusBarScrimColor(primaryDark);
        } else {
            mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
            mCollapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        }
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
                    User user = Utils.cursorToUserEntity(cursor);
                    cursor.close();
                    setUpMetaData(user);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<DotWallFragment> mActivity;

        public LocalHandler(DotWallFragment parent) {
            mActivity = new WeakReference<DotWallFragment>(parent);
        }
    }

    private class FollowActionTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... args) {
            if (args == null || args.length < 2) {
                return Boolean.FALSE;
            }

            try {
                ReqNoBody req = new ReqNoBody();
                req.setToken(Utils.getNonBlankAppToken());
                req.setCmd(args[1]);

                //headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

                HttpEntity<ReqNoBody> payloadEntity = new HttpEntity<>(req, headers);
                String userUrl = StringUtils.join(new String[]{AppConstants.API.USERS.getValue(), args[0]}, "/");

                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(userUrl, HttpMethod.POST, payloadEntity, Resp.class);
                return resp.getBody().getStatus() == 200;
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mFab.setEnabled(true);

            // refresh call
            if (result) {
                //update user details
                Bundle iArgs = new Bundle();
                String dotUid = getArguments().getString(K.KEY_DOT.name());
                iArgs.putString(AppConstants.K.DOT.name(), dotUid);
                Intent intent =
                        new Intent(getActivity(), DotDetailsService.class);
                intent.putExtras(iArgs);
                getActivity().startService(intent);
            }
        }
    }
}