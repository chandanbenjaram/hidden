package co.samepinch.android.app.helpers.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.data.dto.PostDetails;

public class TagRVHolder extends RecyclerView.ViewHolder {
    CheckedTextView mTagName;

    public TagRVHolder(View itemView) {
        super(itemView);
        mTagName = (CheckedTextView) itemView.findViewById(R.id.tag_name);
        setIsRecyclable(false);
    }

    void onBindViewHolderImpl(Cursor cursor) {
//        int imgIdx = cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE);
//        String imgStr = imgIdx > -1 ? cursor.getString(imgIdx) : null;
//        if (StringUtils.isNotBlank(imgStr)) {
//            Utils.setupLoadingImageHolder(mTagImage, imgStr);
//        }
        mTagName.setText(cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_NAME)));
    }

}
