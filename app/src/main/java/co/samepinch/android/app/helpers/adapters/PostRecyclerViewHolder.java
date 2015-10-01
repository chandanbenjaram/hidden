package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.PostDetailActivity;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.data.dto.Commenter;
import co.samepinch.android.data.dto.Post;
import co.samepinch.android.data.dto.User;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostRecyclerViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.layout_post_item)
    RelativeLayout mLayoutPostItem;

    @Bind(R.id.avatar_image_vs)
    ViewSwitcher mAvatarImgVS;


    @Bind(R.id.avatar)
    SimpleDraweeView mAvatarView;

    @Bind(R.id.avatar_name)
    TextView mAvatarName;

    @Bind(R.id.wall_post_dot)
    TextView mWallPostDotView;

    @Bind(R.id.wall_pinch_handle)
    TextView mWallPinchHandleView;

    @Bind(R.id.wall_post_images)
    SimpleDraweeView mWallPostImages;

    @Bind(R.id.wall_post_content)
    TextView mWallPostContentView;

    @Bind(R.id.wall_tags)
    TextView mWallTags;

    @Bind(R.id.wall_post_commenters)
    LinearLayout mWallPostCommentersLayout;

    @Bind(R.id.wall_commenters_count)
    TextView mCommentersCount;

    @Bind(R.id.wall_post_views)
    TextView mWallPostViewsView;

    @Bind(R.id.wall_post_upvote)
    TextView mWallPostUpvoteView;

    @Bind(R.id.wall_post_date)
    TextView mWallPostDateView;

    Context mContext;
    Post mPost;

    public PostRecyclerViewHolder(final Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(this, itemView);
        setIsRecyclable(true);
    }

    public void onBindViewHolderImpl(final Cursor cursor) {
//        Long contentId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
//        Uri contentUri = ContentUris.withAppendedId(SchemaPosts.CONTENT_URI, contentId);
//        cursor.setNotificationUri( this.mContext.getContentResolver(), contentUri);

        mPost = Utils.cursorToPostEntity(cursor);

        final User user = mPost.getOwner();
        if (Utils.isValidUri(user.getPhoto())) {
            Utils.setupLoadingImageHolder(mAvatarView, user.getPhoto());
            mAvatarImgVS.setDisplayedChild(0);
        } else {
            String name = StringUtils.join(StringUtils.substring(user.getFname(), 0, 1), StringUtils.substring(user.getLname(), 0, 1));
            mAvatarName.setText(name);
            mAvatarImgVS.setDisplayedChild(1);
        }
        mWallPostDotView.setText(StringUtils.join(new String[]{user.getFname(), user.getLname()}, " "));
        String pinchHandle = String.format(mContext.getString(R.string.pinch_handle), user.getPinchHandle());
        mWallPinchHandleView.setText(pinchHandle);

        mWallPostViewsView.setText(StringUtils.defaultString(Integer.toString(mPost.getViews()), "0"));
        mWallPostUpvoteView.setText(StringUtils.defaultString(Integer.toString(mPost.getUpvoteCount()), "0"));
        mWallPostDateView.setText(Utils.dateToString(mPost.getCreatedAt()));
        mWallPostContentView.setText(mPost.getContent());
        mCommentersCount.setText(String.valueOf(mPost.getCommentCount()));

        // hide needed ones
        int commentViewsCount = mWallPostCommentersLayout.getChildCount();
        for (int i = 0; i < commentViewsCount; i++) {
            View child = mWallPostCommentersLayout.getChildAt(i);
            if (child instanceof SimpleDraweeView) {
                child.setVisibility(View.GONE);
            }
        }

        List<Commenter> commenters = mPost.getCommenters();
        if (commenters != null) {
            int iViewIndex = 0;
            for (Commenter commenter : commenters) {
                if (StringUtils.isBlank(commenter.getPhoto())) {
                    continue;
                }

                View child = mWallPostCommentersLayout.getChildAt(iViewIndex);
                if (child instanceof SimpleDraweeView) {
                    SimpleDraweeView cImageView = (SimpleDraweeView) child;
                    Utils.setupLoadingImageHolder(cImageView, commenter.getPhoto());
                    cImageView.setVisibility(View.VISIBLE);
                }
                iViewIndex += 1;
            }
        }

        if (CollectionUtils.isEmpty(mPost.getImages())) {
            mWallPostImages.setVisibility(View.GONE);
        } else {
            Utils.setupLoadingImageHolder(mWallPostImages, mPost.getImages().get(0));
            mWallPostImages.setVisibility(View.VISIBLE);
        }

        Utils.markTags(mContext, mWallTags, mPost.getTagsForDB().split(","));
    }

    @OnClick({R.id.avatar, R.id.avatar_name, R.id.wall_post_dot, R.id.wall_pinch_handle})
    public void doOpenDotWall() {
        if(mPost.getAnonymous() !=null && mPost.getAnonymous().booleanValue()){
            return;
        }

        Bundle args = new Bundle();
        args.putString(AppConstants.K.KEY_DOT.name(), mPost.getOwner().getUid());
        args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_DOTWALL.name());

        Intent intent = new Intent(mContext, ActivityFragment.class);
        intent.putExtras(args);

        mContext.startActivity(intent);
    }

    @OnClick({R.id.wall_post_content, R.id.wall_post_images})
    public void doOpenPost() {
        Bundle iArgs = new Bundle();
        iArgs.putString(AppConstants.K.POST.name(), mPost.getUid());

        Intent intent = new Intent(mContext, PostDetailActivity.class);
        intent.putExtras(iArgs);

        mContext.startActivity(intent);
    }
}
