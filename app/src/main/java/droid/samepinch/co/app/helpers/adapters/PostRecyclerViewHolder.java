package droid.samepinch.co.app.helpers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostRecyclerViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final SimpleDraweeView mAvatarView;
    public final TextView mWallPostDotView, mWallPostContentView, mWallPostViewsView, mCommentersCount, mWallTags;
    public final LinearLayout mWallPostCommentersLayout;


    public String mBoundString;

    public PostRecyclerViewHolder(View view) {
        super(view);
        mView = view;
        mAvatarView = (SimpleDraweeView) view.findViewById(R.id.avatar);
        mWallPostDotView = (TextView) view.findViewById(R.id.wall_post_dot);
        mWallPostContentView = (TextView) view.findViewById(R.id.wall_post_content);
        mWallPostViewsView = (TextView) view.findViewById(R.id.wall_post_views);
        mWallPostCommentersLayout = (LinearLayout) view.findViewById(R.id.wall_post_commenters);
        mCommentersCount = (TextView) view.findViewById(R.id.wall_commenters_count);
        mWallTags = (TextView) view.findViewById(R.id.wall_tags);
    }
}
