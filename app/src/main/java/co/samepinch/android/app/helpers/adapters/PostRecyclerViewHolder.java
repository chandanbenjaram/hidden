package co.samepinch.android.app.helpers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import co.samepinch.android.app.R;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostRecyclerViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final RelativeLayout mLayoutPostItem;

    public final SimpleDraweeView mAvatarView;
    public final TextView mAvatarName;

    public final TextView mWallPostDotView;
    public final TextView mWallPinchHandleView;

    public final SimpleDraweeView mWallPostImages;
    public final TextView mWallPostContentView;
    public final TextView mWallTags;
    public final LinearLayout mWallPostCommentersLayout;

    public final TextView mCommentersCount;
    public final TextView mWallPostViewsView;
    public final TextView mWallPostUpvoteView;
    public final TextView mWallPostDateView;



    public PostRecyclerViewHolder(View view) {
        super(view);
        mView = view;
        mLayoutPostItem = (RelativeLayout) view.findViewById(R.id.layout_post_item);
        mAvatarView = (SimpleDraweeView) view.findViewById(R.id.avatar);
        mAvatarName = (TextView) view.findViewById(R.id.avatar_name);

        mWallPostDotView = (TextView) view.findViewById(R.id.wall_post_dot);
        mWallPinchHandleView = (TextView) view.findViewById(R.id.wall_pinch_handle);

        mWallPostContentView = (TextView) view.findViewById(R.id.wall_post_content);
        mWallPostImages = (SimpleDraweeView) view.findViewById(R.id.wall_post_images);
        mWallPostCommentersLayout = (LinearLayout) view.findViewById(R.id.wall_post_commenters);
        mCommentersCount = (TextView) view.findViewById(R.id.wall_commenters_count);
        mWallTags = (TextView) view.findViewById(R.id.wall_tags);

        // stats
        mWallPostViewsView = (TextView) view.findViewById(R.id.wall_post_views);
        mWallPostUpvoteView = (TextView) view.findViewById(R.id.wall_post_upvote);
        mWallPostDateView = (TextView) view.findViewById(R.id.wall_post_date);
    }
}
