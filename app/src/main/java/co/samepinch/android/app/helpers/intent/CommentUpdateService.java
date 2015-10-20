package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaComments;
import co.samepinch.android.data.dto.CommentDetails;
import co.samepinch.android.rest.ReqGeneric;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespCommentDetails;
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
        String commentUID = iArgs.getString(AppConstants.K.COMMENT.name());
        String commentUri = StringUtils.join(new String[]{COMMENTS.getValue(), commentUID}, "/");
        Bundle body = iArgs.getBundle(AppConstants.K.BODY.name());

        ReqGeneric<Map<String, String>> req = new ReqGeneric<>();
        req.setToken(Utils.getAppToken(false));
        req.setCmd(iArgs.getString(AppConstants.K.COMMAND.name()));
        if (body != null) {
            Map<String, String> bodyMap = new HashMap<>();
            for (String k : body.keySet()) {
                bodyMap.put(k, body.getString(k));
            }
            req.setBody(bodyMap);
        }

        try {
            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(RestClient.INSTANCE.jsonMediaType());

            HttpEntity<ReqGeneric<Map<String, String>>> reqEntity = new HttpEntity<>(req, headers);

            ResponseEntity<RespCommentDetails> resp = RestClient.INSTANCE.handle().exchange(commentUri, HttpMethod.POST, reqEntity, RespCommentDetails.class);
            CommentDetails commentDetails;
            if (resp.getBody() != null && (commentDetails = resp.getBody().getBody()) != null) {
                ContentValues values = new ContentValues();
                values.put(SchemaComments.COLUMN_CREATED_AT, commentDetails.getCreatedAt().getTime());
                values.put(SchemaComments.COLUMN_ANONYMOUS, commentDetails.getAnonymous());
                values.put(SchemaComments.COLUMN_TEXT, commentDetails.getText());
                values.put(SchemaComments.COLUMN_UPVOTE_COUNT, commentDetails.getUpvoteCount());
                values.put(SchemaComments.COLUMN_UPVOTED, commentDetails.getUpvoted());
                values.put(SchemaComments.COLUMN_PERMISSIONS, commentDetails.getPermissionsForDB());

                int dbResult = getContentResolver().update(SchemaComments.CONTENT_URI, values, SchemaComments.COLUMN_UID + "=?", new String[]{commentUID});
                if (dbResult > 0) {
                    BusProvider.INSTANCE.getBus().post(new Events.CommentDetailsRefreshEvent(null));
                } else {
                    Log.e(TAG, "no comment record found to update");
                }
            } else if(resp.getBody() !=null && StringUtils.equals(req.getCmd(), "flag")){
                Map<String, String> eventData = new HashMap<>();
                String msg = resp.getBody().getMessage() == null ? AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR.getValue() : resp.getBody().getMessage();
                eventData.put(AppConstants.K.MESSAGE.name(), msg);
                BusProvider.INSTANCE.getBus().post(new Events.CommentDetailsRefreshEvent(eventData));
            }

        } catch (Exception e) {
            // muted
        }
    }
}
