package co.samepinch.android.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.WindowManager;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.data.dao.SchemaComments;
import co.samepinch.android.data.dto.CommentDetails;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespCommentDetails;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.COMMENTS;

public class CommentFragment extends Fragment implements android.support.v7.widget.PopupMenu.OnMenuItemClickListener {
    public static final String TAG = "CommentFragment";

    ProgressDialog progressDialog;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.comment_text_id)
    EditText commentTxt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.comment_add_fragment, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);

        toolbar.setTitle(StringUtils.EMPTY);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });

        // get args
        String commentUid = getArguments().getString(AppConstants.K.COMMENT.name(), StringUtils.EMPTY);
        if (StringUtils.isNotBlank(commentUid)) {
            Cursor cursor = getActivity().getContentResolver().query(SchemaComments.CONTENT_URI, null, SchemaComments.COLUMN_UID + "=?", new String[]{commentUid}, null);
            if (!cursor.moveToFirst()) {
                getActivity().finish();
            }
            final CommentDetails commentDetails = Utils.cursorToCommentDetails(cursor);
            commentTxt.setText(commentDetails.getText());
        }

        commentTxt.requestFocus();
        return view;
    }

    @OnFocusChange(R.id.comment_text_id)
    public void onCommentFocusChange(boolean focussed) {
        if (focussed) {
            Utils.showKeyboard(getActivity());
        } else {
            Utils.hideKeyboard(getActivity());
        }
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

        if (getArguments().containsKey(AppConstants.K.COMMENT.name())) {
            String commentUid = getArguments().getString(AppConstants.K.COMMENT.name());
            new CommentUpdateTask().execute(new String[]{commentUid, commentTxt.getText().toString(), Boolean.toString(asAnonymous)});

        } else {
            String postId = getArguments().getString(AppConstants.K.POST.name());
            new CommentCreateTask().execute(new String[]{postId, commentTxt.getText().toString(), Boolean.toString(asAnonymous)});
        }

    }


    private void doLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showCommentAsMenu() {
        if (!Utils.isLoggedIn()) {
            doLogin();
            return;
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

    private class CommentCreateTask extends AsyncTask<String, Integer, Boolean> {
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
                headers.setAccept(RestClient.INSTANCE.jsonMediaType());

                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.COMMENTS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);
                return resp.getBody().getStatus() == 200;
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                Log.e(TAG, resp == null ? "null" : resp.getMessage(), e);
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Utils.dismissSilently(progressDialog);
            if (result != null && result.booleanValue()) {
                // wall refresh needs to happen
                Utils.PreferencesManager.getInstance().setValue(AppConstants.APP_INTENT.KEY_FRESH_WALL_FLAG.getValue(), Boolean.TRUE.toString());

                Intent resultIntent = new Intent();
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            } else {
                Snackbar.make(getView(), "error commenting. try again...", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class CommentUpdateTask extends AsyncTask<String, Integer, Boolean> {
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
                req.setCmd("update");

                String commentUID = args[0];
                Map<String, String> body = new HashMap<>();
                body.put("text", args[1]);
                body.put("anonymous", args[2]);
                req.setBody(body);

                //headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(RestClient.INSTANCE.jsonMediaType());
                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);

                String commentUri = StringUtils.join(new String[]{COMMENTS.getValue(), commentUID}, "/");
                ResponseEntity<RespCommentDetails> resp = RestClient.INSTANCE.handle().exchange(commentUri, HttpMethod.POST, payloadEntity, RespCommentDetails.class);
                CommentDetails commentDetails;
                if (resp.getBody() != null && (commentDetails = resp.getBody().getBody()) != null) {
                    ContentValues values = new ContentValues();
                    values.put(SchemaComments.COLUMN_CREATED_AT, commentDetails.getCreatedAt().getTime());
                    values.put(SchemaComments.COLUMN_ANONYMOUS, commentDetails.getAnonymous());
                    values.put(SchemaComments.COLUMN_TEXT, commentDetails.getText());
                    values.put(SchemaComments.COLUMN_UPVOTE_COUNT, commentDetails.getUpvoteCount());
                    values.put(SchemaComments.COLUMN_UPVOTED, commentDetails.getUpvoted());
                    values.put(SchemaComments.COLUMN_PERMISSIONS, commentDetails.getPermissionsForDB());

                    int dbResult = getActivity().getContentResolver().update(SchemaComments.CONTENT_URI, values, SchemaComments.COLUMN_UID + "=?", new String[]{commentUID});
                    if (dbResult > 0) {
                        return Boolean.TRUE;
                    }
                }
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                Log.e(TAG, resp == null ? "null" : resp.getMessage(), e);
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Utils.dismissSilently(progressDialog);
            if (result != null && result.booleanValue()) {
                Intent resultIntent = new Intent();
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            } else {
                Snackbar.make(getView(), "error updating comment. try again...", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
