package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.samepinch.android.app.R;
import co.samepinch.android.data.dao.SchemaTags;

public class TagsToManageRVAdapter extends CursorRecyclerViewAdapter<TagToManagerRVHolder> {
    private final Context mContext;
    ItemEventListener mItemEventListener;
    public TagsToManageRVAdapter(Context context, Cursor cursor, ItemEventListener eventListener) {
        super(context, cursor);
        this.mContext = context;
        this.mItemEventListener = eventListener;
    }

    @Override
    public TagToManagerRVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.tag_to_manage, parent, false);
        TagToManagerRVHolder viewHolder = new TagToManagerRVHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TagToManagerRVHolder viewHolder, Cursor cursor) {
        final String tagId = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_NAME));

        viewHolder.onBindViewHolderImpl(cursor);
        viewHolder.mTagImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mItemEventListener.onClick(tagId);
            }
        });
    }

    public static interface ItemEventListener<T> {
        void onClick(T t);
    }
}
