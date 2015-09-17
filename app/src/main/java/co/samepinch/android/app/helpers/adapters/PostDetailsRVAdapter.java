package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.samepinch.android.app.R;

public class PostDetailsRVAdapter extends CursorRecyclerViewAdapter<PostDetailsRVHolder> {
    private final Context context;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;


    public PostDetailsRVAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return 1;
    }

    @Override
    public PostDetailsRVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        PostDetailsRVHolder viewHolder = null;
        switch (viewType) {
            case TYPE_HEADER:
                v = LayoutInflater.from(context)
                        .inflate(R.layout.post_details_content, parent, false);
                viewHolder = new PostContentRVHolder(v);
                break;

            case TYPE_ITEM:
                v = LayoutInflater.from(context)
                        .inflate(R.layout.post_comment_item, parent, false);
                viewHolder = new PostCommentsRVHolder(v);
                break;

            default:
                Thread.dumpStack();
                throw new IllegalStateException("un-known viewType=" + viewType);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PostDetailsRVHolder viewHolder, Cursor cursor) {
        viewHolder.onBindViewHolderImpl(cursor);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

}
