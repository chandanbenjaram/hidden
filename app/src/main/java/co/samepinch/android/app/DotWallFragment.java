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

package co.samepinch.android.app;

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
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dao.SchemaTags;

import static co.samepinch.android.app.helpers.AppConstants.K;

public class DotWallFragment extends Fragment {
    public static final String LOG_TAG = "DotWallFragment";

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

        // dot uid
        String dotUid = getArguments().getString(K.KEY_DOT.name());
        collapsingToolbar.setTitle(dotUid);

        Cursor cursor = activity.getContentResolver().query(SchemaDots.CONTENT_URI, null, SchemaDots.COLUMN_UID + "=?", new String[]{dotUid}, null);
        int photoUrlIndex;
        if (cursor.moveToFirst() && (photoUrlIndex = cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE)) != -1) {
            String photoUrlStr = cursor.getString(photoUrlIndex);
            if(photoUrlStr != null){
                Uri imgUri = Uri.parse(photoUrlStr);
                // tag map
                SimpleDraweeView backdropImg = (SimpleDraweeView) view.findViewById(R.id.backdrop);
                backdropImg.setImageURI(imgUri);
            }

        }

        // recyclers
        // custom recycler
        RecyclerView rv = new RecyclerView(activity.getApplicationContext()){
            @Override
            public void scrollBy(int x, int y) {
                try{
                    super.scrollBy(x, y);
                }catch (NullPointerException nlp){
                    // muted
                }
            }
        };

        LinearLayout rvHolder = (LinearLayout) view.findViewById(R.id.holder_recyclerview);
        rvHolder.addView(rv);

        mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        setupRecyclerView(new String[]{dotUid}, rv);

        return view;
    }

    private void setupRecyclerView(String[] args0, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        Cursor cursor = activity.getContentResolver().query(SchemaPosts.CONTENT_URI, null, SchemaPosts.COLUMN_OWNER + "=?", args0, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}