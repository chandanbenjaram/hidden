package co.samepinch.android.app.helpers.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.apache.commons.lang3.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;

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
        mSIMView.setAspectRatio(1.33f);
        ImageRequest fImageReq =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUri)).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(fImageReq)
                .setOldController(mSIMView.getController())
                .setAutoPlayAnimations(true)
                .build();
        mSIMView.setController(controller);
    }

    public void populateImageViewWithAdjustedAspect(String imgUri) {
        if (StringUtils.isBlank(imgUri)) {
            return;
        }

        // aspect adjust
        ControllerListener listener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                mSIMView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
            }
        };

        ImageRequest fImageReq =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUri)).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(fImageReq)
                .setOldController(mSIMView.getController())
                .setAutoPlayAnimations(true)
                .setControllerListener(listener)
                .build();
        mSIMView.setController(controller);
    }
}
