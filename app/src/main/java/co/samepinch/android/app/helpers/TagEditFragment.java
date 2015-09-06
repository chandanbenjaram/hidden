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
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaTags;

public class TagEditFragment extends Fragment {
    public static final String TAG = "TagEditFragment";

    @Bind(R.id.bg_container)
    FrameLayout mBGContainer;

    @Bind(R.id.tag_edit_save)
    ImageButton mTagEditSaveBtn;

    @Bind(R.id.tag_edit_cancel)
    ImageButton mTagEditCancelBtn;

    //tag_subscription_switch
    @Bind(R.id.tag_subscription_switch)
    MaterialAnimatedSwitch aSubscriptionSwitch;

    ProgressDialog progressDialog;
    private LocalHandler mHandler;
    private String mTagId;

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

        String tag = getArguments().getString(AppConstants.APP_INTENT.KEY_TAG.getValue());
        Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{tag}, null);
        if (!cursor.moveToFirst()) {
            getActivity().finish();
        }
        // grad tag uid for API calls
        mTagId = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_UID));

        // fill image as background
        String imgUrl = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE));
        if (StringUtils.isNotBlank(imgUrl)) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);

            SIMView tagImgView = new SIMView(getActivity().getApplicationContext());
            tagImgView.populateImageViewWithAdjustedAspect(imgUrl, new Integer[]{size.x, size.y});
            mBGContainer.addView(tagImgView);
        }

        return view;
    }


    @OnClick(R.id.tag_edit_save)
    public void onSaveEvent() {
        // prevent further clicks
        mTagEditSaveBtn.setEnabled(false);
        mTagEditCancelBtn.setEnabled(false);

        Log.d(TAG, "is checked.." + aSubscriptionSwitch.isChecked());
    }

    @OnClick(R.id.tag_edit_cancel)
    public void onCancelEvent() {
        getActivity().finish();
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<TagEditFragment> mActivity;

        public LocalHandler(TagEditFragment parent) {
            mActivity = new WeakReference<TagEditFragment>(parent);
        }
    }
}