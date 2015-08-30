package co.samepinch.android.app.helpers.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.data.dto.PostDetails;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class TagRVHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.tag_name)
    TextView mTagName;

    @Bind(R.id.tag_image)
    SimpleDraweeView mTagImage;

    View mView;

    public TagRVHolder(View itemView) {
        super(itemView);
        mView = itemView;
        ButterKnife.bind(this, itemView);
        setIsRecyclable(false);
    }

    void onBindViewHolderImpl(Cursor cursor) {
        int imgIdx = cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE);
        String imgStr = imgIdx > -1 ? cursor.getString(imgIdx) : null;
        if (StringUtils.isNotBlank(imgStr)) {
            Utils.setupLoadingImageHolder(mTagImage, imgStr);
        }
        mTagName.setText(cursor.getString(cursor.getColumnIndex("name")));
    }
}
