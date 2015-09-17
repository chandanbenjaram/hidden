package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaComments;
import co.samepinch.android.data.dto.CommentDetails;
import co.samepinch.android.data.dto.Commenter;
import co.samepinch.android.data.dto.PostDetails;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespCommentDetails;
import co.samepinch.android.rest.RestBase;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.COMMENTS;

public class CommentUpdateService extends IntentService {
    public static final String TAG = "CommentUpdateService";

    public CommentUpdateService() {
        super("CommentUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get caller data
        Bundle iArgs = intent.getExtras();
        String commentUri = StringUtils.join(new String[]{COMMENTS.getValue(), iArgs.getString(AppConstants.K.COMMENT.name())}, "/");

        ReqNoBody req = new ReqNoBody();
        req.setToken(Utils.getAppToken(false));
        req.setCmd(iArgs.getString(AppConstants.K.COMMAND.name()));

        try {
            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ReqNoBody> reqEntity = new HttpEntity<>(req, headers);

            ResponseEntity<RespCommentDetails> resp = RestClient.INSTANCE.handle().exchange(commentUri, HttpMethod.POST, reqEntity, RespCommentDetails.class);
            if (resp.getBody() != null) {
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                // post comments
                appendCommentsOps(resp.getBody().getBody(), ops);
                if (ops.size() > 0) {
                    ContentProviderResult[] result = getContentResolver().
                            applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                }
            }

        } catch (Exception e) {
            // muted
            Resp resp = Utils.parseAsRespSilently(e);
            Log.e(TAG, resp == null ? "null" : resp.getMessage(), e);
        }
    }

    public static void appendCommentsOps(CommentDetails comments, ArrayList<ContentProviderOperation> ops) {
        // comments
        if (comments == null) {
            return;
        }
        ContentProviderOperation.Builder opsBldr;

        //grab comment
        opsBldr = ContentProviderOperation.newUpdate(SchemaComments.CONTENT_URI)
                .withValue(SchemaComments.COLUMN_UID, comments.getUid())
                .withValue(SchemaComments.COLUMN_CREATED_AT, comments.getCreatedAt().getTime())
                .withValue(SchemaComments.COLUMN_ANONYMOUS, comments.getAnonymous())
                .withValue(SchemaComments.COLUMN_TEXT, comments.getText())
                .withValue(SchemaComments.COLUMN_UPVOTE_COUNT, comments.getUpvoteCount())
                .withValue(SchemaComments.COLUMN_UPVOTED, comments.getUpvoted())
                .withValue(SchemaComments.COLUMN_PERMISSIONS, comments.getPermissionsForDB());

        //grab commenter info
        Commenter commenter = comments.getCommenter();
        if (commenter != null) {
            opsBldr.withValue(SchemaComments.COLUMN_DOT_UID, commenter.getUid())
                    .withValue(SchemaComments.COLUMN_DOT_FNAME, commenter.getFname())
                    .withValue(SchemaComments.COLUMN_DOT_LNAME, commenter.getLname())
                    .withValue(SchemaComments.COLUMN_DOT_PINCH_HANDLE, commenter.getPinchHandle())
                    .withValue(SchemaComments.COLUMN_DOT_PHOTO_URL, commenter.getPhoto());
        }
        ops.add(opsBldr.build());
    }
}
