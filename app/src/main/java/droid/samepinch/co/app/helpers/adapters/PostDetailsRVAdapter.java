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

    public PostDetailsRVAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
    }

    @Override
    public PostDetailsRVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.post_comments, parent, false);

        return new PostDetailsRVHolder(v);
    }

    @Override
    public void onBindViewHolder(PostDetailsRVHolder viewHolder, Cursor cursor) {
                StringBuilder dummyTxt = new StringBuilder();

                for(int i=0; i< 25; i++){
            dummyTxt.append( "\r\n" + "DUMMY TEXT..." + i);
        }
//        dummyView.setText(dummyTxt);
        viewHolder.mCommentText.setText("a comment..." + dummyTxt);
    }
}
