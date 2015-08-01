package droid.samepinch.co.app.helpers;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class AppConstants {

    public enum API {
        SHARED_PREFS_NAME("droid.samepinch.co.app.pref"),
        ACCESS_TOKEN("access_token"),
        PREF_POSTS_LIST("posts.lists"),
        DEFAULT_DATE_FORMAT("MM/dd/yyyy hh:mma"),


        BASE("https://msocl.herokuapp.com/"),
        CLIENTAUTH(BASE.getValue().concat("api/clients/token")),
        USERS(BASE.getValue().concat("api/users")),
        POSTS_WITH_FILTER(BASE.getValue().concat("api/v2/posts")),
        POSTS(BASE.getValue().concat("api/posts")),
        GROUPS(BASE.getValue().concat("api/groups")),
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
        REFRESH_ACTION_LOAD("load refreshed content"),

        KEY_POST_COUNT("post_count"),
        KEY_LAST_MODIFIED("last_modified"),
        KEY_STEP("step"),
        KEY_ETAG("etag"),
        KEY_KEY("key"),
        KEY_BY("by"),
        KEY_NEW("new"),
        KEY_NAME("name"),
        KEY_UID("uid"),



        ;
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
        ;

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

    public enum K {
        APP_EXTRAS,
        TARGET_FRAGMENT,
        FRAGMENT_TAGWALL,
        FRAGMENT_DOTWALL,
        FRAGMENT_COMMENT,
        KEY_TAG,
        KEY_DOT,
        Wall,
        POST
    }
}
