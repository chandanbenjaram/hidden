package droid.samepinch.co.app.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 6/25/15.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final ImageView mImageView;
    public final TextView mTextView;
    public String mBoundString;


    public PostViewHolder(View view) {
        super(view);
        mView = view;
        mImageView = (ImageView) view.findViewById(R.id.avatar);
        mTextView = (TextView) view.findViewById(android.R.id.text1);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mTextView.getText();
    }
}