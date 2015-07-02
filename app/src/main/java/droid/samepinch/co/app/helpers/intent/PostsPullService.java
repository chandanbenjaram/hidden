package droid.samepinch.co.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import droid.samepinch.co.app.helpers.AppConstants;
import droid.samepinch.co.data.DB;
import droid.samepinch.co.data.dto.Post;
import droid.samepinch.co.rest.ReqPosts;
import droid.samepinch.co.rest.RespPosts;

import static droid.samepinch.co.app.helpers.AppConstants.KV.CLIENT_ID;
import static droid.samepinch.co.app.helpers.AppConstants.KV.CLIENT_SECRET;
import static droid.samepinch.co.app.helpers.AppConstants.KV.GRANT_TYPE;
import static droid.samepinch.co.app.helpers.AppConstants.KV.SCOPE;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class PostsPullService extends IntentService {
    public static final String LOG_TAG = PostsPullService.class.getSimpleName();
    private static RestTemplate rest = new RestTemplate();

    private BroadcastNotifier mBroadcaster;


    public PostsPullService() {
        super(PostsPullService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mBroadcaster = new BroadcastNotifier(this);
        mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.REFRESH_ACTION_STARTED);

        String appToken = getAppToken();
        if (appToken == null) {
            mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.REFRESH_ACTION_FAILED);
            return;
        }

        ReqPosts postsReq = new ReqPosts();
        postsReq.setToken(appToken);
        postsReq.setCmd("all");
        postsReq.setPostCount(10);
        postsReq.setLastModified(null);
        postsReq.setStep(1);
        postsReq.setEtag("");

        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeAdapter()).create();

            String reqData = gson.toJson(postsReq.build());


            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            // call
//            rest.setMessageConverters();
            HttpEntity<ReqPosts> payloadEntity = new HttpEntity<>(postsReq.build(), headers);
            ResponseEntity<RespPosts> resp = rest.exchange(AppConstants.API.POSTS.getValue(), HttpMethod.POST, payloadEntity, RespPosts.class);

//            String respStr = response.getBody();
//            System.out.println("responseStr...\n" + respStr);
//
            RespPosts respData = resp.getBody();
            List<Post> postsToInsert = respData.getBody().getPosts();
            DB.mPostDAO.addPosts(postsToInsert);

            System.out.println(respData);

//            Map<String, String> responseEntity = response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
        }


        mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.REFRESH_ACTION_COMPLETE);
    }

    public String getAppToken() {
        String accessToken = AppConstants.API.ACCESS_TOKEN.getValue();
        SharedPreferences settings = getSharedPreferences(AppConstants.API.SHARED_PREFS_NAME.getValue(), 0);
        String token = settings.getString(accessToken, null);
        if (token != null) {
            return token;
        }
        mBroadcaster.broadcastIntentWithState(AppConstants.APP_INTENT.AUTHENTICATING_CLIENT);

        try {
            //time to fetch a token
            final Map<String, String> payload = new HashMap<>();
            payload.put(CLIENT_ID.getKey(), CLIENT_ID.getValue());
            payload.put(CLIENT_SECRET.getKey(), CLIENT_SECRET.getValue());
            payload.put(SCOPE.getKey(), SCOPE.getValue());
            payload.put(GRANT_TYPE.getKey(), GRANT_TYPE.getValue());

            ResponseEntity<Map> response = rest.postForEntity(AppConstants.API.CLIENTAUTH.getValue(), payload, Map.class);
            Map<String, String> responseEntity = response.getBody();
            // populate to shared prefs
            SharedPreferences.Editor editor = settings.edit();
            for (Map.Entry<String, String> entry : responseEntity.entrySet()) {
                editor.putString(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            editor.commit();

            return settings.getString(accessToken, null);
        } catch (RuntimeException e) {
            // muted
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

}
