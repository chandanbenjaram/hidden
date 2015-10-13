package co.samepinch.android.app.helpers.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
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
import butterknife.OnClick;
import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.AppConstants;

/**
 * Created by imaginationcoder on 7/22/15.
 */
public class SIMView extends RelativeLayout {

    @Bind(R.id.sim_id)
    SimpleDraweeView mSIMView;

    String mImagePath;
    boolean isClickDisabled;

    public boolean isClickDisabled() {
        return isClickDisabled;
    }

    public void setIsClickDisabled(boolean isClickDisabled) {
        this.isClickDisabled = isClickDisabled;
    }

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

    public void setAspectRatio(float aspectRatio) {
        mSIMView.setAspectRatio(aspectRatio);
    }

    public void setImageHierarchy(GenericDraweeHierarchy hierarchy){
        mSIMView.setHierarchy(hierarchy);
    }

    public void setRoundingParams(RoundingParams roundingParams) {
        mSIMView.getHierarchy().setRoundingParams(roundingParams);
    }

    public void populateImageViewWithAdjustedAspect(String imgUri, Postprocessor postprocessor, Integer... resizeDimensions) {
        mImagePath = imgUri;
        if (StringUtils.isBlank(imgUri)) {
            return;
        }
//        imgUri = "http://www.aoaophoto.com/Sample/imgs/animation.gif";
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
        mSIMView.setAspectRatio(mSIMView.getAspectRatio());
    }


    @OnClick(R.id.sim_id)
    public void onClick() {
        if(isClickDisabled()){
            return;
        }

        Bundle args = new Bundle();
        // target
        args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_IMAGEVIEW.name());
        args.putString(AppConstants.K.IMAGE_URL.name(), mImagePath);

        // intent
        Intent intent = new Intent(mSIMView.getContext(), ActivityFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(args);
        mSIMView.getContext().startActivity(intent);
    }
}
