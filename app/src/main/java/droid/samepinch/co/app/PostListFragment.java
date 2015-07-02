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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.app.helpers.adapters.PostListRecyclerViewAdapter;
import droid.samepinch.co.app.helpers.intent.PostsPullService;
import droid.samepinch.co.data.DB;
import droid.samepinch.co.data.dto.Post;

public class PostListFragment extends Fragment {
    public static final String LOG_TAG = PostListFragment.class.getSimpleName();
    PostListFragmentUpdater postListFragmentUpdater = new PostListFragmentUpdater();
    // Intent for starting the IntentService that downloads the Picasa featured picture RSS feed
    private Intent mServiceIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                AppConstants.APP_INTENT.BROADCAST_ACTION.getValue());
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        // Registers the PostListFragmentUpdater and its intent filters
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                postListFragmentUpdater,
                statusIntentFilter);

        Log.i(LOG_TAG, "creating posts pull intent...");
        mServiceIntent =
                new Intent(getActivity(), PostsPullService.class);
        Log.i(LOG_TAG, "starting service intent...");
        getActivity().startService(mServiceIntent);

        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_cheese_list, container, false);
        setupRecyclerView(rv);

        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        List<Post> posts = DB.mPostDAO.fetchAllPosts();


        recyclerView.setAdapter(new PostListRecyclerViewAdapter(getActivity(), posts));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    private class PostListFragmentUpdater extends BroadcastReceiver {
        private PostListFragmentUpdater() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentName = intent.getStringExtra(AppConstants.APP_INTENT.EXTENDED_DATA_STATUS.getValue());
            AppConstants.APP_INTENT intentNameConst = AppConstants.APP_INTENT.valueOf(intentName);
            Snackbar.make(getActivity().findViewById(R.id.fab), intentNameConst.getValue(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

}
