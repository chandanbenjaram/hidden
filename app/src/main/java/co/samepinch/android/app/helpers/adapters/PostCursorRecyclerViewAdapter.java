package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.samepinch.android.app.R;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<PostRecyclerViewHolder> {
    public static final String TAG = "PostCursorRecyclerView";

    final Context mContext;

    public PostCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(cursor);
        this.mContext = context;
    }

    @Override
    public PostRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item, parent, false);
        // setup view
        PostRecyclerViewHolder vh = new PostRecyclerViewHolder(mContext, v);
        return vh;
    }

    @Override
    public void onBindViewHolderCursor(final PostRecyclerViewHolder vh, Cursor cursor) {
        vh.onBindViewHolderImpl(cursor);
    }
}