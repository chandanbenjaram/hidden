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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import co.samepinch.android.data.dto.Post;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqPosts;
import co.samepinch.android.rest.RespPosts;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_BY;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_ETAG;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FRESH_DATA_FLAG;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_KEY;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_LAST_MODIFIED;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POSTS_FAV;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POSTS_TAG;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POSTS_WALL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_POST_COUNT;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_STEP;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class PostsPullService extends IntentService {
    public static final String TAG = "PostsPullService";
    public static final String DFLT_ZERO = "0";

    public PostsPullService() {
        super("PostsPullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Map<String, String> metaData = new HashMap<>();

        try {
            StorageComponent component = DaggerStorageComponent.create();
            ReqPosts postsReq = component.provideReqPosts();

            // get caller data
            Bundle iArgs = intent.getExtras();
            // set base args
            postsReq.setToken(Utils.getNonBlankAppToken());
            postsReq.setCmd("filter");
            // set context args
            Integer postCount = Integer.parseInt(iArgs.getString(KEY_POST_COUNT.getValue(), DFLT_ZERO));
            postsReq.setPostCount(postCount);
            postsReq.setLastModified(iArgs.getString(KEY_LAST_MODIFIED.getValue(), EMPTY));
            postsReq.setKey(iArgs.getString(KEY_STEP.getValue(), "new"));
            postsReq.setEtag(iArgs.getString(KEY_ETAG.getValue(), EMPTY));
            postsReq.setKey(iArgs.getString(KEY_KEY.getValue(), EMPTY));
            postsReq.setBy(iArgs.getString(KEY_BY.getValue(), EMPTY));

            boolean isFreshData = iArgs.getBoolean(KEY_FRESH_DATA_FLAG.getValue(), Boolean.FALSE);

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(RestClient.INSTANCE.jsonMediaType());
            HttpEntity<ReqPosts> payloadEntity = new HttpEntity<>(postsReq.build(), headers);
            ResponseEntity<RespPosts> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.POSTS_WITH_FILTER.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);

            // get latest anonymous image
            if (StringUtils.isNotBlank(resp.getBody().getBody().getAnonymousImage())) {
                Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_ANONYMOUS_IMG.getValue(), resp.getBody().getBody().getAnonymousImage());
            }
            // process posts ops
            ArrayList<ContentProviderOperation> ops = parseResponse(iArgs, resp.getBody());

            if (ops != null) {
                if (isFreshData) {
                    // clear db
                    ArrayList<ContentProviderOperation> dropOps = new ArrayList<ContentProviderOperation>();
                    dropOps.add(ContentProviderOperation.newDelete(SchemaPosts.CONTENT_URI).build());
                    dropOps.add(ContentProviderOperation.newDelete(SchemaPostDetails.CONTENT_URI).build());
                    dropOps.add(ContentProviderOperation.newDelete(SchemaComments.CONTENT_URI).build());

                    ContentProviderResult[] result = getContentResolver().
                            applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), dropOps);
                }

                ContentProviderResult[] result = getContentResolver().
                        applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                if (result.length > 0) {
                    // event data
                    RespPosts.Body respBody = resp.getBody().getBody();
                    metaData.put(KEY_STEP.getValue(), respBody.getEtag());
                    metaData.put(KEY_LAST_MODIFIED.getValue(), respBody.getLastModifiedStr());
                    metaData.put(KEY_ETAG.getValue(), respBody.getEtag());
                    metaData.put(KEY_POST_COUNT.getValue(), String.valueOf(respBody.getPostCount()));
                    metaData.put(KEY_BY.getValue(), iArgs.getString(KEY_BY.getValue(), EMPTY));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error refreshing posts...", e);
        }
        BusProvider.INSTANCE.getBus().post(new Events.PostsRefreshedEvent(metaData));
    }


    private ArrayList<ContentProviderOperation> parseResponse(Bundle iArgs, RespPosts respData) {
        List<Post> postsToInsert = respData.getBody().getPosts();
        if (postsToInsert == null) {
            return null;
        }

        // anonymous dot construction
        StorageComponent component = DaggerStorageComponent.create();
        User anonyOwner = component.provideAnonymousDot();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        User postOwner;
        for (Post post : postsToInsert) {
            postOwner = post.getOwner();
            // dot
            appendDOTOps(postOwner, anonyOwner, ops);
            // post
            appendPostOps(iArgs, post, postOwner, anonyOwner, ops);
        }
        return ops;

    }

    private static void appendDOTOps(User postOwner, User anonyOwner, ArrayList<ContentProviderOperation> ops) {
        if (postOwner == null) {
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

    private static void appendPostOps(Bundle iArgs, Post post, User postOwner, User anonyOwner, ArrayList<ContentProviderOperation> ops) {
        ContentProviderOperation.Builder bldr = ContentProviderOperation.newInsert(SchemaPosts.CONTENT_URI);
        bldr.withValue(SchemaPosts.COLUMN_UID, post.getUid())
                .withValue(SchemaPosts.COLUMN_WALL_CONTENT, post.getWallContent())
                .withValue(SchemaPosts.COLUMN_WALL_IMAGES, post.getWallImagesForDB())
                .withValue(SchemaPosts.COLUMN_COMMENT_COUNT, post.getCommentCount())
                .withValue(SchemaPosts.COLUMN_UPVOTE_COUNT, post.getUpvoteCount())
                .withValue(SchemaPosts.COLUMN_VIEWS, post.getViews())
                .withValue(SchemaPosts.COLUMN_ANONYMOUS, post.getAnonymous())
                .withValue(SchemaPosts.COLUMN_CREATED_AT, Utils.stringToDate(post.getCreatedAtStr()).getTime())
                .withValue(SchemaPosts.COLUMN_COMMENTERS, post.getCommentersForDB())
                .withValue(SchemaPosts.COLUMN_OWNER, postOwner != null ? postOwner.getUid() : anonyOwner.getUid())
                .withValue(SchemaPosts.COLUMN_TAGS, post.getTagsForDB());
        String srcBy = iArgs.getString(KEY_BY.getValue(), EMPTY);
        if (StringUtils.isBlank(srcBy) || StringUtils.equals(srcBy, KEY_POSTS_WALL.getValue())) {
            bldr.withValue(SchemaPosts.COLUMN_SRC_WALL, true);
        } else if (StringUtils.equals(srcBy, KEY_POSTS_FAV.getValue())) {
            bldr.withValue(SchemaPosts.COLUMN_SRC_FAV, true);
        } else if (StringUtils.equals(srcBy, KEY_POSTS_TAG.getValue())) {
            bldr.withValue(SchemaPosts.COLUMN_SRC_TAG, true);
        }
        ops.add(bldr.build());
    }
}
