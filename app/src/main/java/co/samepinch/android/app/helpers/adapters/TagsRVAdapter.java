package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.ImageOrTextViewAdapter;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class TagsRVAdapter extends CursorRecyclerViewAdapter<TagRVHolder> {
    private final Context context;
    private final ItemEventListener itemEventListener;
    public TagsRVAdapter(Context context, Cursor cursor, ItemEventListener itemEventListener) {
        super(context, cursor);
        this.context = context;
        this.itemEventListener = itemEventListener;
    }

    @Override
    public TagRVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.tag_select, parent, false);
        TagRVHolder viewHolder = new TagRVHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TagRVHolder viewHolder, Cursor cursor) {
        viewHolder.onBindViewHolderImpl(cursor);

        if(itemEventListener != null){
            viewHolder.mTagName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemEventListener.onClick(viewHolder.mTagName.getText());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static interface ItemEventListener<T>{
        void onClick(T t);
    }
}
