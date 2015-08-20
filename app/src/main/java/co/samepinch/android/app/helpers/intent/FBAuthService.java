package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

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
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespLogin;
import co.samepinch.android.rest.RestClient;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class FBAuthService extends IntentService {
    public static final String LOG_TAG = "FBAuthService";

    public FBAuthService() {
        super("FBAuthService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Map<String, String> eventData = new HashMap<>();

        StorageComponent component = DaggerStorageComponent.create();
        ReqSetBody loginReq = component.provideReqSetBody();

        //headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        try {
            eventData.put(AppConstants.K.provider.name(), intent.getStringExtra("provider"));

            Map<String, String> reqBody = (Map<String, String>) intent.getSerializableExtra("user");
            loginReq.setBody(reqBody);

            // set base args
            loginReq.setToken(Utils.getNonBlankAppToken());
            loginReq.setCmd("externalSignIn");

            HttpEntity<ReqSetBody> payloadEntity;
            ResponseEntity<RespLogin> resp = null;
            // call remote
            payloadEntity = new HttpEntity<>(loginReq, headers);
            resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS_EXT.getValue(), HttpMethod.POST, payloadEntity, RespLogin.class);

            Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_AUTH_PROVIDER.getValue(), intent.getStringExtra("provider"));
            Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_AUTH_USER.getValue(), resp.getBody().getBody());

            BusProvider.INSTANCE.getBus().post(new Events.AuthSuccessEvent(eventData));
        } catch (Exception e) {
            Resp resp = Utils.parseAsRespSilently(e);
            if (resp != null) {
                eventData.put(AppConstants.K.MESSAGE.name(), resp.getMessage());
            }

//            Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_PROVIDER.getValue());
//            Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_USER.getValue());
            BusProvider.INSTANCE.getBus().post(new Events.AuthFailEvent(eventData));
        }
    }

    public static Map<String, String> toMap(JSONObject arg0) throws JSONException {
        Map<String, String> body = new HashMap<>();
        body.put(AppConstants.K.provider.name(), AppConstants.K.facebook.name());
        body.put("oauth_uid", Utils.emptyIfNull(arg0.getString("id")));
        body.put("fname", Utils.emptyIfNull(arg0.getString("first_name")));
        body.put("lname", Utils.emptyIfNull(arg0.getString("last_name")));
        body.put("email", Utils.emptyIfNull(arg0.getString("email")));
        if (arg0.has("pinch_handle")) {
            body.put("pinch_handle", arg0.getString("pinch_handle"));
        }
        body.put("rphoto", "http://harrogatearchsoc.org/wp-content/uploads/2013/12/Active-Imagination-.jpg");
        return body;
    }
}
