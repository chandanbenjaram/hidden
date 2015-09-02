package co.samepinch.android.app.helpers.intent;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dao.SchemaPostDetails;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.data.dto.Post;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqPosts;
import co.samepinch.android.rest.RespPosts;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.*;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class PostsPullService extends IntentService {
    public static final String LOG_TAG = "PostsPullService";

    public PostsPullService() {
        super("PostsPullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        StorageComponent component = DaggerStorageComponent.create();
        ReqPosts postsReq = component.provideReqPosts();

        // get caller data
        Bundle iArgs = intent.getExtras();
        // set base args
        postsReq.setToken(Utils.getAppToken(false));
        postsReq.setCmd("filter");
        // set context args
        postsReq.setPostCount(iArgs.getString(KEY_POST_COUNT.getValue()));
        postsReq.setLastModified(iArgs.getString(KEY_LAST_MODIFIED.getValue()));
        postsReq.setStep(StringUtils.defaultString(iArgs.getString(KEY_STEP.getValue())));
        postsReq.setEtag(iArgs.getString(KEY_ETAG.getValue()));
        postsReq.setKey(iArgs.getString(KEY_KEY.getValue()));
        postsReq.setBy(iArgs.getString(KEY_BY.getValue()));

        //headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        try {
            HttpEntity<ReqPosts> payloadEntity;
            ResponseEntity<RespPosts> resp = null;
            try {
                // call remote
                payloadEntity = new HttpEntity<>(postsReq.build(), headers);
                resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.POSTS_WITH_FILTER.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    // try resetting token?
                    postsReq.setToken(Utils.getAppToken(true));
                    payloadEntity = new HttpEntity<>(postsReq.build(), headers);
                    resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);
                } else {
                    throw new IllegalStateException("un-known response code.", e);
                }
            }

            // publish this event
            Map<String, String> metaData = new HashMap<>();
            RespPosts.Body respBody = resp.getBody().getBody();
            metaData.put(KEY_LAST_MODIFIED.getValue(), respBody.getLastModifiedStr());
            metaData.put(KEY_ETAG.getValue(), respBody.getEtag());
            metaData.put(KEY_POST_COUNT.getValue(), String.valueOf(respBody.getPostCount()));

            ArrayList<ContentProviderOperation> ops = parseResponse(resp.getBody());
            if(ops != null){
                ContentProviderResult[] result = getContentResolver().
                        applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                BusProvider.INSTANCE.getBus().post(new Events.PostsRefreshedEvent(metaData));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private ArrayList<ContentProviderOperation> parseResponse(RespPosts respData) {
        List<Post> postsToInsert = respData.getBody().getPosts();
        if(postsToInsert == null){
            return null;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // anonymous dot construction
        StorageComponent component = DaggerStorageComponent.create();
        User dfltAnonyDot = component.provideAnonymousDot();
        String anonyImg = respData.getBody().getAnonymousImage();
        dfltAnonyDot.setPhoto(anonyImg);

        for (Post post : postsToInsert) {
            User postOwner = post.getOwner();
            // DOTS
            if (post.getAnonymous()) {
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
            // POSTS
            ops.add(ContentProviderOperation.newInsert(SchemaPosts.CONTENT_URI)
                    .withValue(SchemaPosts.COLUMN_UID, post.getUid())
                    .withValue(SchemaPosts.COLUMN_CONTENT, post.getContent())
                    .withValue(SchemaPosts.COLUMN_IMAGES, post.getImagesForDB())
                    .withValue(SchemaPosts.COLUMN_COMMENT_COUNT, post.getCommentCount())
                    .withValue(SchemaPosts.COLUMN_UPVOTE_COUNT, post.getUpvoteCount())
                    .withValue(SchemaPosts.COLUMN_VIEWS, post.getViews())
                    .withValue(SchemaPosts.COLUMN_ANONYMOUS, post.getAnonymous())
                    .withValue(SchemaPosts.COLUMN_CREATED_AT, post.getCreatedAt().getTime())
                    .withValue(SchemaPosts.COLUMN_COMMENTERS, post.getCommentersForDB())
                    .withValue(SchemaPosts.COLUMN_COMMENTERS, post.getCommentersForDB())
                    .withValue(SchemaPosts.COLUMN_OWNER, (post.getAnonymous() ? dfltAnonyDot.getUid() : postOwner.getUid()))
                    .withValue(SchemaPosts.COLUMN_TAGS, post.getTagsForDB()).build());
            // TAGS
            for (String tag : post.getTags()) {
                ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                        .withValue(SchemaTags.COLUMN_NAME, tag)
                        .build());
            }
        }
        return ops;
    }
}
