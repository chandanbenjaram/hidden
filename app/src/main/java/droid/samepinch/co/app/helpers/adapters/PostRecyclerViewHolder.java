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
    public final SimpleDraweeView mAvatarView;
    public final TextView mWallPostDotView, mWallPostContentView, mWallPostCommentersView, mWallPostViewsView;

    public String mBoundString;

    public PostRecyclerViewHolder(View view) {
        super(view);
        mView = view;
        mAvatarView = (SimpleDraweeView) view.findViewById(R.id.avatar);
        mWallPostDotView = (TextView) view.findViewById(R.id.wall_post_dot);
        mWallPostContentView = (TextView) view.findViewById(R.id.wall_post_content);
        mWallPostCommentersView = (TextView) view.findViewById(R.id.wall_post_commenters);
        mWallPostViewsView = (TextView) view.findViewById(R.id.wall_post_views);
    }
}
