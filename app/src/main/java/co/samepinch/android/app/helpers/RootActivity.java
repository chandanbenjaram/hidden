package co.samepinch.android.app.helpers;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.common.util.UriUtil;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import co.samepinch.android.app.MainActivity;
import co.samepinch.android.app.MainActivityIn;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.intent.ParseSyncService;
import co.samepinch.android.app.helpers.widget.SIMView;

import static co.samepinch.android.app.helpers.AppConstants.API.PREF_APP_HELLO_WORLD;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_APP_ACCESS_STATE;

public class RootActivity extends AppCompatActivity {
    public static final String TAG = "RootActivity";

    public static final String DIALOG_TITLE = "Reminder";
    public static final String DIALOG_UPDATE = "UPDATE";
    public static final String DIALOG_FORCED_UPDATE = "FORCED_UPDATE";
    public static final String DIALOG_REVIEW = "REVIEW";
    public static final String DIALOG_REFERRALS = "REFERRALS";
    public static final String DIALOG_LOGIN = "LOGIN";
    public static final String DIALOG_POST = "POST";

    private LocalHandler mHandler;

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.root_activity);
        setupWindowAnimations();

        mHandler = new LocalHandler(RootActivity.this);

        final boolean isFirstLaunch = Utils.isAppFirstTime();
        final boolean isLoggedIn = Utils.isLoggedIn();

        // sync parse state
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                syncStateToParse(isFirstLaunch, isLoggedIn);
            }
        });

        // has outstanding message?
        final String pendingDialog = Utils.PreferencesManager.getInstance().getValue(AppConstants.APP_INTENT.KEY_ADMIN_COMMAND.getValue());
        if (StringUtils.isNotBlank(pendingDialog)) {
            String pendingDialogMsg;
            switch (pendingDialog.toUpperCase()) {
                case DIALOG_UPDATE:
                    pendingDialogMsg = getApplicationContext().getString(R.string.dialog_update);
                    break;
                case DIALOG_FORCED_UPDATE:
                    pendingDialogMsg = getApplicationContext().getString(R.string.dialog_f_update);
                    break;
                case DIALOG_REVIEW:
                    pendingDialogMsg = getApplicationContext().getString(R.string.dialog_review);
                    break;
                case DIALOG_REFERRALS:
                    pendingDialogMsg = getApplicationContext().getString(R.string.dialog_referrals);
                    break;
                case DIALOG_LOGIN:
                    pendingDialogMsg = getApplicationContext().getString(R.string.dialog_login);
                    break;
                case DIALOG_POST:
                    pendingDialogMsg = getApplicationContext().getString(R.string.dialog_post);
                    break;
                default:
                    pendingDialogMsg = null;
                    break;
            }

            if (!StringUtils.equalsIgnoreCase(pendingDialog, DIALOG_FORCED_UPDATE)) {
                Utils.PreferencesManager.getInstance().remove(AppConstants.APP_INTENT.KEY_ADMIN_COMMAND.getValue());
            }

            if (StringUtils.isNotBlank(pendingDialog)) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(DIALOG_TITLE)
                        .content(pendingDialogMsg)
                        .positiveText(R.string.dialog_ok)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                // dialog dismiss
                                if (materialDialog.isShowing()) {
                                    materialDialog.dismiss();
                                }

                                // handle continue action
                                if (StringUtils.equalsIgnoreCase(pendingDialog, DIALOG_FORCED_UPDATE)) {
                                    Intent iRate = new Intent(Intent.ACTION_VIEW);
                                    iRate.setData(Uri.parse(AppConstants.API.GPLAY_LINK.getValue()));
                                    startActivity(iRate);
                                    finish();
                                } else if (StringUtils.equalsIgnoreCase(pendingDialog, DIALOG_REVIEW)) {
                                    Intent iRate = new Intent(Intent.ACTION_VIEW);
                                    iRate.setData(Uri.parse(AppConstants.API.GPLAY_LINK.getValue()));
                                    startActivity(iRate);
                                } else {
                                    launchTargetActivity(isFirstLaunch, isLoggedIn);
                                }
                            }
                        })
                        .show();
                return;
            }
        }
        // show appropriate screen
        ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.bg_text_switch);
        vs.setDisplayedChild(isFirstLaunch ? 1 : 0);

        launchTargetActivity(isFirstLaunch, isLoggedIn);
    }

    private void launchTargetActivity(boolean isFirstLaunch, final boolean isLoggedIn) {
        // target activity to launch
        if (isFirstLaunch) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // reset first time
                    Utils.PreferencesManager.getInstance().setValue(PREF_APP_HELLO_WORLD.getValue(), StringUtils.EMPTY);

                    FrameLayout container = (FrameLayout) findViewById(R.id.bg_container);
                    container.removeAllViews();

                    Uri bgResourceUri = new Uri.Builder()
                            .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                            .pgath(String.valueOf(R.drawable.welcome))
                            .build();

                    SIMView bgImageView = new SIMView(getApplicationContext());
                    bgImageView.setIsClickDisabled(Boolean.TRUE);
                    bgImageView.populateImageViewWithAdjustedAspect(bgResourceUri.toString());
                    bgImageView.setDuplicateParentStateEnabled(true);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RootActivity.this.launchTargetActivity(isLoggedIn);
                        }
                    }, 1999);
                    container.addView(bgImageView);
                }
            }, 999);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RootActivity.this.launchTargetActivity(isLoggedIn);
                }
            }, 299);
        }
    }

    private void launchTargetActivity(final boolean isLoggedIn) {
        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(RootActivity.this, MainActivityIn.class);
        } else {
            intent = new Intent(RootActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(RootActivity.this, R.anim.fade_in_n_out, R.anim.fade_in_n_out);
        ActivityCompat.startActivity(RootActivity.this, intent, options.toBundle());
    }

    private void syncStateToParse(boolean isFirstLaunch, boolean isLoggedIn) {
        // call for intent
        Intent intent =
                new Intent(getApplicationContext(), ParseSyncService.class);
        Bundle iArgs = new Bundle();
        iArgs.putInt(KEY_APP_ACCESS_STATE.getValue(), isLoggedIn ? 1 : (isFirstLaunch ? 0 : -1));
        intent.putExtras(iArgs);
        startService(intent);
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
            getWindow().setEnterTransition(slide);

            Slide fade = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
            getWindow().setExitTransition(fade);
        }
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<RootActivity> mActivity;

        public LocalHandler(RootActivity parent) {
            mActivity = new WeakReference<RootActivity>(parent);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }
}