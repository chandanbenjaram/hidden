package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

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
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class SignOutService extends IntentService {
    public static final String LOG_TAG = "SignOutService";

    public SignOutService() {
        super("SignOutService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // google
        GoogleApiClient mGoogleApiClient =
                new GoogleApiClient.Builder(this)
                        .addApi(Plus.API, Plus.PlusOptions.builder().build())
                        .addScope(Plus.SCOPE_PLUS_LOGIN)
                        .addScope(Plus.SCOPE_PLUS_PROFILE)
                        .build();

        mGoogleApiClient.blockingConnect();
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }

        // fb
        LoginManager.getInstance().logOut();

        //headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        try {
            ReqNoBody logoutReq = new ReqNoBody();
            // set base args
            logoutReq.setToken(Utils.getAppToken(false));
            logoutReq.setCmd("signOut");

            HttpEntity<ReqNoBody> payloadEntity;
            ResponseEntity<Resp> resp = null;
            // call remote
            payloadEntity = new HttpEntity<>(logoutReq, headers);
            resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);

        } catch (Exception e) {
            // get rid of auth session
            Log.e(LOG_TAG, e.getMessage());
        }

        Utils.PreferencesManager.getInstance().remove(AppConstants.API.ACCESS_TOKEN.getValue());
        Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_PROVIDER.getValue());
        Utils.PreferencesManager.getInstance().remove(AppConstants.API.PREF_AUTH_USER.getValue());

        BusProvider.INSTANCE.getBus().post(new Events.AuthOutEvent(null));

    }
}
