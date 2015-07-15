package droid.samepinch.co.app.helpers.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

import droid.samepinch.co.app.ActivityFragment;
import droid.samepinch.co.app.PostDetailActivity;
import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.data.dto.Commenter;
import droid.samepinch.co.data.dto.Post;
import droid.samepinch.co.data.dto.User;

import static droid.samepinch.co.app.helpers.AppConstants.K;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<PostRecyclerViewHolder> {
    //    private final TypedValue mTypedValue = new TypedValue();
    private final Context context;

    public PostCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;

//        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
//        mBackground = mTypedValue.resourceId;
    }


    @Override
    public PostRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.list_item, parent, false);
//        v.setBackgroundResource(mBackground);

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
            if (TextUtils.isEmpty(user.getFname()) || user.getFname().length() < 1
                    || TextUtils.isEmpty(user.getLname()) || user.getLname().length() < 1) {
                vh.mAvatarName.setText("");
            } else {
                vh.mAvatarName.setText(user.getFname().substring(0, 1) + user.getLname().substring(0, 1));
            }
        } else {
//            vh.mAvatarName.setText("");
            vh.mAvatarView.setVisibility(View.VISIBLE);
            vh.mAvatarName.setVisibility(View.GONE);

            // set image
            Utils.setupLoadingImageHolder(vh.mAvatarView, user.getPhoto());

        }

        vh.mWallPostDotView.setOnClickListener(dotClick);
        vh.mAvatarName.setOnClickListener(dotClick);
        vh.mAvatarView.setOnClickListener(dotClick);

        vh.mWallPostDotView.setText(StringUtils.join(new String[]{user.getFname(), user.getLname()}, " "));
        vh.mWallPinchHandleView.setText("@" + user.getPinchHandle());

        vh.mWallPostContentView.setText(post.getContent());
        vh.mWallPostViewsView.setText(String.valueOf(post.getViews() == null ? 0 : post.getViews()));

        vh.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_NAME, post.getUid());
                context.startActivity(intent);
            }
        });

        vh.mCommentersCount.setText(String.valueOf(post.getCommentCount()));

        // hide needed ones
        int commentViewsCount= vh.mWallPostCommentersLayout.getChildCount();
        for(int i=0; i< commentViewsCount; i++){
            View child = vh.mWallPostCommentersLayout.getChildAt(i);
            if (child instanceof SimpleDraweeView) {
                child.setVisibility(View.GONE);
            }
        }

        List<Commenter> commenters = post.getCommenters();
        if (commenters != null) {
            int iViewIndex = 0;
            for(Commenter commenter: commenters){
                if(StringUtils.isBlank(commenter.getPhoto())) {
                    continue;
                }

                View child = vh.mWallPostCommentersLayout.getChildAt(iViewIndex);
                if (child instanceof SimpleDraweeView) {
                    SimpleDraweeView cImageView = (SimpleDraweeView) child;
                    Utils.setupLoadingImageHolder(cImageView, commenter.getPhoto());
                    cImageView.setVisibility(View.VISIBLE);
                }
                iViewIndex +=1;
            }
        }


        if(CollectionUtils.isEmpty(post.getImages())){
            vh.mWallPostImages.setVisibility(View.GONE);
        }else{
            Utils.setupLoadingImageHolder(vh.mWallPostImages, post.getImages().get(0));
            vh.mWallPostImages.setVisibility(View.VISIBLE);
        }

        customTextView(vh.mWallTags, post.getTagsForDB().split(","));
    }

    private void customTextView(TextView view, String[] tags) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder();

        for (final String tag : tags) {
            spanTxt.append(tag);
            spanTxt.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // TARGET
                    Bundle args = new Bundle();
                    args.putString(K.TARGET_FRAGMENT.name(), K.FRAGMENT_TAGWALL.name());
                    // data
                    args.putString(K.KEY_TAG.name(), tag);

                    // intent
                    Intent intent = new Intent(context, ActivityFragment.class);
                    intent.putExtras(args);

                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(Color.BLUE);
                }
            }, spanTxt.length() - tag.length(), spanTxt.length(), 0);
            spanTxt.append(" ");
        }

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }

    @Override
    public void onBindViewHolder(PostRecyclerViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
    }
}