package droid.samepinch.co.app.helpers;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class AppConstants {

    public enum API {
        SHARED_PREFS_NAME("AppPref"),
        ACCESS_TOKEN("access_token"),

        BASE("https://msocl.herokuapp.com/"),
        CLIENTAUTH(BASE.getValue().concat("api/clients/token")),
        USERS(BASE.getValue().concat("api/users")),
        POSTS(BASE.getValue().concat("api/posts")),
        CONTENT_AUTHORITY("droid.samepinch.co.app");

        private final String value;

        API(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum APP_INTENT {
        BROADCAST_ACTION("droid.samepinch.co.app.helpers.intent.BROADCAST_ACTION"),
        EXTENDED_DATA_STATUS("droid.samepinch.co.app.helpers.intent.STATUS"),
        EXTENDED_STATUS_LOG("droid.samepinch.co.app.helpers.intent.LOG"),
        REFRESH_ACTION_STARTED("starting to refresh"),
        REFRESH_ACTION_FAILED("failed to refresh"),
        REFRESH_ACTION_COMPLETE("refresh complete"),
        AUTHENTICATING_CLIENT("authenticating client..."),
        REFRESH_ACTION_LOAD("load refreshed content");

        private final String value;

        APP_INTENT(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    public enum KV {
        GRANT_TYPE("grant_type", "client_credentials"),
        CLIENT_ID("client_id", "3e0786a2f258e6f9b08250dbd7f35010480988e0d3d1ef373b79e07884be79f9"),
        CLIENT_SECRET("client_secret", "813c95cc2eb6c0cf4f49d30d0add0c6fc3ea82863d30507beb6733c0e643927c"),
        SCOPE("scope", "imsocl"),
        LOGV("", "false"),
        LOGD("", "true");

        private final String key, value;

        KV(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }


        public Integer getIntValue() {
            return Integer.parseInt(getValue());
        }
    }
}
