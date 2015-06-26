package droid.samepinch.co.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import droid.samepinch.co.app.R;
import droid.samepinch.co.data.WallContract;
import droid.samepinch.co.data.WallDbHelper;

/**
 * Created by imaginationcoder on 6/25/15.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    public String mBoundString;

    public final View mView;
    public final ImageView mImageView;
    public final TextView mTextView;


    public PostViewHolder(View view) {
        super(view);
        mView = view;
        mImageView = (ImageView) view.findViewById(R.id.avatar);
        mTextView = (TextView) view.findViewById(android.R.id.text1);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mTextView.getText();
    }
}