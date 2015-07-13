package droid.samepinch.co.app.helpers.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
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

import droid.samepinch.co.app.ActivityFragment;
import droid.samepinch.co.app.PostDetailActivity;
import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.Utils;
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
        Post post = Utils.cursorToPostEntity(cursor);
        vh.mBoundString = String.valueOf(post.getUid());
//        vh.mTextView.setText(post.getContent());

        User user = post.getOwner();
        Uri userPhotoUri;
        if (user == null || TextUtils.isEmpty(user.getPhoto())) {
            userPhotoUri = Uri.parse("http://posts.samepinch.co/assets/anonymous-9970e78c322d666ccc2aba97a42e4689979b00edf724e0a01715f3145579f200.png");
        } else {
            userPhotoUri = Uri.parse(user.getPhoto());
        }
        vh.mAvatarView.setImageURI(userPhotoUri);
        vh.mWallPostDotView.setText(post.getOwner() == null || post.getOwner().getFname() == null ? "dummy" : post.getOwner().getFname());
        vh.mWallPostContentView.setText(post.getContent());
        vh.mWallPostViewsView.setText(post.getViews() == null ? "0" : post.getViews() + "");

        vh.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);

                intent.putExtra(PostDetailActivity.EXTRA_NAME, vh.mBoundString);
                context.startActivity(intent);
            }
        });

        if (post.getCommentersForDB() != null) {
            String[] commenterImageArr = post.getCommentersForDB().split(",");
            int chidViewsCnt = vh.mWallPostCommentersLayout.getChildCount();
            for (int i = 0; i < chidViewsCnt && i < commenterImageArr.length; i++) {
                View child = vh.mWallPostCommentersLayout.getChildAt(i);
                if (child instanceof SimpleDraweeView) {
                    SimpleDraweeView cImageView = (SimpleDraweeView) child;
                    Uri commenterImageUri = Uri.parse(commenterImageArr[i]);
                    cImageView.setImageURI(commenterImageUri);
                    cImageView.setVisibility(View.VISIBLE);
                }
            }
            vh.mCommentersCount.setText(commenterImageArr.length + "");
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