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
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaComments;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dao.SchemaPostDetails;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.data.dto.CommentDetails;
import co.samepinch.android.data.dto.Commenter;
import co.samepinch.android.data.dto.PostDetails;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespPostDetails;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.POSTS;

public class PostDetailsService extends IntentService {
    public static final String TAG = "PostDetailsService";

    public PostDetailsService() {
        super("PostDetailsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get caller data
        Bundle iArgs = intent.getExtras();
        String postUri = StringUtils.join(new String[]{POSTS.getValue(), iArgs.getString(AppConstants.K.POST.name())}, "/");

        ReqNoBody req = new ReqNoBody();
        req.setToken(Utils.getAppToken(false));
        req.setCmd("show");

        try {

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ReqNoBody> reqEntity = new HttpEntity<>(req, headers);
            ResponseEntity<RespPostDetails> resp = RestClient.INSTANCE.handle().exchange(postUri, HttpMethod.POST, reqEntity, RespPostDetails.class);
            ArrayList<ContentProviderOperation> ops = parseResponse(resp.getBody());
            if (ops != null) {
                ContentProviderResult[] result = getContentResolver().
                        applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);

                BusProvider.INSTANCE.getBus().post(new Events.PostDetailsRefreshEvent(null));
            }
        } catch (Exception e) {
            // muted
            Resp resp = Utils.parseAsRespSilently(e);
            Log.e(TAG, resp == null ? "null" : resp.getMessage(), e);
        }
    }

    public static ArrayList<ContentProviderOperation> parseResponse(RespPostDetails respData) {
        PostDetails details = respData.getBody();
        if (details == null) {
            return null;
        }

        if (StringUtils.isNotBlank(details.getAnonymousImage())) {
            Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_ANONYMOUS_IMG.getValue(), details.getAnonymousImage());
        }

        // anonymous dot construction
        StorageComponent component = DaggerStorageComponent.create();
        User anonyOwner = component.provideAnonymousDot();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        // DOT
        appendDOTOps(details, anonyOwner, ops);
        // post details incl. content
        appendPostDetailsOps(details, anonyOwner, ops);

        // post details incl. content
        appendPostOps(details, anonyOwner, ops);

        // post comments
        appendCommentsOps(details, anonyOwner, ops);

        // TAGS
        for (String tag : details.getTags()) {
            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withValue(SchemaTags.COLUMN_NAME, tag)
                    .build());
        }

        return ops;
    }

    private static void appendDOTOps(PostDetails details, User anonyOwner, ArrayList<ContentProviderOperation> ops) {
        final User postOwner = details.getOwner();
        if (details.getAnonymous()) {
            ops.add(ContentProviderOperation.newInsert(SchemaDots.CONTENT_URI)
                    .withValue(SchemaDots.COLUMN_UID, anonyOwner.getUid())
                    .withValue(SchemaDots.COLUMN_FNAME, anonyOwner.getFname())
                    .withValue(SchemaDots.COLUMN_LNAME, anonyOwner.getLname())
                    .withValue(SchemaDots.COLUMN_PREF_NAME, anonyOwner.getPrefName())
                    .withValue(SchemaDots.COLUMN_PINCH_HANDLE, anonyOwner.getPinchHandle())
                    .withValue(SchemaDots.COLUMN_PHOTO_URL, anonyOwner.getPhoto()).build());
        } else {
            ops.add(ContentProviderOperation.newInsert(SchemaDots.CONTENT_URI)
                    .withValue(SchemaDots.COLUMN_UID, postOwner.getUid())
                    .withValue(SchemaDots.COLUMN_FNAME, postOwner.getFname())
                    .withValue(SchemaDots.COLUMN_LNAME, postOwner.getLname())
                    .withValue(SchemaDots.COLUMN_PREF_NAME, postOwner.getPrefName())
                    .withValue(SchemaDots.COLUMN_PINCH_HANDLE, postOwner.getPinchHandle())
                    .withValue(SchemaDots.COLUMN_PHOTO_URL, postOwner.getPhoto()).build());
        }
    }

    private static void appendPostDetailsOps(PostDetails details, User anonyOwner, ArrayList<ContentProviderOperation> ops) {
        final User postOwner = details.getOwner();
        // POST DETAILS
        ops.add(ContentProviderOperation.newInsert(SchemaPostDetails.CONTENT_URI)
                .withValue(SchemaPostDetails.COLUMN_UID, details.getUid())
                .withValue(SchemaPostDetails.COLUMN_URL, details.getUrl())
                .withValue(SchemaPostDetails.COLUMN_CONTENT, details.getContent())
                .withValue(SchemaPostDetails.COLUMN_IMAGES, details.getImagesForDB())
                .withValue(SchemaPostDetails.COLUMN_LARGE_IMAGES, details.getImagesForDB())
                .withValue(SchemaPostDetails.COLUMN_COMMENT_COUNT, details.getCommentCount())
                .withValue(SchemaPostDetails.COLUMN_UPVOTE_COUNT, details.getUpvoteCount())
                .withValue(SchemaPostDetails.COLUMN_UPVOTED, details.getUpvoted())
                .withValue(SchemaPostDetails.COLUMN_VIEWS, details.getViews())
                .withValue(SchemaPostDetails.COLUMN_ANONYMOUS, details.getAnonymous())
                .withValue(SchemaPostDetails.COLUMN_CREATED_AT, details.getCreatedAt().getTime())
                .withValue(SchemaPostDetails.COLUMN_OWNER, (details.getAnonymous() ? anonyOwner.getUid() : postOwner.getUid()))
                .withValue(SchemaPostDetails.COLUMN_PERMISSIONS, details.getPermissionsForDB())
                .withValue(SchemaPostDetails.COLUMN_TAGS, details.getTagsForDB()).build());
    }

    private static void appendPostOps(PostDetails details, User anonyOwner, ArrayList<ContentProviderOperation> ops) {
        final User postOwner = details.getOwner();
        ops.add(ContentProviderOperation.newInsert(SchemaPosts.CONTENT_URI)
                .withValue(SchemaPosts.COLUMN_UID, details.getUid())
                .withValue(SchemaPosts.COLUMN_CONTENT, details.getContent())
                .withValue(SchemaPosts.COLUMN_IMAGES, details.getImagesForDB())
                .withValue(SchemaPosts.COLUMN_COMMENT_COUNT, details.getCommentCount())
                .withValue(SchemaPosts.COLUMN_UPVOTE_COUNT, details.getUpvoteCount())
                .withValue(SchemaPosts.COLUMN_VIEWS, details.getViews())
                .withValue(SchemaPosts.COLUMN_ANONYMOUS, details.getAnonymous())
                .withValue(SchemaPosts.COLUMN_CREATED_AT, details.getCreatedAt().getTime())
                .withValue(SchemaPosts.COLUMN_OWNER, (details.getAnonymous() ? anonyOwner.getUid() : postOwner.getUid()))
                .withValue(SchemaPosts.COLUMN_TAGS, details.getTagsForDB())
                .build());
    }

    private static void appendCommentsOps(PostDetails details, User anonyOwner, ArrayList<ContentProviderOperation> ops) {
        // comments
        if (details.getComments() == null) {
            return;
        }
        ContentProviderOperation.Builder opsBldr;
        Commenter commenter;
        for (CommentDetails comments : details.getComments()) {
            //grab comment
            opsBldr = ContentProviderOperation.newInsert(SchemaComments.CONTENT_URI)
                    .withValue(SchemaComments.COLUMN_UID, comments.getUid())
                    .withValue(SchemaComments.COLUMN_CREATED_AT, comments.getCreatedAt().getTime())
                    .withValue(SchemaComments.COLUMN_ANONYMOUS, comments.getAnonymous())
                    .withValue(SchemaComments.COLUMN_TEXT, comments.getText())
                    .withValue(SchemaComments.COLUMN_UPVOTE_COUNT, comments.getUpvoteCount())
                    .withValue(SchemaComments.COLUMN_UPVOTED, comments.getUpvoted())
                    .withValue(SchemaComments.COLUMN_PERMISSIONS, comments.getPermissionsForDB())
                    .withValue(SchemaComments.COLUMN_POST_DETAILS, details.getUid());

            //grab commenter info
            commenter = comments.getCommenter();
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
}
