package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Window;
import android.view.animation.AnimationUtils;

import java.lang.ref.WeakReference;

import co.samepinch.android.app.MainActivity;
import co.samepinch.android.app.MainActivityIn;
import co.samepinch.android.app.R;

public class RootActivity extends AppCompatActivity {

    private LocalHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);

//        getWindow().setWindowAnimations(R.anim.fadein);
//        AnimationUtils.loadAnimation(RootActivity.this);
//        getWindow().setWindowAnimations(R.anim.fade_in);

        setContentView(R.layout.root_activity);
        mHandler = new LocalHandler(RootActivity.this);


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
        }, 499);

        setupWindowAnimations();
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