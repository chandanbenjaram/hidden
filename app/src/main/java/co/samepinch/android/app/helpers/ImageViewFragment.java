package co.samepinch.android.app.helpers;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.misc.PinchToZoomDraweeView;

public class ImageViewFragment extends Fragment {
    public static final String TAG = "WebViewFragment";

    @Bind(R.id.image_view)
    PinchToZoomDraweeView mImage;

    private LocalHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new LocalHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.imageview, container, false);
        ButterKnife.bind(this, view);

        String imgUrl = getArguments().getString(AppConstants.K.IMAGE_URL.name(), null);
        if (StringUtils.isBlank(imgUrl)) {
            handleError("missing url. closing...");
            return view;
        }
        try {
//            mImage.setAspectRatio(1.33f);
            mImage.setImageURI(Uri.parse(imgUrl));
        } catch (Exception e) {
            handleError("error opening url. closing...");
            return view;
        }

//        mImage.setImageBitmap(Uri.parse(imgUrl));
        return view;
    }

    @OnClick(R.id.close)
    public void onCloseEvent() {
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<ImageViewFragment> mActivity;

        public LocalHandler(ImageViewFragment parent) {
            mActivity = new WeakReference<ImageViewFragment>(parent);
        }
    }

    private void handleError(String errMsg) {
        Snackbar.make(mImage, errMsg, Snackbar.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().finish();
            }
        }, 999);
    }

}