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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

public class CommentFragment extends Fragment implements android.support.v7.widget.PopupMenu.OnMenuItemClickListener {
    public static final String TAG = "CommentFragment";

    ProgressDialog progressDialog;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.comment_text_id)
    EditText commentTxt;

    private String mPostId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.comment_add_fragment, container, false);
        ButterKnife.bind(this, view);

        // retrieve post from args
        this.mPostId = getArguments().getString(AppConstants.K.POST.name());

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });
        toolbar.setTitle(StringUtils.EMPTY);
        return view;
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
                ((AppCompatActivity) getActivity()).onBackPressed();
                return true;

            case R.id.menuitem_comment:
                showCommentAsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_comment_as_anonymous:
                doComment(true);
                return true;

            case R.id.menuitem_comment_as_user:
                doComment(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doComment(boolean asAnonymous) {
        if (StringUtils.isBlank(commentTxt.getText())) {
            String errMsg = getResources().getString(R.string.enter_comment);
            commentTxt.setError(errMsg);
            return;
        } else {
            commentTxt.setError(null);
        }
        progressDialog.setMessage("posting comment...");
        new CommentTask().execute(new String[]{mPostId, commentTxt.getText().toString(), Boolean.toString(asAnonymous)});
    }


    private void doLogin() {
        //TODO login implementation
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showCommentAsMenu() {
        if (!Utils.isLoggedIn()) {
            doLogin();
        }

        View commentItem = getActivity().findViewById(R.id.menuitem_comment);
        PopupMenu popup = new PopupMenu(getActivity(), commentItem);
        popup.inflate(R.menu.add_comment_selection_menu);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private class CommentTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... args) {
            if (args == null || args.length < 3) {
                return Boolean.FALSE;
            }

            try {
                StorageComponent component = DaggerStorageComponent.create();
                ReqSetBody req = component.provideReqSetBody();
                // set base args
                req.setToken(Utils.getNonBlankAppToken());
                req.setCmd("create");

                Map<String, String> body = new HashMap<>();
                body.put("post_id", args[0]);
                body.put("text", args[1]);
                body.put("anonymous", args[2]);
                req.setBody(body);

                //headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.COMMENTS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);
                return resp.getBody().getStatus() == 200;
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                Log.e(TAG, resp == null ? "" : resp.getMessage(), e);
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Utils.dismissSilently(progressDialog);
            if (result != null && result.booleanValue()) {
                Snackbar.make(getView(), "commented successfully.", Snackbar.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            } else {
                Snackbar.make(getView(), "error commenting. try again...", Snackbar.LENGTH_SHORT).show();
            }
        }

    }
}
