package co.samepinch.android.app.helpers.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.PostDetailActivity;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.data.dto.Commenter;
import co.samepinch.android.data.dto.Post;
import co.samepinch.android.data.dto.User;

import static co.samepinch.android.app.helpers.AppConstants.K;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<PostRecyclerViewHolder> {
    private final Context context;

    public PostCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
    }


    @Override
    public PostRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.list_item, parent, false);

        // setup view
        PostRecyclerViewHolder vh = new PostRecyclerViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final PostRecyclerViewHolder vh, Cursor cursor) {
        final Post post = Utils.cursorToPostEntity(cursor);

        final User user = post.getOwner();
        View.OnClickListener dotClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TARGET
                Bundle args = new Bundle();
                args.putString(K.TARGET_FRAGMENT.name(), K.FRAGMENT_DOTWALL.name());
                // data
                args.putString(K.KEY_DOT.name(), user.getUid());

                // intent
                Intent intent = new Intent(context, ActivityFragment.class);
                intent.putExtras(args);

                context.startActivity(intent);
            }
        };
        if (TextUtils.isEmpty(user.getPhoto())) {
            vh.mAvatarName.setVisibility(View.VISIBLE);
            vh.mAvatarView.setVisibility(View.INVISIBLE);

            String name = StringUtils.join(StringUtils.substring(user.getFname(), 0, 1), StringUtils.substring(user.getLname(), 0, 1));
            vh.mAvatarName.setText(name);
            vh.mAvatarName.setOnClickListener(dotClick);
        } else {
            vh.mAvatarView.setVisibility(View.VISIBLE);
            vh.mAvatarName.setVisibility(View.GONE);

            // set image
            Utils.setupLoadingImageHolder(vh.mAvatarView, user.getPhoto());
            vh.mAvatarView.setOnClickListener(dotClick);
        }
        vh.mWallPostDotView.setOnClickListener(dotClick);

        vh.mWallPostDotView.setText(StringUtils.join(new String[]{user.getFname(), user.getLname()}, " "));
        String pinchHandle = String.format(context.getString(R.string.pinch_handle), user.getPinchHandle());
        vh.mWallPinchHandleView.setText(pinchHandle);

        vh.mWallPostViewsView.setText(StringUtils.defaultString(Integer.toString(post.getViews()), "0"));
        vh.mWallPostUpvoteView.setText(StringUtils.defaultString(Integer.toString( post.getUpvoteCount()), "0"));
        vh.mWallPostDateView.setText(Utils.dateToString(post.getCreatedAt()));

        vh.mWallPostContentView.setText(post.getContent());
        vh.mWallPostContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle iArgs = new Bundle();
                iArgs.putString(K.POST.name(), post.getUid());

                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtras(iArgs);

                context.startActivity(intent);
            }
        });

        vh.mCommentersCount.setText(String.valueOf(post.getCommentCount()));

        // hide needed ones
        int commentViewsCount = vh.mWallPostCommentersLayout.getChildCount();
        for (int i = 0; i < commentViewsCount; i++) {
            View child = vh.mWallPostCommentersLayout.getChildAt(i);
            if (child instanceof SimpleDraweeView) {
                child.setVisibility(View.GONE);
            }
        }

        List<Commenter> commenters = post.getCommenters();
        if (commenters != null) {
            int iViewIndex = 0;
            for (Commenter commenter : commenters) {
                if (StringUtils.isBlank(commenter.getPhoto())) {
                    continue;
                }

                View child = vh.mWallPostCommentersLayout.getChildAt(iViewIndex);
                if (child instanceof SimpleDraweeView) {
                    SimpleDraweeView cImageView = (SimpleDraweeView) child;
                    Utils.setupLoadingImageHolder(cImageView, commenter.getPhoto());
                    cImageView.setVisibility(View.VISIBLE);
                }
                iViewIndex += 1;
            }
        }

        if (CollectionUtils.isEmpty(post.getImages())) {
            vh.mWallPostImages.setVisibility(View.GONE);
        } else {
            Utils.setupLoadingImageHolder(vh.mWallPostImages, post.getImages().get(0));
            vh.mWallPostImages.setVisibility(View.VISIBLE);
        }

        Utils.markTags(getContext(), vh.mWallTags, post.getTagsForDB().split(","));
    }


}