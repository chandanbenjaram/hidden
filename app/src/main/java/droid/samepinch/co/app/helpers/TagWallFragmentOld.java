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

package droid.samepinch.co.app.helpers;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.LinearLayout;

import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import droid.samepinch.co.data.dao.SchemaPosts;

public class TagWallFragmentOld extends Fragment {
    public static final String LOG_TAG = "TagWallFragment";

    PostCursorRecyclerViewAdapter mViewAdapter;
    private Intent mServiceIntent;
    private RecyclerView.LayoutManager mLayoutManager;
    AppCompatActivity activity;


    LinearLayout linearLayout;
    View rootView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tags_wall_view, container, false);

        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("TagWallFragment");

//        SimpleDraweeView backdropImg = (SimpleDraweeView) activity.findViewById(R.id.backdrop);
//        Uri imgUri = Uri.parse("https://posts.samepinch.co/assets/anonymous-9970e78c322d666ccc2aba97a42e4689979b00edf724e0a01715f3145579f200.png");
//        backdropImg.setImageURI(imgUri);


        RecyclerView rv = (RecyclerView) rootView.findViewById(
                R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        setupRecyclerView(rv);
        return rootView;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        Cursor cursor = activity.getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
