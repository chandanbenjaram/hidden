package droid.samepinch.co.app.helpers.intent;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.module.DaggerStorageComponent;
import droid.samepinch.co.app.helpers.module.StorageComponent;
import droid.samepinch.co.data.dao.SchemaDots;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dao.SchemaTags;
import droid.samepinch.co.data.dto.Post;
import droid.samepinch.co.data.dto.User;
import droid.samepinch.co.rest.ReqPosts;
import droid.samepinch.co.rest.RespPosts;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class PostsPullService extends IntentService {
    public static final String LOG_TAG = "PostsPullService";
    private BroadcastNotifier mBroadcaster;


    public PostsPullService() {
        super(PostsPullService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mBroadcaster = new BroadcastNotifier(this);
//        mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.REFRESH_ACTION_STARTED);

//        if (appToken == null) {
//            mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.REFRESH_ACTION_FAILED);
//            return;
//        }

        StorageComponent component = DaggerStorageComponent.create();
        ReqPosts postsReq = component.provideReqPosts();

        // get caller data
        Bundle iArgs = intent.getExtras();
        // set base args
        postsReq.setToken(Utils.getAppToken(false));
        postsReq.setCmd("filter");
        // set context args
        postsReq.setPostCount(iArgs.getString("post_count"));
        postsReq.setLastModified(iArgs.getString("last_modified"));
        postsReq.setStep(iArgs.getString("step"));
        postsReq.setEtag(iArgs.getString("etag"));
        postsReq.setKey(iArgs.getString("key"));
        postsReq.setBy(iArgs.getString("by"));

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
//                resp = component.provideRestTemplate().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);

                ResponseEntity<String> respStr = component.provideRestTemplate().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, String.class);
                System.out.println("respStr...\n" + respStr.getBody());
                resp = component.provideRestTemplate().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);

            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    // try resetting token?
                    postsReq.setToken(Utils.getAppToken(true));
                    payloadEntity = new HttpEntity<>(postsReq.build(), headers);
//                resp = component.provideRestTemplate().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);
                    ResponseEntity<String> respStr = component.provideRestTemplate().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, String.class);
                    System.out.println("respStr...\n" + respStr.getBody());
                    resp = component.provideRestTemplate().exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);
                } else {
                    throw new IllegalStateException("un-known response code.", e);
                }
            }

            ArrayList<ContentProviderOperation> ops = parseResponse(resp);
            ContentProviderResult[] result = getContentResolver().
                    applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
            mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.REFRESH_ACTION_COMPLETE);
        } catch (Exception e) {
            e.printStackTrace();
//            mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.REFRESH_ACTION_FAILED);
        }
    }

    @NonNull
    private ArrayList<ContentProviderOperation> parseResponse(ResponseEntity<RespPosts> resp) {
        RespPosts respData = resp.getBody();

        List<Post> postsToInsert = respData.getBody().getPosts();
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
//                    dfltAnonyDot.setPhoto(anonyImg);
                // TODO
                ops.add(ContentProviderOperation.newInsert(SchemaDots.CONTENT_URI)
                        .withValue(SchemaDots.COLUMN_UID, dfltAnonyDot.getUid())
                        .withValue(SchemaDots.COLUMN_FNAME, dfltAnonyDot.getFname())
                        .withValue(SchemaDots.COLUMN_LNAME, dfltAnonyDot.getLname())
                        .withValue(SchemaDots.COLUMN_PREF_NAME, dfltAnonyDot.getPrefName())
//                                    .withValue(SchemaDots.COLUMN_PINCH_HANDLE, "anonymous")
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
                    .withValue(SchemaPosts.COLUMN_COMMENT_COUNT, post.getCommentCount())
                    .withValue(SchemaPosts.COLUMN_UPVOTE_COUNT, post.getUpvoteCount())
                    .withValue(SchemaPosts.COLUMN_VIEWS, post.getViews())
                    .withValue(SchemaPosts.COLUMN_ANONYMOUS, post.getAnonymous())
                    .withValue(SchemaPosts.COLUMN_CREATED_AT, post.getCreatedAt().getTime())
                    .withValue(SchemaPosts.COLUMN_COMMENTERS, post.getCommentersForDB())
                    .withValue(SchemaPosts.COLUMN_OWNER, (post.getAnonymous() ? dfltAnonyDot.getUid() : postOwner.getUid()))
                    .withValue(SchemaPosts.COLUMN_TAGS, post.getTagsForDB()).build());
            // TAGS
            for (String tag : post.getTags()) {
                ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                        .withValue(SchemaTags.COLUMN_NAME, tag)
                        .withValue(SchemaTags.COLUMN_PHOTO_URL, "http://bigtheme.ir/wp-content/uploads/2015/06/sample.jpg")
                        .build());
            }
        }
        return ops;
    }


}
