package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.fenjuly.mylibrary.ToggleExpandLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kyleduo.switchbutton.SwitchButton;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespUserDetails;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.USERS;

public class SettingsFragment extends Fragment {
    public static final String TAG = "SettingsFragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.toggleLayout1)
    ToggleExpandLayout layout1;

    @Bind(R.id.toggleLayout2)
    ToggleExpandLayout layout2;

    @Bind(R.id.switch_button1)
    SwitchButton switchButton1;

    @Bind(R.id.switch_button2)
    SwitchButton switchButton2;

    // options
    @Bind(R.id.settings_notifs_posts_likes_tags_switch)
    SwitchButton notifsPostsLikesTags;

    @Bind(R.id.settings_notifs_posts_likes_switch)
    SwitchButton notifsPostsLikes;

    @Bind(R.id.settings_notifs_posts_switch)
    SwitchButton notifsPosts;

    @Bind(R.id.settings_email_subscribe_switch)
    SwitchButton emailSubscribe;

    ProgressDialog progressDialog;
    User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);
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
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        try {
            Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
            Gson gson = new Gson();
            String userInfoStr = gson.toJson(userInfo);
            mUser = gson.fromJson(userInfoStr, User.class);
        } catch (Exception e) {
            // muted
            getActivity().finish();
        }

        setupLayoutWithToggleExpand(layout1, switchButton1);
        setupLayoutWithToggleExpand(layout2, switchButton2);

        setupData(mUser);
        return view;
    }

    private void setupData(User aUser) {
        // notification stuff
        if (aUser.getApnNotify() != null) {
            notifsPostsLikesTags.setChecked(aUser.getApnNotify() == 3);
            notifsPostsLikes.setChecked(aUser.getApnNotify() == 2);
            notifsPosts.setChecked(aUser.getApnNotify() == 1);

        }
        // email stuff
        if (aUser.getEmailNotify() != null) {
            emailSubscribe.setChecked(aUser.getEmailNotify().booleanValue());
        } else {
            emailSubscribe.setChecked(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dot_settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                (getActivity()).onBackPressed();
                return true;

            case R.id.menuitem_update:
                saveAction();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAction() {
        User userDTO = new User();
        // notifications
        Integer apnNotify = 3;
        if (notifsPostsLikesTags.isChecked()) {
            apnNotify = 3;
        } else if (notifsPostsLikes.isChecked()) {
            apnNotify = 2;
        } else if (notifsPosts.isChecked()) {
            apnNotify = 1;
        }

        // app notify
        userDTO.setApnNotify(apnNotify);

        // email
        userDTO.setEmailNotify(emailSubscribe.isChecked());

        progressDialog.setMessage("saving your preferences...");
        progressDialog.show();

        new SettingsUpdateTask().execute(userDTO);
    }


    private class SettingsUpdateTask extends AsyncTask<User, Integer, User> {
        @Override
        protected User doInBackground(User... users) {
            if (users == null || users.length < 1) {
                return null;
            }

            try {
                ReqSetBody req = new ReqSetBody();
                // set base args
                req.setToken(Utils.getNonBlankAppToken());
                req.setCmd("update");

                Map<String, String> args = new HashMap<>();
                Gson gson = new Gson();
                String userStr = gson.toJson(users[0]);

                Type mapType = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> body = gson.fromJson(userStr, mapType);

                // set body
                req.setBody(body);

                //headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);

                ResponseEntity<RespUserDetails> resp = RestClient.INSTANCE.handle().exchange(USERS.getValue(), HttpMethod.POST, payloadEntity, RespUserDetails.class);
                User updated;
                if (resp != null && resp.getBody() != null && (updated = resp.getBody().getBody()) != null) {
                    return updated;
                }
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                Log.e(TAG, resp == null ? "null" : resp.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            Utils.dismissSilently(progressDialog);
            try {
                if (user != null) {
                    Gson gson = new Gson();
                    String userStr = gson.toJson(user);
                    Type mapType = new TypeToken<Map<String, String>>() {
                    }.getType();
                    Map<String, String> userNew = gson.fromJson(userStr, mapType);

                    Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
                    userInfo.putAll(userNew);
                    Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_AUTH_USER.getValue(), userInfo);

                    String userInfoStr = gson.toJson(user);
                    mUser = gson.fromJson(userInfoStr, User.class);
                    setupData(mUser);
                    Snackbar.make(getView(), "updated successfully.", Snackbar.LENGTH_SHORT).show();
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                    return;
                }
            } catch (Exception e) {
                // muted
            }
            Snackbar.make(getView(), AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue(), Snackbar.LENGTH_LONG).show();
        }
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