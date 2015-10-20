package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.ReqSignUp;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespLogin;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_CHECK_EMAIL_EXISTENCE;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_EMAIL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_KEY;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_LNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PASSWORD;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PINCH_HANDLE;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class MultiMediaUploadService extends IntentService {
    public static final String TAG = "MultiMediaUploadService";

    public MultiMediaUploadService() {
        super("MultiMediaUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Map<String, String> eventData = new HashMap<>();

        try {
            // get caller data
            Bundle iArgs = intent.getExtras();

            eventData.put("name", iArgs.getString("name"));
            eventData.put("callback", iArgs.getString("callback"));

            StorageComponent component = DaggerStorageComponent.create();
            ReqSetBody req = component.provideReqSetBody();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("s3upload");

            Map<String, String> body = new HashMap<>();
            body.put("name", iArgs.getString("name"));
            body.put("content", iArgs.getString("content"));
            body.put("content_type", iArgs.getString("content_type"));

            req.setBody(body);

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(RestClient.INSTANCE.jsonMediaType());

            HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);
            if (resp.getBody() != null) {
                Map<String, Object> respBody = resp.getBody().getBody();
                String imageKey = (String) respBody.get(AppConstants.APP_INTENT.KEY_KEY.getValue());

                eventData.put(AppConstants.APP_INTENT.KEY_KEY.getValue(), imageKey);
            }
        } catch (Exception e) {
            Log.e(TAG, "err uploading...");
        }

        BusProvider.INSTANCE.getBus().post(new Events.MultiMediaUploadEvent(eventData));
    }
}
