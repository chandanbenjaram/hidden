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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import co.samepinch.android.app.helpers.AppConstants;

public class CommentFragment extends Fragment implements android.support.v7.widget.PopupMenu.OnMenuItemClickListener {
    public static final String LOG_TAG = "CommentFragment";

    private Intent mServiceIntent;
    AppCompatActivity activity;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.comment_text_id)
    TextView commentTxt;

    private String mPostId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.comment_add_fragment, container, false);
        ButterKnife.bind(this, view);

        // retrieve post from args
        this.mPostId = getArguments().getString(AppConstants.K.POST.name());

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                activity.onBackPressed();
            }
        });
        toolbar.setTitle(StringUtils.EMPTY);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnEditorAction(R.id.comment_text_id)
    public boolean dummy() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_comment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                activity.onBackPressed();
                return true;

            case R.id.menuitem_login:
                doLogin(item);
                return true;

            case R.id.menuitem_comment:
                doComment(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doLogin(MenuItem item) {
        //TODO login implementation
        Intent intent = new Intent(activity.getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.getApplicationContext().startActivity(intent);
    }

    private void doComment(MenuItem item) {
        View menuitemCommentView = getView().findViewById(R.id.menuitem_comment);
        PopupMenu popup = new PopupMenu(activity, menuitemCommentView);

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.add_comment_selection_menu, popup.getMenu());
//        MenuItem commentUser = popup.getMenu().findItem(R.id.menuitem_comment_as_user);
//        commentUser.setTitle("as " + System.currentTimeMillis());
//        popup.getMenu().add("as you");
//        popup.getMenu().add("as anonymous");
//
//        commentUser.setIcon(R.drawable.common_signin_btn_icon_disabled_dark);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_comment_as_anonymous:
                doCommentAsAnonym(item);
//                activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
//                activity.onBackPressed();
                return true;

            case R.id.menuitem_comment_as_user:
                doCommentAsUser(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doCommentAsAnonym(MenuItem item) {
        Bundle iArgs = new Bundle();
        iArgs.putString(AppConstants.K.POST.name(), mPostId);
        Intent intent = new Intent(activity.getApplicationContext(), PostDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(iArgs);

        activity.getApplicationContext().startActivity(intent);
    }

    private void doCommentAsUser(MenuItem item) {
    }
}
