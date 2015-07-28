package droid.samepinch.co.app.helpers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/27/15.
 */
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
                        .inflate(R.layout.post_content, parent, false);
                viewHolder = new PostContentRVHolder(v);
                break;

            case TYPE_ITEM:
                v = LayoutInflater.from(context)
                        .inflate(R.layout.post_comments, parent, false);
                viewHolder = new PostCommentsRVHolder(v);
                break;

            default:
                throw new IllegalStateException("unknown viewType=" + viewType);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PostDetailsRVHolder viewHolder, Cursor cursor) {
//        StringBuilder dummyTxt = new StringBuilder();
//        for (int i = 0; i < 25; i++) {
//            dummyTxt.append("\r\n" + "DUMMY TEXT..." + i);
//        }

        if (viewHolder instanceof PostContentRVHolder) {
            PostContentRVHolder _header = (PostContentRVHolder) viewHolder;
            _header.mContentText.setText("I am header");
        } else if (viewHolder instanceof PostCommentsRVHolder) {
            PostCommentsRVHolder _item = (PostCommentsRVHolder) viewHolder;
            _item.mCommentText.setText("I am comment...");
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


}
