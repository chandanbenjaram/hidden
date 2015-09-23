package co.samepinch.android.app.helpers;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.fenjuly.mylibrary.ToggleExpandLayout;
import com.kyleduo.switchbutton.SwitchButton;
import com.squareup.otto.Subscribe;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;

public class SettingsFragment extends Fragment {
    public static final String TAG = "SettingsFragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.toggleLayout1)
    ToggleExpandLayout layout1;

    @Bind(R.id.toggleLayout2)
    ToggleExpandLayout layout2;

    @Bind(R.id.toggleLayout3)
    ToggleExpandLayout layout3;

    @Bind(R.id.switch_button1)
    SwitchButton switchButton1;

    @Bind(R.id.switch_button2)
    SwitchButton switchButton2;

    @Bind(R.id.switch_button3)
    SwitchButton switchButton3;

    ProgressDialog progressDialog;
    String mCurrUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes.
        setRetainInstance(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);

        // keep current logged in user id
        Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
        mCurrUserId = userInfo.get(AppConstants.APP_INTENT.KEY_UID.getValue());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });
        toolbar.setTitle("SETTINGS");
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        setupPreferences();
        return view;
    }

    private void setupPreferences() {
        setupLayoutWithToggleExpand(layout1, switchButton1);
        setupLayoutWithToggleExpand(layout2, switchButton2);
        setupLayoutWithToggleExpand(layout3, switchButton3);
    }

    private void setupLayoutWithToggleExpand(final ToggleExpandLayout aLayout, final SwitchButton aSwitch) {
        aLayout.setOnToggleTouchListener(new ToggleExpandLayout.OnToggleTouchListener() {
            @Override
            public void onStartOpen(int height, int originalHeight) {
            }

            @Override
            public void onOpen() {
                int childCount = aLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View view = aLayout.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(i));
                }
            }

            @Override
            public void onStartClose(int height, int originalHeight) {
                int childCount = aLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View view = aLayout.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(i));
                }
            }

            @Override
            public void onClosed() {

            }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    aLayout.open();
                } else {
                    aLayout.close();
                }
            }
        });
    }


    @Subscribe
    public void onTagsRefreshFailEvent(final Events.TagsRefreshFailEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(getView(), event.getMetaData().get(AppConstants.K.MESSAGE.name()), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.INSTANCE.getBus().register(this);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}