package droid.samepinch.co.app.helpers.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.Bind;
import butterknife.ButterKnife;
import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/22/15.
 */
public class SIMView extends RelativeLayout {

    @Bind(R.id.sim_id)
    SimpleDraweeView mSIMView;

    String mImagePath;

    public SIMView(Context context) {
        super(context);

        init();
        onFinishInflate();
    }

    public SIMView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
        getAttributes(context, attrs);
    }

    public SIMView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SIM,
                0, 0);
        try {
            mImagePath = typedArray.getString(R.styleable.SIM_imagePath);
        } finally {
            typedArray.recycle();
        }
    }

    private void init() {
        inflate(getContext(), R.layout.widget_sim, this);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        mImagePath = "http://www.maisasolutions.com/img/logo.png";
        setSIMView(mImagePath);
    }

    public void setSIMView(String aImagePath) {
        Uri uri = Uri.parse(aImagePath);
        mSIMView.setImageURI(uri);
    }

    public String getmImagePath() {
        return mImagePath;
    }

    public void setmImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }
}
