package droid.samepinch.co.app.helpers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostRecyclerViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final SimpleDraweeView mImageView;
    public final TextView mTextView;
    public String mBoundString;

    public PostRecyclerViewHolder(View view) {
        super(view);
        mView = view;
//        mImageView = (ImageView) view.findViewById(R.id.avatar);
        // http://media.giphy.com/media/9aGG56B1hgl44/giphy.gif
//        Uri uri = Uri.parse("https://asset.samepinch.co/uploads/user/photo/558d283a3962660009040000/67ad7a827d1bda965e19f77f8b713b97.JPEG");
        mImageView = (SimpleDraweeView) view.findViewById(R.id.avatar);
//        mImageView.setImageURI(uri);

        mTextView = (TextView) view.findViewById(android.R.id.text1);
    }
}
