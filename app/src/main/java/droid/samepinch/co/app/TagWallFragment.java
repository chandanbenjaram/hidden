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
import android.content.Intent;
import android.database.Cursor;
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
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import droid.samepinch.co.app.helpers.intent.TagsPullService;
import droid.samepinch.co.app.helpers.pubsubs.BusProvider;
import droid.samepinch.co.app.helpers.pubsubs.Events;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dao.SchemaTags;

import static droid.samepinch.co.app.helpers.AppConstants.APP_INTENT.KEY_NAME;
import static droid.samepinch.co.app.helpers.AppConstants.K;

public class TagWallFragment extends Fragment {
    public static final String LOG_TAG = "TagWallFragment";

    private Intent mServiceIntent;

    private AppCompatActivity activity;
    private PostCursorRecyclerViewAdapter mViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View mView;
    private String mTag;
    private SimpleDraweeView mBackdropImg;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        // register to event bus
        BusProvider.INSTANCE.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = inflater.inflate(R.layout.tags_wall_view, container, false);

        final Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
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
                (CollapsingToolbarLayout) mView.findViewById(R.id.collapsing_toolbar);

        // tag name
        mTag = getArguments().getString(K.KEY_TAG.name());
        collapsingToolbar.setTitle(mTag);

        mBackdropImg = (SimpleDraweeView) mView.findViewById(R.id.backdrop);
        Cursor cursor = activity.getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{mTag}, null);
        if (cursor.moveToFirst()) {
            int imgIdx = cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE);
            String imgStr = imgIdx > -1 ? cursor.getString(imgIdx) : null;
            if (StringUtils.isNotBlank(imgStr)) {
                Utils.setupLoadingImageHolder(mBackdropImg, imgStr);
            }
        }
        cursor.close();

        // recyclers
        // custom recycler
        RecyclerView rv = new RecyclerView(activity.getApplicationContext()) {
            @Override
            public void scrollBy(int x, int y) {
                try {
                    super.scrollBy(x, y);
                } catch (NullPointerException nlp) {
                    // muted
                }
            }
        };

        LinearLayout rvHolder = (LinearLayout) mView.findViewById(R.id.holder_recyclerview);
        rvHolder.addView(rv);
        mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        setupRecyclerView(new String[]{"%" + mTag + "%"}, rv);

        callForRemoteTagData(mTag);
        return mView;
    }


    private void setupRecyclerView(String[] tags, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        Cursor cursor = activity.getContentResolver().query(SchemaPosts.CONTENT_URI, null, "tags LIKE ?", tags, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void callForRemoteTagData(String tag) {
        // construct context from preferences if any?
        Bundle iArgs = new Bundle();
        iArgs.putString(KEY_NAME.getValue(), tag);

        // call for intent
        mServiceIntent =
                new Intent(activity.getApplicationContext(), TagsPullService.class);
        mServiceIntent.putExtras(iArgs);
        activity.startService(mServiceIntent);
    }

    @Subscribe
    public void onTagRefreshedEvent(Events.TagRefreshedEvent event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update preferences metadata
                Cursor cursor = activity.getContentResolver().query(SchemaTags.CONTENT_URI, null, SchemaTags.COLUMN_NAME + "=?", new String[]{mTag}, null);
                try {
                    if (cursor.moveToFirst()) {
                        int imgIdx = cursor.getColumnIndex(SchemaTags.COLUMN_IMAGE);
                        String imgStr = imgIdx > -1 ? cursor.getString(imgIdx) : null;
                        if (StringUtils.isNotBlank(imgStr)) {
                            Utils.setupLoadingImageHolder(mBackdropImg, imgStr);
                        }
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }finally{
                    cursor.close();
                }
            }
        });
    }
}