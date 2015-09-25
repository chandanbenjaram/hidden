package co.samepinch.android.app;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.RootActivity;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.SPFragmentPagerAdapter;
import co.samepinch.android.app.helpers.intent.PostsPullService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int INTENT_LOGIN = 0;

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

    SPFragmentPagerAdapter adapterViewPager;

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
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        setupDrawerContent(mNavigationView);
        setupViewPager(mViewPager);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(adapterViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setCustomView(SPFragmentPagerAdapter.getTabView(getApplicationContext(), i));
        }
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menuitem_sign_in_id).setVisible(true);
        menu.findItem(R.id.menuitem_sign_out_id).setVisible(false);

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

    private void setupViewPager(ViewPager viewPager) {
        adapterViewPager = new SPFragmentPagerAdapter(getSupportFragmentManager());
        adapterViewPager.setCount(1);
        viewPager.setAdapter(adapterViewPager);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @OnClick(R.id.fab)
    public void onClickFAB() {
        Bundle iArgs = new Bundle();
        Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
        Map<String, String> pPosts = pref.getValueAsMap(AppConstants.API.PREF_POSTS_LIST.getValue());
        for (Map.Entry<String, String> e : pPosts.entrySet()) {
            iArgs.putString(e.getKey(), e.getValue());
        }

        // call for intent
        Intent mServiceIntent =
                new Intent(getApplicationContext(), PostsPullService.class);
        mServiceIntent.putExtras(iArgs);
        startService(mServiceIntent);
    }

    @Subscribe
    public void onPostsRefreshedEvent(Events.PostsRefreshedEvent event) {
        Snackbar.make(this.findViewById(R.id.fab), "refreshed", Snackbar.LENGTH_LONG).show();
    }
}
