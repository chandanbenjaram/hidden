package droid.samepinch.co.app.helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import droid.samepinch.co.app.R;
import droid.samepinch.co.app.TagWallFragment;

import static droid.samepinch.co.app.helpers.AppConstants.K;

/**
 * Created by imaginationcoder on 7/11/15.
 */
public class RootFragment extends Fragment {
    private Bundle mArgs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fragment args
        mArgs = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.root_fragment, container, false);

        Fragment fragment = null;
        String targetFragment = mArgs.getString(K.TARGET_FRAGMENT.name());
        switch (K.valueOf(targetFragment)) {
            case FRAGMENT_TAGWALL:
                fragment = new TagWallFragment();
                break;
            default:
                throw new IllegalArgumentException("invalid URI");
        }

        fragment.setArguments(mArgs);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.root_frame, fragment).commit();

        return view;
    }
}