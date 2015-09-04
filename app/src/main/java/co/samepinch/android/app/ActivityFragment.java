package co.samepinch.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import co.samepinch.android.app.helpers.RootFragment;
import co.samepinch.android.app.helpers.SmartFragmentStatePagerAdapter;

public class ActivityFragment extends AppCompatActivity {

    public static final String EXTRA_NAME = "ActivityFragment";

    ViewPager mPager;
    Adapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new Adapter(getSupportFragmentManager(), getIntent().getExtras());
        mPager.setAdapter(mPagerAdapter);
    }

    static class Adapter extends SmartFragmentStatePagerAdapter {
        private static int NUM_ITEMS = 1;
        private final Bundle args;
        public Adapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.args = null;
        }

        public Adapter(FragmentManager fragmentManager, Bundle args) {
            super(fragmentManager);
            this.args = args;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            Fragment frag = new RootFragment();
            frag.setArguments(args);
            return frag;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}