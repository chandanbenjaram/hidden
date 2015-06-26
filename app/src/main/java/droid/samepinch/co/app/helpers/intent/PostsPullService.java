package droid.samepinch.co.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.GsonBuilder;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import droid.samepinch.co.app.helpers.AppConstants;

import static droid.samepinch.co.app.helpers.AppConstants.KV.CLIENT_ID;
import static droid.samepinch.co.app.helpers.AppConstants.KV.CLIENT_SECRET;
import static droid.samepinch.co.app.helpers.AppConstants.KV.SCOPE;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class PostsPullService extends IntentService {
    public static final String LOG_TAG = PostsPullService.class.getSimpleName();

    private BroadcastNotifier mBroadcaster;


    public PostsPullService() {
        super(PostsPullService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mBroadcaster = new BroadcastNotifier(this);

        Log.i(LOG_TAG, "processing...");
        mBroadcaster.broadcastIntentWithState(AppConstants.KV.STATE_ACTION_STARTED.getValue());
        Log.i(LOG_TAG, "broadcasted state action started...");

//        "grant_type":"client_credentials",
        //String postCount = intent.getDataString();
        Map<String, String> payload = new HashMap<>();
        //"client_id":"< app id shared by kidslink >",
        payload.put(CLIENT_ID.getKey(), CLIENT_ID.getValue());
        //"client_secret":"< secret key shared by kidslink >",
        payload.put(CLIENT_SECRET.getKey(), CLIENT_SECRET.getValue());
        //"scope":"ikidslink"
        payload.put(SCOPE.getKey(), SCOPE.getValue());
        String payloadStr = new GsonBuilder().create().toJson(payload, Map.class);
        Log.i(LOG_TAG, "payloadStr=" + payloadStr);

        String url = AppConstants.API.CLIENTAUTH.getValue();

        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new StringHttpMessageConverter());

        ResponseEntity<String> result = rest.postForEntity(url, payloadStr, String.class);
        Log.i(LOG_TAG, "result=" + result.getBody());

        // Reports that the feed retrieval is complete.
        mBroadcaster.broadcastIntentWithState(AppConstants.KV.STATE_ACTION_COMPLETE.getValue());
        Log.i(LOG_TAG, "broadcasted state action complete...");

    }

}
