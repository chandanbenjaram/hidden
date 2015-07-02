package droid.samepinch.co.app.helpers.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import droid.samepinch.co.app.R;
import droid.samepinch.co.data.dto.Post;

/**
 * Created by imaginationcoder on 7/2/15.
 */
public class PostListRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewHolder> {
    private final TypedValue mTypedValue = new TypedValue();
    private List<Post> mData;
    private int mBackground;

    public PostListRecyclerViewAdapter(Context context, List<Post> mData) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);

        this.mData = mData;
        mBackground = mTypedValue.resourceId;
    }

    @Override
    public PostRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        view.setBackgroundResource(mBackground);

        // create ViewHolder
        PostRecyclerViewHolder viewHolder = new PostRecyclerViewHolder(view);
        return new PostRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostRecyclerViewHolder viewHolder, int position) {
        viewHolder.mTextView.setText(mData
                .get(position)
                .getContent());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
