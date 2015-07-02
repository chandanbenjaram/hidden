package droid.samepinch.co.app.helpers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import droid.samepinch.co.app.Cheeses;
import droid.samepinch.co.app.R;
import droid.samepinch.co.data.DB;
import droid.samepinch.co.data.dto.Post;

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
        Post post = DB.mPostDAO.cursorToEntity(cursor);
        holder.mBoundString = String.valueOf(post.getUid());

        holder.mTextView.setText(post.getContent());
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Context context = v.getContext();
//                Intent intent = new Intent(context, PostDetailActivity.class);
//
//                intent.putExtra(PostDetailActivity.EXTRA_NAME, holder.mBoundString);
//                context.startActivity(intent);
//            }
//        });

        Glide.with(holder.mImageView.getContext())
                .load(Cheeses.getRandomCheeseDrawable())
                .fitCenter()
                .into(holder.mImageView);
    }

}
