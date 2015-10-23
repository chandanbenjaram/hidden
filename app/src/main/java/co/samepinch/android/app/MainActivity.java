package co.samepinch.android.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.IntentPickerSheetView;

import org.apache.commons.lang3.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.RootActivity;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.SPFragmentPagerAdapter;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int INTENT_LOGIN = 0;

    @Bind(R.id.bottomsheet)
    BottomSheetLayout mBottomsheet;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.nav_view)
    NavigationView mNavigationView;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isLoggedIn()) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isLoggedIn()) {
            Intent intent = new Intent(this, RootActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (!this.isFinishing()) {
                finish();
            }
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);
        BusProvider.INSTANCE.getBus().register(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.menu_blue);
        ab.setDisplayHomeAsUpEnabled(true);

        setupDrawerContent(mNavigationView);

        SPFragmentPagerAdapter adapterViewPager = new SPFragmentPagerAdapter(getSupportFragmentManager());
        adapterViewPager.setCount(1);
        mViewPager.setAdapter(adapterViewPager);
        mViewPager.setPageTransformer(false, new DepthPageTransformer());

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(adapterViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(SPFragmentPagerAdapter.getTabView(getApplicationContext(), i));
        }

//        Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sp_icon);
//        mBackdrop.setImageBitmap(appIcon);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_LOGIN) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menuitem_sign_in_id:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent, INTENT_LOGIN);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        setupDrawerNavListener();
    }

    private void setupDrawerNavListener() {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        Bundle args = new Bundle();
                        switch (menuItem.getItemId()) {
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
                            case R.id.nav_spread_it:
                                doSpreadIt(MainActivity.this, mBottomsheet);
                                break;
                            case R.id.nav_rate_it:
                                Intent iRate = new Intent(Intent.ACTION_VIEW);
                                iRate.setData(Uri.parse(AppConstants.API.GPLAY_LINK.getValue()));
                                startActivity(iRate);
                                break;
                            case R.id.nav_feedback:
                                doFeedback();
                                break;
                            case R.id.nav_sign_in:
                                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivityForResult(loginIntent, INTENT_LOGIN);
                                break;
                            default:
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


    protected static void doSpreadIt(final Activity activity, final BottomSheetLayout bs) {
        final String subject = activity.getString(R.string.share_subject);
        final String body = activity.getString(R.string.share_body);
        // prepare menu options
        View menu = LayoutInflater.from(activity).inflate(R.layout.bs_menu, bs, false);
        final LinearLayout layout = (LinearLayout) menu.findViewById(R.id.layout_menu_list);
        // email
        TextView viaEmail = (TextView) LayoutInflater.from(activity).inflate(R.layout.bs_raw_email, null);
        viaEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bs.isSheetShowing()){
                    bs.dismissSheet();
                }
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    activity.startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, SPApplication.getContext().getString(R.string.msg_no_email), Toast.LENGTH_SHORT).show();
                }
            }
        });
        layout.addView(viaEmail);

        // others
        TextView viaOther = (TextView) LayoutInflater.from(activity).inflate(R.layout.bs_raw_via_other, null);
        viaOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sms
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(Intent.EXTRA_TITLE, subject);
                shareIntent.putExtra(Intent.EXTRA_TEXT, body);

                shareIntent.setType("text/plain");
                IntentPickerSheetView sheetView = new IntentPickerSheetView(activity, shareIntent, StringUtils.EMPTY, new IntentPickerSheetView.OnIntentPickedListener() {
                    @Override
                    public void onIntentPicked(Intent intent) {
                        if(bs.isSheetShowing()){
                            bs.dismissSheet();
                        }
                        activity.startActivity(intent);
                    }
                });
                layout.removeAllViews();
                layout.addView(sheetView);
            }
        });
        layout.addView(viaOther);
        bs.showWithSheetView(menu);
    }

    private void doFeedback() {
        final String subject = SPApplication.getContext().getString(R.string.feedback_subject);
        final String to =SPApplication.getContext().getString(R.string.feedback_to);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, SPApplication.getContext().getString(R.string.msg_no_email), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fab)
    public void onClickFAB() {
        Toast.makeText(MainActivity.this, SPApplication.getContext().getString(R.string.msg_login_req), Toast.LENGTH_SHORT).show();

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, INTENT_LOGIN);
    }
}
