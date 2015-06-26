package droid.samepinch.co.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import droid.samepinch.co.app.PostDetailActivity;
import droid.samepinch.co.app.Cheeses;
import droid.samepinch.co.app.R;
import droid.samepinch.co.data.WallContract;
import droid.samepinch.co.data.WallDbHelper;

/**
 * Created by imaginationcoder on 6/25/15.
 */
public class SimpleStringRecyclerViewAdapter
        extends RecyclerView.Adapter<PostViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private List<String> mValues;

    Cursor mCursor;

    public String getValueAt(int position) {
        return mValues.get(position);
    }

    public SimpleStringRecyclerViewAdapter(Context context, List<String> items) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mValues = items;

        Uri postsUri = WallContract.Posts.CONTENT_URI;

        ContentValues testValues = new ContentValues();
        testValues.put(WallContract.Posts.COLUMN_POST_ID, Math.random());
        testValues.put(WallContract.Posts.COLUMN_CONTENT, "CB " + Math.random());

        WallDbHelper dbHelper = new WallDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(WallContract.Posts.TABLE_NAME, null, testValues);

        mCursor = context.getContentResolver().query(postsUri, null, null, null, null);
    }


    // called only when we need to create view
    // inflates view
    // inflate view holder
    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new PostViewHolder(view);
    }

    // bind data to view
    @Override
    public void onBindViewHolder(final PostViewHolder holder, int position) {
        holder.mBoundString = mValues.get(position);
            holder.mTextView.setText(mValues.get(position));
        int contentIdx = mCursor.getColumnIndex(WallContract.Posts.COLUMN_CONTENT);
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move mCursor to position " + position);
        } else {
            holder.mTextView.setText("dummy...");
        }


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

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
