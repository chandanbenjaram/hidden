package co.samepinch.android.app.helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.samepinch.android.app.CommentFragment;
import co.samepinch.android.app.DotWallFragment;
import co.samepinch.android.app.R;
import co.samepinch.android.app.TagWallFragment;

import static co.samepinch.android.app.helpers.AppConstants.K;

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
            case FRAGMENT_DOTWALL:
                fragment = new DotWallFragment();
                break;
            case FRAGMENT_COMMENT:
                fragment = new CommentFragment();
                break;
            case FRAGMENT_CHOOSE_HANDLE:
                fragment = new ChooseHandleFragment();
                break;
            case FRAGMENT_CREATE_POST:
                fragment = new PostCreateFragment();
                break;
            case FRAGMENT_EDIT_POST:
                fragment = new PostEditFragment();
                break;
            case FRAGMENT_MANAGE_TAGS:
                fragment = new ManageTagsFragment();
                break;
            case FRAGMENT_MANAGE_A_TAG:
                fragment = new TagEditFragment();
                break;
            case FRAGMENT_DOTEDIT:
                fragment = new DotEditFragment();
                break;
            case FRAGMENT_SETTINGS:
                fragment = new SettingsFragment();
                break;
            case FRAGMENT_WEBVIEW:
                fragment = new WebViewFragment();
                break;
            default:
                throw new IllegalArgumentException("un-known fragment " + targetFragment);
        }

        fragment.setArguments(mArgs);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.root_frame, fragment).commit();

        return view;
    }
}