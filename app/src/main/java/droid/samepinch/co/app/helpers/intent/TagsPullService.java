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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;

import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.pubsubs.BusProvider;
import droid.samepinch.co.app.helpers.pubsubs.Events;
import droid.samepinch.co.data.dao.SchemaTags;
import droid.samepinch.co.rest.ReqGroups;
import droid.samepinch.co.rest.RespGroups;
import droid.samepinch.co.rest.RestClient;

import static droid.samepinch.co.app.helpers.AppConstants.API.GROUPS;
import static droid.samepinch.co.app.helpers.AppConstants.APP_INTENT.KEY_NAME;


/**
 * Created by imaginationcoder on 7/16/15.
 */
public class TagsPullService extends IntentService {
    public static final String LOG_TAG = "TagsPullService";

    public TagsPullService() {
        super("TagsPullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get caller data
        Bundle iArgs = intent.getExtras();

        ReqGroups req = new ReqGroups();
        req.setToken(Utils.getAppToken(false));
        req.setCmd("show");
        req.setName(iArgs.getString(KEY_NAME.getValue()));

        try {

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ReqGroups> reqEntity = new HttpEntity<>(req, headers);
            //ResponseEntity<String> resp = RestClient.INSTANCE.handle().exchange(GROUPS.getValue(), HttpMethod.POST, reqEntity, String.class);
            ResponseEntity<RespGroups> resp = RestClient.INSTANCE.handle().exchange(GROUPS.getValue(), HttpMethod.POST, reqEntity, RespGroups.class);

            ArrayList<ContentProviderOperation> ops = parseResponse(resp.getBody());
            if (ops != null) {
                ContentProviderResult[] result = getContentResolver().
                        applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                BusProvider.INSTANCE.getBus().post(new Events.TagRefreshedEvent(null));
            }
        } catch (Exception e) {

        }

    }

    @NonNull
    private ArrayList<ContentProviderOperation> parseResponse(RespGroups respData) {
        RespGroups.Body body = respData.getBody();
        if (body == null) {
            return null;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                .withValue(SchemaTags.COLUMN_NAME, body.getName())
                .withValue(SchemaTags.COLUMN_UID, body.getUid())
                .withValue(SchemaTags.COLUMN_POSTS_COUNT, body.getPostsCount())
                .withValue(SchemaTags.COLUMN_FOLLOWERS_COUNT, body.getFollowersCount())
                .withValue(SchemaTags.COLUMN_FOLLOWERS_COUNT, body.getFollowersCount())
                .withValue(SchemaTags.COLUMN_IMAGE, body.getImage())
                .build());
        return ops;
    }
}
