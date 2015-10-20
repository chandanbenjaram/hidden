package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.rest.ReqGroups;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespArrGroups;
import co.samepinch.android.rest.RespTags;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.GROUPS;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_CHECK_EMAIL_EXISTENCE;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_EMAIL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_MSG_GENERIC_ERR;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_TAGS_PULL_ALL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_TAGS_PULL_FAV;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_TAGS_PULL_RECOMMENDED;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_TAGS_PULL_TYPE;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_UID;


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
        try {
            // get caller data
            Bundle iArgs = intent.getExtras();
            // default all
            String reqType;
            if (iArgs == null) {
                reqType = KEY_TAGS_PULL_ALL.getValue();
            } else {
                reqType = iArgs.getString(KEY_TAGS_PULL_TYPE.getValue(), KEY_TAGS_PULL_ALL.getValue());
            }

            if (StringUtils.equals(KEY_TAGS_PULL_FAV.getValue(), reqType)) {
                procAsSpecificIntent("favourites");
                return;
            } else if (StringUtils.equals(KEY_TAGS_PULL_RECOMMENDED.getValue(), reqType)) {
                procAsSpecificIntent("recommendations");
                return;
            }

            // process as req all
            ReqGroups req = new ReqGroups();
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("all");

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(RestClient.INSTANCE.jsonMediaType());

            HttpEntity<ReqGroups> reqEntity = new HttpEntity<>(req, headers);
            ResponseEntity<RespTags> resp = RestClient.INSTANCE.handle().exchange(GROUPS.getValue(), HttpMethod.POST, reqEntity, RespTags.class);

            ArrayList<ContentProviderOperation> ops = parseResponse(resp.getBody());
            if (ops != null) {
                ContentProviderResult[] result = getContentResolver().
                        applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                BusProvider.INSTANCE.getBus().post(new Events.TagsRefreshedEvent(null));
            }
        } catch (Exception e) {
            String errMsg;
            Resp resp = Utils.parseAsRespSilently(e);
            if (resp != null) {
                errMsg = resp.getMessage();
            } else {
                errMsg = KEY_MSG_GENERIC_ERR.getValue();
            }

            Map<String, String> cause = new HashMap<>();
            cause.put(AppConstants.K.MESSAGE.name(), errMsg);
            BusProvider.INSTANCE.getBus().post(new Events.TagsRefreshFailEvent(cause));
        }

    }

    private void procAsSpecificIntent(String cmd) throws RemoteException, OperationApplicationException {
        ReqGroups req = new ReqGroups();
        req.setToken(Utils.getNonBlankAppToken());
        req.setCmd(cmd);

        //headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(RestClient.INSTANCE.jsonMediaType());

        HttpEntity<ReqGroups> reqEntity = new HttpEntity<>(req, headers);
        ResponseEntity<RespArrGroups> resp = RestClient.INSTANCE.handle().exchange(GROUPS.getValue(), HttpMethod.POST, reqEntity, RespArrGroups.class);

        String currUserId = null;
        if (StringUtils.equals(cmd, "favourites")) {
            Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
            currUserId = userInfo.get(KEY_UID.getValue());
        }


        ArrayList<ContentProviderOperation> ops = parseResponse(resp.getBody(), currUserId);
        if (ops != null) {
            ContentProviderResult[] result = getContentResolver().
                    applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
            BusProvider.INSTANCE.getBus().post(new Events.TagsRefreshedEvent(null));
        }
    }

    @NonNull
    private ArrayList<ContentProviderOperation> parseResponse(RespArrGroups respData, String userId) {
        RespArrGroups.Body body = respData.getBody();
        if (body == null || body.getGroups() == null) {
            return null;
        }


        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (RespArrGroups.BodyItem gItem : body.getGroups()) {
            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withValue(SchemaTags.COLUMN_NAME, gItem.getName())
                    .withValue(SchemaTags.COLUMN_UID, gItem.getUid())
                    .withValue(SchemaTags.COLUMN_IMAGE, gItem.getImage())
                    .withValue(SchemaTags.COLUMN_USER_ID, userId)
                    .build());
        }
        return ops;
    }

    @NonNull
    private ArrayList<ContentProviderOperation> parseResponse(RespTags respData) {
        RespTags.Body body = respData.getBody();
        if (body == null) {
            return null;
        }
        Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
        String currUserId = userInfo.get(KEY_UID.getValue());

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (RespArrGroups.BodyItem gItem : body.getFavourites()) {
            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withValue(SchemaTags.COLUMN_NAME, gItem.getName())
                    .withValue(SchemaTags.COLUMN_UID, gItem.getUid())
                    .withValue(SchemaTags.COLUMN_IMAGE, gItem.getImage())
                    .withValue(SchemaTags.COLUMN_USER_ID, currUserId)
                    .build());
        }

        for (RespArrGroups.BodyItem gItem : body.getRecommended()) {
            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withValue(SchemaTags.COLUMN_NAME, gItem.getName())
                    .withValue(SchemaTags.COLUMN_UID, gItem.getUid())
                    .withValue(SchemaTags.COLUMN_IMAGE, gItem.getImage())
                    .withValue(SchemaTags.COLUMN_USER_ID, null)
                    .build());
        }
        return ops;
    }
}
