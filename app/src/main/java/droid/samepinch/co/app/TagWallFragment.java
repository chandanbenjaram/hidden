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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import droid.samepinch.co.app.helpers.intent.PostsPullService;
import droid.samepinch.co.data.dao.SchemaPosts;

public class TagWallFragment extends Fragment {
    public static final String LOG_TAG = "TagWallFragment";

    PostListFragmentUpdater postListFragmentUpdater = new PostListFragmentUpdater();
    PostCursorRecyclerViewAdapter mViewAdapter;
    private Intent mServiceIntent;
    private RecyclerView.LayoutManager mLayoutManager;
    FragmentActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (FragmentActivity) activity;
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
        super.onCreate(savedInstanceState);

        SimpleDraweeView backdropImg = (SimpleDraweeView) activity.findViewById(R.id.backdrop);
        Uri imgUri = Uri.parse("https://posts.samepinch.co/assets/anonymous-9970e78c322d666ccc2aba97a42e4689979b00edf724e0a01715f3145579f200.png");
        backdropImg.setImageURI(imgUri);

//        TextView txtView = (TextView) activity.findViewById(R.id.text_view_id);
//        txtView.setText(imgUri + "");

        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.posts_recycler_view, container, false);
        mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        setupRecyclerView(rv);


        //s The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                AppConstants.APP_INTENT.BROADCAST_ACTION.getValue());
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        // Registers the PostListFragmentUpdater and its intent filters
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                postListFragmentUpdater,
                statusIntentFilter);
        mServiceIntent =
                new Intent(getActivity(), PostsPullService.class);
        activity.startService(mServiceIntent);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        Cursor cursor = activity.getContentResolver().query(SchemaPosts.CONTENT_URI, null, null, null, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    private class PostListFragmentUpdater extends BroadcastReceiver {
        private PostListFragmentUpdater() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
