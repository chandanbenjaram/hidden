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

import java.util.Arrays;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaComments;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespUserDetails;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.USERS;

public class DotDetailsService extends IntentService {
    public static final String TAG = "DotDetailsService";

    public DotDetailsService() {
        super("DotDetailsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get caller data
        Bundle iArgs = intent.getExtras();
        String dotUID = iArgs.getString(AppConstants.K.DOT.name());
        String dotUri = StringUtils.join(new String[]{USERS.getValue(), dotUID}, "/");

        ReqNoBody req = new ReqNoBody();
        req.setToken(Utils.getAppToken(false));
        req.setCmd("public_profile");

        try {
            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ReqNoBody> reqEntity = new HttpEntity<>(req, headers);
            ResponseEntity<RespUserDetails> resp = RestClient.INSTANCE.handle().exchange(dotUri, HttpMethod.POST, reqEntity, RespUserDetails.class);
            User user;
            if (resp.getBody() != null && (user = resp.getBody().getBody()) != null) {
                ContentValues values = new ContentValues();
                values.put(SchemaDots.COLUMN_FNAME, user.getFname());
                values.put(SchemaDots.COLUMN_LNAME, user.getLname());
                values.put(SchemaDots.COLUMN_PREF_NAME, user.getPrefName());
                values.put(SchemaDots.COLUMN_PINCH_HANDLE, user.getPinchHandle());
                values.put(SchemaDots.COLUMN_PHOTO_URL, user.getPhoto());
                values.put(SchemaDots.COLUMN_POSTS_COUNT, user.getPostsCount());
                values.put(SchemaDots.COLUMN_FOLLOWERS_COUNT, user.getFollowersCount());
                values.put(SchemaDots.COLUMN_BLOG, user.getBlog());
                values.put(SchemaDots.COLUMN_SUMMARY, user.getSummary());
                values.put(SchemaDots.COLUMN_FOLLOW, user.getFollow());

                int dbResult = getContentResolver().update(SchemaDots.CONTENT_URI, values, SchemaComments.COLUMN_UID + "=?", new String[]{dotUID});
                if (dbResult > 0) {
                    BusProvider.INSTANCE.getBus().post(new Events.DotDetailsRefreshEvent(null));
                } else {
                    Log.e(TAG, "no comment record found to update");
                }
            }
        } catch (Exception e) {
            // muted
            Resp resp = Utils.parseAsRespSilently(e);
            Log.e(TAG, resp == null ? "null" : resp.getMessage(), e);
        }
    }
}
