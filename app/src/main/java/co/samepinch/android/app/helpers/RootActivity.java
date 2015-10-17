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

import com.facebook.common.util.UriUtil;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import co.samepinch.android.app.MainActivity;
import co.samepinch.android.app.MainActivityIn;
import co.samepinch.android.app.R;
import co.samepinch.android.app.SPApplication;
import co.samepinch.android.app.helpers.intent.ParseSyncService;
import co.samepinch.android.app.helpers.widget.SIMView;

import static co.samepinch.android.app.helpers.AppConstants.API.PREF_APP_HELLO_WORLD;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PARSE_ACCESS_STATE;

public class RootActivity extends AppCompatActivity {

    private LocalHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.root_activity);
        mHandler = new LocalHandler(RootActivity.this);
        setupWindowAnimations();
        if (Utils.isAppFirstTime()) {
            FrameLayout container = (FrameLayout) findViewById(R.id.bg_container);
            container.removeAllViews();


//            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            Uri bgResourceUri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.welcome))
                    .build();

            SIMView bgImageView = new SIMView(getApplicationContext());
            bgImageView.setIsClickDisabled(Boolean.TRUE);
            bgImageView.populateImageViewWithAdjustedAspect(bgResourceUri.toString());
            container.addView(bgImageView);

            // reset first time
            Utils.PreferencesManager.getInstance().setValue(PREF_APP_HELLO_WORLD.getValue(), StringUtils.EMPTY);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent;
                    if (Utils.isLoggedIn()) {
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
            }, 999);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent;
                    if (Utils.isLoggedIn()) {
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
            }, 299);
        }

        syncStateToParse();
    }

    private void syncStateToParse() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // call for intent
                Intent intent =
                        new Intent(SPApplication.getContext(), ParseSyncService.class);
                Bundle iArgs = new Bundle();
                iArgs.putInt(KEY_PARSE_ACCESS_STATE.getValue(), Utils.isLoggedIn() ? 1 : 0);
                intent.putExtras(iArgs);
                startService(intent);
            }
        });
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
}