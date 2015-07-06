package droid.samepinch.co.app.helpers.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import droid.samepinch.co.app.PostDetailActivity;
import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.data.dto.Post;
import droid.samepinch.co.data.dto.User;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<PostRecyclerViewHolder> {
    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;

    public PostCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);

        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
    }


    @Override
    public PostRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new PostRecyclerViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final PostRecyclerViewHolder holder, Cursor cursor) {
        Post post = Utils.cursorToPostEntity(cursor);
        holder.mBoundString = String.valueOf(post.getUid());
        holder.mTextView.setText(post.getContent());

        User user = post.getOwner();
        Uri userPhotoUri;
        if (user == null || TextUtils.isEmpty(user.getPhoto())) {
            userPhotoUri = Uri.parse("http://posts.samepinch.co/assets/anonymous-9970e78c322d666ccc2aba97a42e4689979b00edf724e0a01715f3145579f200.png");
        } else {
            userPhotoUri = Uri.parse(user.getPhoto());
        }
        holder.mImageView.setImageURI(userPhotoUri);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, PostDetailActivity.class);

                intent.putExtra(PostDetailActivity.EXTRA_NAME, holder.mBoundString);
                context.startActivity(intent);
            }
        });

//        Glide.with(holder.mImageView.getContext())
//                .load(Cheeses.getRandomCheeseDrawable())
//                .fitCenter()
//                .into(holder.mImageView);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }

}
