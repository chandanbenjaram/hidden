package droid.samepinch.co.app.helpers.ext;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import droid.samepinch.co.app.PostDetailActivity;
import droid.samepinch.co.app.Cheeses;
import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.PostViewHolder;
import droid.samepinch.co.data.dto.PostItem;

/**
 * Created by imaginationcoder on 6/25/15.
 */
public class AppCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<PostViewHolder> {
    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;

    public AppCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);

        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
    }


    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new PostViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final PostViewHolder holder, Cursor cursor) {
        PostItem postItem = PostItem.fromCursor(cursor);
        holder.mBoundString = String.valueOf(postItem.getId());

        holder.mTextView.setText(postItem.getContent());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, PostDetailActivity.class);

                intent.putExtra(PostDetailActivity.EXTRA_NAME, holder.mBoundString);
                context.startActivity(intent);
            }
        });

        Glide.with(holder.mImageView.getContext())
                .load(Cheeses.getRandomCheeseDrawable())
                .fitCenter()
                .into(holder.mImageView);
    }
}
