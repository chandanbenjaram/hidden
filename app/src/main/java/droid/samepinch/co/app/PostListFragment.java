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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import droid.samepinch.co.app.helpers.ext.AppCursorRecyclerViewAdapter;
import droid.samepinch.co.app.helpers.intent.PostsPullService;
import droid.samepinch.co.data.WallContract;
import droid.samepinch.co.data.WallDbHelper;

public class PostListFragment extends Fragment {
    public static final String LOG_TAG = PostListFragment.class.getSimpleName();
    // Intent for starting the IntentService that downloads the Picasa featured picture RSS feed
    private Intent mServiceIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_cheese_list, container, false);
        setupRecyclerView(rv);

        Uri postsUri = WallContract.Posts.CONTENT_URI;

        ContentValues testValues = new ContentValues();
        testValues.put(WallContract.Posts.COLUMN_POST_ID, Math.random());
        testValues.put(WallContract.Posts.COLUMN_CONTENT, "CB " + Math.random());

        WallDbHelper dbHelper = new WallDbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(WallContract.Posts.TABLE_NAME, null, testValues);

        Log.i(LOG_TAG, "creating posts pull intent...");
        mServiceIntent =
                new Intent(getActivity(), PostsPullService.class);
        Log.i(LOG_TAG, "starting service intent...");
        getActivity().startService(mServiceIntent);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        Uri postsUri = WallContract.Posts.CONTENT_URI;
        Cursor cursor = getActivity().getContentResolver().query(postsUri, null, null, null, null);

        recyclerView.setAdapter(new AppCursorRecyclerViewAdapter(getActivity(), cursor));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

}
