package co.samepinch.android.app.helpers.adapters;


import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;

import co.samepinch.android.app.R;
import co.samepinch.android.data.dao.SchemaTags;

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
