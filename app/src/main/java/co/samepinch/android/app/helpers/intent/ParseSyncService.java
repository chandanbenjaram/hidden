package co.samepinch.android.app.helpers.intent;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseInstallation;
import com.parse.ParsePush;

import java.util.List;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_PARSE_ACCESS_STATE;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class ParseSyncService extends IntentService {
    public static final String LOG_TAG = "ParseSyncService";
    public static final String CHANNELS = "channels";

    public ParseSyncService() {
        super("ParseSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // grab current parse state
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        // get caller data
        Bundle iArgs = intent.getExtras();
        int accessState = iArgs.getInt(KEY_PARSE_ACCESS_STATE.getValue(), -1);

        // upsert access state
        installation.put(KEY_PARSE_ACCESS_STATE.getValue(), accessState);

        // log-in state
        if (accessState == 1) {
            Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
            String currUserId = userInfo.get(AppConstants.APP_INTENT.KEY_UID.getValue());
            ParsePush.subscribeInBackground(currUserId);
        } else {
            // non-login state - get rid of all channels
            List<String> subscribedChannels = installation.getList(CHANNELS);
            if (subscribedChannels != null) {
                for (String channel : subscribedChannels) {
                    ParsePush.unsubscribeInBackground(channel);
                }
            }
        }
    }
}
