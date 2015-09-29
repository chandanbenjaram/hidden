package co.samepinch.android.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.ToxicBakery.viewpager.transforms.ZoomInTransformer;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.Postprocessor;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.ImageUtils;
import co.samepinch.android.app.helpers.RootActivity;
import co.samepinch.android.app.helpers.SmartFragmentStatePagerAdapter;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.intent.DotDetailsService;
import co.samepinch.android.app.helpers.misc.FragmentLifecycle;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dto.User;

public class MainActivityIn extends AppCompatActivity {
    public static final String TAG = "MainActivityIn";
    private static final int INTENT_LOGOUT = 1;
    private static final int TAB_ITEM_COUNT = 2;

    @Bind(R.id.bottomsheet)
    BottomSheetLayout mBottomsheet;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    /**
     * BODY
     */
    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    /**
     * NAVIGATION RELATED
     */
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.nav_view)
    NavigationView mNavigationView;

    @Bind(R.id.dot_wall_switch)
    ViewSwitcher mVS;

    @Bind(R.id.backdrop)
    ImageView mBackdrop;

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

    @Bind(R.id.dot_wall_edit)
    ImageView mDotEdit;

    private LocalHandler mHandler;

    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (Utils.isLoggedIn()) {
                String userStr = Utils.PreferencesManager.getInstance().getValue(AppConstants.API.PREF_AUTH_USER.getValue());
                Gson gson = new Gson();
                mUser = gson.fromJson(userStr, User.class);
            } else {
                throw new IllegalStateException("failed to load user.");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() == null ? "" : e.getMessage(), e);
            Intent intent = new Intent(this, RootActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (!this.isFinishing()) {
                finish();
            }
        }

        setContentView(R.layout.activity_main_in);
        ButterKnife.bind(MainActivityIn.this);
        BusProvider.INSTANCE.getBus().register(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // handler
        mHandler = new LocalHandler(this);

        setupDrawerContent(mUser, true);
        setupViewPager();

        //update user details
        Bundle iArgs = new Bundle();
        iArgs.putString(AppConstants.K.DOT.name(), mUser.getUid());
        Intent intent =
                new Intent(getApplicationContext(), DotDetailsService.class);
        intent.putExtras(iArgs);
        startService(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_LOGOUT) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, RootActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (!this.isFinishing()) {
                    finish();
                }
            }
        }
    }

    private void setupViewPager() {
        final TabItemAdapter pagerAdapter = new TabItemAdapter(getSupportFragmentManager(), TAB_ITEM_COUNT);
        mViewPager.setAdapter(pagerAdapter);
//        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setPageTransformer(false, new ZoomInTransformer());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                FragmentLifecycle active = (FragmentLifecycle) pagerAdapter.getItem(position);
                active.onResumeFragment();

                FragmentLifecycle passive;
                for (int i = 0; i < pagerAdapter.getCount(); i++) {
                    if (i == position) {
                        // skip
                        continue;
                    }
                    // rest call onPause even though redundant for already hidden
                    passive = (FragmentLifecycle) pagerAdapter.getItem(i);
                    passive.onPauseFragment();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // setup tabs
        mTabLayout.setupWithViewPager(mViewPager);
        // title tabs
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(getCustomTabView(i));
        }
    }

    public View getCustomTabView(int position) {
        View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_tab_main, null);
        ViewSwitcher vs = (ViewSwitcher) v.findViewById(R.id.tab_main_switch);
        vs.setDisplayedChild(position);
        return v;
    }

    private void setupDrawerContent(final User user, boolean init) {

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
                public void process(final Bitmap bitmap) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBackdrop.setImageBitmap(null);
                            Bitmap blurredBitmap = ImageUtils.blur(getApplicationContext(), bitmap);
                            mBackdrop.setImageBitmap(blurredBitmap);
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
        String pinchHandle = String.format(getApplicationContext().getString(R.string.pinch_handle), user.getPinchHandle());
        mDotHandle.setText(pinchHandle);
        if (user.getFollowersCount() != null) {
            mDotFollowersCnt.setText(Long.toString(user.getFollowersCount()));
        } else {
            mDotFollowersCnt.setVisibility(View.GONE);
        }

        if (StringUtils.isNotBlank(user.getSummary())) {
            mDotAbout.setText(user.getSummary());
        } else {
            mDotAbout.setVisibility(View.GONE);
        }

        if (user.getPostsCount() != null) {
            mDotPostsCnt.setText(Long.toString(user.getPostsCount()));
        } else {
            mDotPostsCnt.setVisibility(View.GONE);
        }

        if (Utils.isValidUri(user.getBlog())) {
            mDotBlog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(user.getBlog()));
                    startActivity(intent);
                }
            });
            mDotBlog.setVisibility(View.VISIBLE);
        } else {
            mDotBlog.setVisibility(View.GONE);
        }

        mDotEdit.setVisibility(View.VISIBLE);
        if (init) {
            mDotEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    // target
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_DOTEDIT.name());

                    // intent
                    Intent intent = new Intent(getApplicationContext(), ActivityFragment.class);
                    intent.putExtras(args);
                    startActivityForResult(intent, AppConstants.KV.REQUEST_EDIT_DOT.getIntValue());
                }
            });

            setupDrawerNavListener();
        }
    }

    private void setupDrawerNavListener() {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        Bundle args = new Bundle();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_post:
                                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_CREATE_POST.name());
                                break;
                            case R.id.nav_tags:
                                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_MANAGE_TAGS.name());
                                break;
                            case R.id.nav_settings:
                                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_SETTINGS.name());
                                break;
                            case R.id.nav_rules:
                                args.putString(AppConstants.K.REMOTE_URL.name(), AppConstants.API.URL_RULES.getValue());
                                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_WEBVIEW.name());
                                break;
                            case R.id.nav_sys_status:
                                args.putString(AppConstants.K.REMOTE_URL.name(), AppConstants.API.URL_SYS_STATUS.getValue());
                                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_WEBVIEW.name());
                                break;
                            case R.id.nav_t_n_c:
                                args.putString(AppConstants.K.REMOTE_URL.name(), AppConstants.API.URL_TERMS_COND.getValue());
                                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_WEBVIEW.name());
                                break;
                            case R.id.nav_rate_it:
                                Intent iRate = new Intent(Intent.ACTION_VIEW);
                                iRate.setData(Uri.parse(AppConstants.API.GPLAY_LINK.getValue()));
                                startActivity(iRate);
                                break;
                            case R.id.nav_feedback:
                                doFeedback();
                                break;
                            case R.id.nav_spread_it:
                                doSpreadIt();
                                break;

                            case R.id.nav_sign_out:
                                Intent logOutIntent = new Intent(getApplicationContext(), LogoutActivity.class);
                                startActivityForResult(logOutIntent, INTENT_LOGOUT);
                                break;
                            default:
                                Log.d(TAG, "do not know how to launch :: " + menuItem.getTitle());
                                break;
                        }
                        if (!args.isEmpty()) {
                            // intent
                            Intent intent = new Intent(getApplicationContext(), ActivityFragment.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtras(args);
                            startActivity(intent);
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.isLoggedIn()) {
            Intent intent = new Intent(this, RootActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (!this.isFinishing()) {
                finish();
            }
        }

        mNavigationView.getMenu().getItem(0).setChecked(true);
        //update user details
        Bundle iArgs = new Bundle();
        iArgs.putString(AppConstants.K.DOT.name(), mUser.getUid());
        Intent intent =
                new Intent(getApplicationContext(), DotDetailsService.class);
        intent.putExtras(iArgs);
        startService(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.clearOnPageChangeListeners();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    private void doSpreadIt() {
        final String subject = getApplicationContext().getString(R.string.share_subject);
        final String body = getApplicationContext().getString(R.string.share_body);

        // prepare menu options
        View menu = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bs_menu, mBottomsheet, false);
        LinearLayout layout = (LinearLayout) menu.findViewById(R.id.layout_menu_list);
        // sms
        TextView viaSMS = (TextView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.bs_raw_sms, null);
        viaSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomsheet.dismissSheet();
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
                intent.putExtra("sms_body", body);
                intent.putExtra(intent.EXTRA_TEXT, body);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        layout.addView(viaSMS);
        // email
        TextView viaEmail = (TextView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.bs_raw_email, null);
        viaEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomsheet.dismissSheet();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        layout.addView(viaEmail);
        mBottomsheet.showWithSheetView(menu);
    }

    private void doFeedback() {
        final String subject = getApplicationContext().getString(R.string.feedback_subject);
        final String to = getApplicationContext().getString(R.string.feedback_to);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.fab)
    public void onClickFAB() {
        Bundle args = new Bundle();
        args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_CREATE_POST.name());

        // intent
        Intent intent = new Intent(getApplicationContext(), ActivityFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(args);
        startActivity(intent);
    }

    @Subscribe
    public void onPostsRefreshedEvent(Events.PostsRefreshedEvent event) {
        Snackbar.make(this.findViewById(R.id.fab), "refreshed", Snackbar.LENGTH_LONG).show();
    }

    public class TabItemAdapter extends SmartFragmentStatePagerAdapter {
        private final int itemCount;

        public TabItemAdapter(FragmentManager fm, int itemCount) {
            super(fm);
            this.itemCount = itemCount;
        }

        @Override
        public int getCount() {
            return itemCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = getRegisteredFragment(position);
            if (fragment == null) {
                switch (position) {
                    case 0:
                        fragment = PostListFragment.newInstance(position);
                        break;
                    case 1:
                        fragment = FavPostListFragment.newInstance(position);
                        break;
                    default:
                        throw new IllegalStateException("non-bound position index: " + position);
                }
            }
            return fragment;
        }
    }

    @Subscribe
    public void onDotDetailsRefreshEvent(final Events.DotDetailsRefreshEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(SchemaDots.CONTENT_URI, null, SchemaDots.COLUMN_UID + "=?", new String[]{mUser.getUid()}, null);
                    if (cursor.moveToFirst()) {
                        User updatedUser = Utils.cursorToUserEntity(cursor);

                        Gson gson = new Gson();
                        // update stored user info
                        String userStr = Utils.PreferencesManager.getInstance().getValue(AppConstants.API.PREF_AUTH_USER.getValue());
                        User user = gson.fromJson(userStr, User.class);
                        user.setFname(updatedUser.getFname());
                        user.setLname(updatedUser.getLname());
                        user.setSummary(updatedUser.getSummary());
                        user.setBlog(updatedUser.getBlog());
                        user.setBadges(updatedUser.getBadges());
                        user.setPhoto(updatedUser.getPhoto());
                        user.setImageKey(updatedUser.getImageKey());
                        user.setPostsCount(updatedUser.getPostsCount());
                        user.setFollowersCount(updatedUser.getFollowersCount());
                        Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_AUTH_USER.getValue(), gson.toJson(user));
                        invalidateOptionsMenu();
                        setupDrawerContent(user, false);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<MainActivityIn> mActivity;

        public LocalHandler(MainActivityIn parent) {
            mActivity = new WeakReference<MainActivityIn>(parent);
        }
    }
}
