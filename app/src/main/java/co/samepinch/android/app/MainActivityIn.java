package co.samepinch.android.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.SPFragmentPagerAdapter;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.app.helpers.widget.SIMView;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_LNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PHOTO;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PINCH_HANDLE;

public class MainActivityIn extends AppCompatActivity {
    public static final String TAG = "MainActivityIn";
    private static final int INTENT_LOGOUT = 1;

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

    @Bind(R.id.nav_header_switch)
    ViewSwitcher mHeaderSwitch;

    @Bind(R.id.nav_header_img)
    SIMView mNavHeaderImg;

    @Bind(R.id.nav_header_name)
    TextView mNavHeaderName;

    @Bind(R.id.nav_header_summary)
    TextView mNavHeaderSummary;

    SPFragmentPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
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


        setupDrawerContent();
        setupViewPager();

    }

    private void setupViewPager() {
        adapterViewPager = new SPFragmentPagerAdapter(getSupportFragmentManager());
        adapterViewPager.setCount(2);
        mViewPager.setAdapter(adapterViewPager);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(adapterViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(SPFragmentPagerAdapter.getTabView(getApplicationContext(), i));
        }
    }

    private void setupDrawerContent() {
        Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
        if (StringUtils.isNotBlank(userInfo.get(KEY_PHOTO.getValue()))) {
            mNavHeaderImg.populateImageViewWithAdjustedAspect(userInfo.get(KEY_PHOTO.getValue()));
        } else {
            String fName = userInfo.get(KEY_FNAME.getValue());
            String lName = userInfo.get(KEY_LNAME.getValue());
            String initials = StringUtils.join(StringUtils.substring(fName, 0, 1), StringUtils.substring(lName, 0, 1));
            mNavHeaderName.setText(initials);
            mHeaderSwitch.showNext();
        }

        String pinchHandle = String.format(getString(R.string.pinch_handle), userInfo.get(KEY_PINCH_HANDLE.getValue()));
        mNavHeaderSummary.setText(pinchHandle);

        // drawer nav events
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (!this.isFinishing()) {
                finish();
            }
        }

        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_LOGOUT) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
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
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return true;
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        return true;
//    }

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
}
