package droid.samepinch.co.app.helpers.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.apache.commons.lang3.StringUtils;

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
        initView(context);
//        onFinishInflate();
    }

    public SIMView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(context);
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

    private void initView(Context context) {
        this.inflate(context, R.layout.widget_sim, this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        populateImageView(mImagePath);
    }

    public String getmImagePath() {
        return mImagePath;
    }

    public void setmImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public void populateImageView(String imgUri) {
        if (StringUtils.isBlank(imgUri)) {
            return;
        }
//
        ImageRequest fImageReq =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUri)).build();
        DraweeController contrlr = Fresco.newDraweeControllerBuilder()
                .setImageRequest(fImageReq)
                .setAutoPlayAnimations(true)
                .build();
        mSIMView.setController(contrlr);
//        Uri uri = Uri.parse(imgUri);
//        mSIMView.setImageURI(uri);
    }
}
