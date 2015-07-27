package droid.samepinch.co.app.helpers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostDetailsRVHolder extends RecyclerView.ViewHolder {
    View mView;
    TextView mCommentText;
    public PostDetailsRVHolder (View itemView) {
        super (itemView);
        mView = itemView;
        mCommentText = (TextView) mView.findViewById(R.id.post_comment);
    }
}
