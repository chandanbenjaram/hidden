package droid.samepinch.co.app.helpers.intent;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;

import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.module.DaggerStorageComponent;
import droid.samepinch.co.app.helpers.module.StorageComponent;
import droid.samepinch.co.app.helpers.pubsubs.BusProvider;
import droid.samepinch.co.app.helpers.pubsubs.Events;
import droid.samepinch.co.data.dao.SchemaComments;
import droid.samepinch.co.data.dao.SchemaDots;
import droid.samepinch.co.data.dao.SchemaPostDetails;
import droid.samepinch.co.data.dao.SchemaTags;
import droid.samepinch.co.data.dto.CommentDetails;
import droid.samepinch.co.data.dto.PostDetails;
import droid.samepinch.co.data.dto.User;
import droid.samepinch.co.rest.ReqNoBody;
import droid.samepinch.co.rest.RespPostDetails;
import droid.samepinch.co.rest.RestClient;

import static droid.samepinch.co.app.helpers.AppConstants.API.POSTS;
import static droid.samepinch.co.app.helpers.AppConstants.APP_INTENT.KEY_UID;


/**
 * Created by imaginationcoder on 7/16/15.
 */
public class PostDetailsService extends IntentService {
    public static final String LOG_TAG = "PostDetailsService";

    public PostDetailsService() {
        super("PostDetailsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get caller data
        Bundle iArgs = intent.getExtras();
        ReqNoBody req = new ReqNoBody();
        req.setToken(Utils.getAppToken(false));
        req.setCmd("show");
        String postUri = StringUtils.join(new String[]{POSTS.getValue(), iArgs.getString(KEY_UID.getValue())}, "/");

        try {

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ReqNoBody> reqEntity = new HttpEntity<>(req, headers);
            ResponseEntity<RespPostDetails> resp = RestClient.INSTANCE.handle().exchange(postUri, HttpMethod.POST, reqEntity, RespPostDetails.class);

            ArrayList<ContentProviderOperation> ops = parseResponse(resp.getBody());
            if(ops != null){
                ContentProviderResult[] result = getContentResolver().
                        applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                BusProvider.INSTANCE.getBus().post(new Events.PostDetailsRefreshEvent(null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @NonNull
    private ArrayList<ContentProviderOperation> parseResponse(RespPostDetails respData) {
        PostDetails details = respData.getBody();
        if (details == null) {
            return null;
        }

        // anonymous dot construction
        StorageComponent component = DaggerStorageComponent.create();
        User dfltAnonyDot = component.provideAnonymousDot();
        String anonyImg = details.getAnonymousImage();

        User postOwner = details.getOwner();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        // DOTS
        if (details.getAnonymous()) {
            // TODO
            ops.add(ContentProviderOperation.newInsert(SchemaDots.CONTENT_URI)
                    .withValue(SchemaDots.COLUMN_UID, dfltAnonyDot.getUid())
                    .withValue(SchemaDots.COLUMN_FNAME, dfltAnonyDot.getFname())
                    .withValue(SchemaDots.COLUMN_LNAME, dfltAnonyDot.getLname())
                    .withValue(SchemaDots.COLUMN_PREF_NAME, dfltAnonyDot.getPrefName())
                    .withValue(SchemaDots.COLUMN_PINCH_HANDLE, dfltAnonyDot.getPinchHandle())
                    .withValue(SchemaDots.COLUMN_PHOTO_URL, dfltAnonyDot.getPhoto()).build());
        } else {
            ops.add(ContentProviderOperation.newInsert(SchemaDots.CONTENT_URI)
                    .withValue(SchemaDots.COLUMN_UID, postOwner.getUid())
                    .withValue(SchemaDots.COLUMN_FNAME, postOwner.getFname())
                    .withValue(SchemaDots.COLUMN_LNAME, postOwner.getLname())
                    .withValue(SchemaDots.COLUMN_PREF_NAME, postOwner.getPrefName())
                    .withValue(SchemaDots.COLUMN_PINCH_HANDLE, postOwner.getPinchHandle())
                    .withValue(SchemaDots.COLUMN_PHOTO_URL, postOwner.getPhoto()).build());
        }
        // POST DETAILS
        ops.add(ContentProviderOperation.newInsert(SchemaPostDetails.CONTENT_URI)
                .withValue(SchemaPostDetails.COLUMN_UID, details.getUid())
                .withValue(SchemaPostDetails.COLUMN_CONTENT, details.getContent())
                .withValue(SchemaPostDetails.COLUMN_IMAGES, details.getImagesForDB())
                .withValue(SchemaPostDetails.COLUMN_LARGE_IMAGES, details.getImagesForDB())
                .withValue(SchemaPostDetails.COLUMN_COMMENT_COUNT, details.getCommentCount())
                .withValue(SchemaPostDetails.COLUMN_UPVOTE_COUNT, details.getUpvoteCount())
                .withValue(SchemaPostDetails.COLUMN_VIEWS, details.getViews())
                .withValue(SchemaPostDetails.COLUMN_ANONYMOUS, details.getAnonymous())
                .withValue(SchemaPostDetails.COLUMN_CREATED_AT, details.getCreatedAt().getTime())
                .withValue(SchemaPostDetails.COLUMN_OWNER, (details.getAnonymous() ? dfltAnonyDot.getUid() : postOwner.getUid()))
                .withValue(SchemaPostDetails.COLUMN_TAGS, details.getTagsForDB()).build());

        // comments
        if( details.getComments() !=null){
            for(CommentDetails cd : details.getComments()){
                ops.add(ContentProviderOperation.newInsert(SchemaComments.CONTENT_URI)
                        .withValue(SchemaComments.COLUMN_UID, cd.getUid())
                        .withValue(SchemaComments.COLUMN_CREATED_AT, cd.getCreatedAt().getTime())
                        .withValue(SchemaComments.COLUMN_ANONYMOUS, cd.getAnonymous())
                        .withValue(SchemaComments.COLUMN_TEXT, cd.getText())
                        .withValue(SchemaComments.COLUMN_UPVOTE_COUNT, cd.getUpvoteCount())
                        .withValue(SchemaComments.COLUMN_DOT_UID, cd.getCommenter().getUid())
                        .withValue(SchemaComments.COLUMN_DOT_FNAME, cd.getCommenter().getFname())
                        .withValue(SchemaComments.COLUMN_DOT_LNAME, cd.getCommenter().getLname())
                        .withValue(SchemaComments.COLUMN_DOT_PINCH_HANDLE, cd.getCommenter().getPinchHandle())
                        .withValue(SchemaComments.COLUMN_DOT_PHOTO_URL, cd.getCommenter().getPhoto())
                        .withValue(SchemaComments.COLUMN_POST_DETAILS, details.getUid()).build());
            }
        }

        // TAGS
        for (String tag : details.getTags()) {
            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withValue(SchemaTags.COLUMN_NAME, tag)
                    .build());
        }

        return ops;
    }
}
