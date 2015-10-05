package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.rest.ReqGeneric;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.API.POSTS;

public class PostMetaUpdateService extends IntentService {
    public static final String TAG = "PostMetaUpdateService";

    public PostMetaUpdateService() {
        super("PostMetaUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get caller data
        Bundle iArgs = intent.getExtras();
        String postUID = iArgs.getString(AppConstants.K.POST.name());
        Bundle body = iArgs.getBundle(AppConstants.K.BODY.name());

        String postUri = StringUtils.join(new String[]{POSTS.getValue(), postUID}, "/");

        ReqGeneric<Map> req = new ReqGeneric();
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
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ReqGeneric<Map>> reqEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(postUri, HttpMethod.POST, reqEntity, Resp.class);
            Map<String, String> eventData = new HashMap<>();
            eventData.put(AppConstants.K.MESSAGE.name(), resp.getBody().getMessage());
            BusProvider.INSTANCE.getBus().post(new Events.PostMetaUpdateServiceSuccessEvent(eventData));
        } catch (Exception e) {
            // muted
            Map<String, String> eventData = new HashMap<>();
            Resp resp = Utils.parseAsRespSilently(e);
            eventData.put(AppConstants.K.MESSAGE.name(), resp.getMessage());
            BusProvider.INSTANCE.getBus().post(new Events.PostMetaUpdateServiceFailEvent(eventData));
        }
    }
}
