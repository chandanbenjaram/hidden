package co.samepinch.android.app.helpers.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

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
        populateImageViewWithAdjustedAspect(imgUri);
    }

    public void populateImageViewWithAdjustedAspect(String imgUri, Integer... resizeDimensions) {
        populateImageViewWithAdjustedAspect(imgUri, null, resizeDimensions);
    }

    public void setRoundingParams(RoundingParams roundingParams) {
        mSIMView.getHierarchy().setRoundingParams(roundingParams);
    }

    public void populateImageViewWithAdjustedAspect(String imgUri, Postprocessor postprocessor, Integer... resizeDimensions) {
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
        ImageRequestBuilder imgReqBldr = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUri));
        if (resizeDimensions != null && resizeDimensions.length > 1) {
            int width = resizeDimensions[0];
            int height = resizeDimensions[1];
            imgReqBldr.setResizeOptions(new ResizeOptions(width, height));
        }

        // user call back
        if (postprocessor != null) {
            imgReqBldr.setPostprocessor(postprocessor);
        }

        ImageRequest fImageReq = imgReqBldr.build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(fImageReq)
                .setOldController(mSIMView.getController())
                .setAutoPlayAnimations(true)
                .setControllerListener(listener)
                .build();
        mSIMView.setController(controller);
        mSIMView.setDrawingCacheEnabled(true);
        mSIMView.buildDrawingCache(true);
    }
}
