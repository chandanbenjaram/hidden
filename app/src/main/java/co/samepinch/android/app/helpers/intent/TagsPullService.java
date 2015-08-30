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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaTags;
import co.samepinch.android.rest.ReqGroups;
import co.samepinch.android.rest.RespArrGroups;
import co.samepinch.android.rest.RespGroups;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.GROUPS;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_NAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PHOTO;
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

        ReqGroups req = new ReqGroups();
        req.setToken(Utils.getAppToken(false));
        req.setCmd("recommendations");

        try {

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ReqGroups> reqEntity = new HttpEntity<>(req, headers);
            ResponseEntity<RespArrGroups> resp = RestClient.INSTANCE.handle().exchange(GROUPS.getValue(), HttpMethod.POST, reqEntity, RespArrGroups.class);

            ArrayList<ContentProviderOperation> ops = parseResponse(resp.getBody());
            if (ops != null) {
                ContentProviderResult[] result = getContentResolver().
                        applyBatch(AppConstants.API.CONTENT_AUTHORITY.getValue(), ops);
                BusProvider.INSTANCE.getBus().post(new Events.TagsRefreshedEvent(null));
            }
        } catch (Exception e) {

        }

    }

    @NonNull
    private ArrayList<ContentProviderOperation> parseResponse(RespArrGroups respData) {
        RespArrGroups.Body body = respData.getBody();
        if (body == null) {
            return null;
        }

        Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
        String currUserId = userInfo.get(KEY_UID.getValue());

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (RespArrGroups.BodyItem gItem : body.getGroups()) {
            ops.add(ContentProviderOperation.newInsert(SchemaTags.CONTENT_URI)
                    .withValue(SchemaTags.COLUMN_NAME, gItem.getName())
                    .withValue(SchemaTags.COLUMN_UID, gItem.getUid())
                    .withValue(SchemaTags.COLUMN_IMAGE, gItem.getImage())
                    .withValue(SchemaTags.COLUMN_USER_ID, currUserId)
                    .build());
        }
        return ops;
    }
}
