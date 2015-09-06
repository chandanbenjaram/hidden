package co.samepinch.android.app.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaTags;

public class TagEditFragment extends Fragment {
    public static final String TAG = "TagEditFragment";

    //    @Bind(R.id.toolbar)
//    Toolbar toolbar;
//
    @Bind(R.id.bg_container)
    FrameLayout mBGContainer;
//
//    @Bind(R.id.holder_recyclerview)
//    FrameLayout frameLayout;

    ProgressDialog progressDialog;
    private LocalHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // a handler
        mHandler = new LocalHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_edit, container, false);
        ButterKnife.bind(this, view);
//
//    /* adapt the image to the size of the display */
//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
//                getResources(), R.drawable.home), size.x, size.y, true);
//
//    /* fill the background ImageView with the resized image */
//        ImageView iv_background = (ImageView) view.findViewById(R.id.tag_edit_background);
//        iv_background.setImageBitmap(bmp);
//
//        String tagId = getArguments().getString(AppConstants.KV.REQUEST_EDIT_TAG.getKey());
        String tag = getArguments().getString(AppConstants.APP_INTENT.KEY_TAG.getValue());

        Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{tag}, null);
        if (!cursor.moveToFirst()) {
            getActivity().finish();
        }

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);

        String imgUrl = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE));

        SIMView tagImgView = new SIMView(getActivity().getApplicationContext());
        tagImgView.populateImageViewWithAdjustedAspect(imgUrl, new Integer[]{size.x, size.y});
        mBGContainer.addView(tagImgView);

        return view;
    }


    private static final class LocalHandler extends Handler {
        private final WeakReference<TagEditFragment> mActivity;

        public LocalHandler(TagEditFragment parent) {
            mActivity = new WeakReference<TagEditFragment>(parent);
        }
    }
}