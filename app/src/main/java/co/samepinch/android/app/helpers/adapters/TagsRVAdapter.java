package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.ImageOrTextViewAdapter;
import co.samepinch.android.data.dao.SchemaTags;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class TagsRVAdapter extends CursorRecyclerViewAdapter<TagRVHolder> {
    private final Context context;
    private final ItemEventListener itemEventListener;
    final Set<String> tagSelections;

    public TagsRVAdapter(Context context, Cursor cursor, ItemEventListener itemEventListener) {
        super(context, cursor);
        this.context = context;
        this.itemEventListener = itemEventListener;
        this.tagSelections = new HashSet<>();
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

        final String tagId = cursor.getString(cursor.getColumnIndex(SchemaTags.COLUMN_NAME));
        viewHolder.mTagName.setChecked(tagSelections.contains(tagId));

        viewHolder.mTagName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckedTextView _view = (CheckedTextView) view;
                _view.toggle();
                if (_view.isChecked()) {
                    tagSelections.add(_view.getText().toString());
                } else {
                    tagSelections.remove(_view.getText().toString());
                }
                if (itemEventListener != null) {
                    itemEventListener.onChange(tagSelections.toArray(new String[]{}));
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static interface ItemEventListener<T> {
        void onChange(T t);
    }
}
