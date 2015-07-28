package droid.samepinch.co.app.helpers.adapters;


import android.view.View;
import android.widget.TextView;

import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostContentRVHolder extends PostDetailsRVHolder{
    TextView mContentText;

    public PostContentRVHolder(View itemView) {
        super(itemView);
        mContentText = (TextView) mView.findViewById(R.id.post_content);
    }
}
