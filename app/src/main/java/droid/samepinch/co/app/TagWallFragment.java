/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package droid.samepinch.co.app;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

import droid.samepinch.co.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dao.SchemaTags;

import static droid.samepinch.co.app.helpers.AppConstants.K;

public class TagWallFragment extends Fragment {
    public static final String LOG_TAG = "TagWallFragment";

    AppCompatActivity activity;
    PostCursorRecyclerViewAdapter mViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.tags_wall_view, container, false);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                activity.onBackPressed();
            }
        });
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);

        // tag name
        String tag = getArguments().getString(K.KEY_TAG.name());
        collapsingToolbar.setTitle(tag);

        Cursor cursor = activity.getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{tag}, null);
        Uri imgUri;
        int photoUrlIndex;
        if (cursor.moveToFirst() && (photoUrlIndex = cursor.getColumnIndex(SchemaTags.COLUMN_PHOTO_URL)) != -1) {
            String photoUrlStr = cursor.getString(photoUrlIndex);
             imgUri = Uri.parse(photoUrlStr);
        }else{
            imgUri = Uri.parse("https://posts.samepinch.co/assets/anonymous-9970e78c322d666ccc2aba97a42e4689979b00edf724e0a01715f3145579f200.png");
        }
        // tag map
        SimpleDraweeView backdropImg = (SimpleDraweeView) view.findViewById(R.id.backdrop);
        backdropImg.setImageURI(imgUri);

        // recyclers
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        setupRecyclerView(new String[]{"%" + tag + "%"}, rv);

        return view;
    }

    private void setupRecyclerView(String[] tags, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        Cursor cursor = activity.getContentResolver().query(SchemaPosts.CONTENT_URI, null, "tags LIKE ?", tags, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}