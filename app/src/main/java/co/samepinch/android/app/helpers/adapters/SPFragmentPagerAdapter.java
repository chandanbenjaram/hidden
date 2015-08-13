package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;

import co.samepinch.android.app.PostListFragment;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.SmartFragmentStatePagerAdapter;

/**
 * Created by cbenjaram on 8/13/15.
 */
public class SPFragmentPagerAdapter extends SmartFragmentStatePagerAdapter {
    private int count;

    public SPFragmentPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void setCount(int count) {
        this.count = count;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return count;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return new PostListFragment();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return new PostListFragment();
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? AppConstants.K.Wall.name() : "#" + position;
    }
//
        public View getTabView(Context context, int position){
            View v = LayoutInflater.from(context).inflate(R.layout.custom_tab_main, null);
            return v;
        }

//        @Override
//        public int getItemPosition(Object object) {
//            return super.getItemPosition(object);
//        }
}
