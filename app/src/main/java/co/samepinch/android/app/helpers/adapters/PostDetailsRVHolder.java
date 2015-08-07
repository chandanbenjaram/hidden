package co.samepinch.android.app.helpers.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostDetailsRVHolder extends RecyclerView.ViewHolder {
    View mView;

    public PostDetailsRVHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    void onBindViewHolderImpl(Cursor cursor){
        // do nothing in base
        return;
    }
}
