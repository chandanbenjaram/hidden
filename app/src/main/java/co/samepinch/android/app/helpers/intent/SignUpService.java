package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.ReqSignUp;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RespLogin;
import co.samepinch.android.rest.RestClient;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_EMAIL;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_LNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PASSWORD;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PINCH_HANDLE;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class SignUpService extends IntentService {
    public static final String LOG_TAG = "SignUpService";

    public SignUpService() {
        super("SignUpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get rid of any logins
        Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_PROVIDER.getValue());
        Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_USER.getValue());

        Map<String, String> eData = new HashMap<>();
        eData.put(AppConstants.API.PREF_AUTH_PROVIDER.getValue(), AppConstants.K.via_email_password.name());

        //headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        try {
            ReqSignUp req = new ReqSignUp();
            // set base args
            req.setToken(Utils.getAppToken(true));
            req.setCmd("create");

            // get caller data
            Bundle iArgs = intent.getExtras();
            req.setFname(iArgs.getString(KEY_FNAME.getValue()));
            req.setLname(iArgs.getString(KEY_LNAME.getValue()));
            req.setEmail(iArgs.getString(KEY_EMAIL.getValue()));
            req.setPassword(iArgs.getString(KEY_PASSWORD.getValue()));
            req.setPinchHandle(iArgs.getString(KEY_PINCH_HANDLE.getValue()));


            HttpEntity<ReqSignUp> payloadEntity;
            ResponseEntity<RespLogin> resp = null;
            // call remote
            payloadEntity = new HttpEntity<>(req.build(), headers);
            resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS.getValue(), HttpMethod.POST, payloadEntity, RespLogin.class);

            Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_AUTH_PROVIDER.getValue(), AppConstants.K.via_email_password.name());
            Utils.PreferencesManager.getInstance().setValue(AppConstants.API.PREF_AUTH_USER.getValue(), resp.getBody().getBody());

            BusProvider.INSTANCE.getBus().post(new Events.SignUpSuccessEvent(eData));
        } catch (Exception e) {
            Resp resp = Utils.parseAsRespSilently(e);
            if(resp !=null){
                eData.put(AppConstants.K.MESSAGE.name(), resp.getMessage());
            }
            // get rid of auth session
            Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_PROVIDER.getValue());
            Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_USER.getValue());

            BusProvider.INSTANCE.getBus().post(new Events.SignUpFailEvent(eData));
        }
//
//        Utils.PreferencesManager.getInstance().remove(AppConstants.API.ACCESS_TOKEN.getValue());
//        Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_PROVIDER.getValue());
//        Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_USER.getValue());
//
//        BusProvider.INSTANCE.getBus().post(new Events.AuthOutEvent(null));

    }
}
