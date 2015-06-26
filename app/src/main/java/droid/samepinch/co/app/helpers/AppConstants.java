package droid.samepinch.co.app.helpers;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class AppConstants {
    public static enum API {
        BASE("https://posts.samepinch.co/"),
        CLIENTAUTH(BASE.getValue().concat("api/clients/token")),
        USERS(BASE.getValue().concat("api/users")),
        POSTS(BASE.getValue().concat("api/posts")),
        ;

        private final String value;

        API(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static enum KV {
        GRANT_TYPE("grant_type", "client_credentials"),
        CLIENT_ID("client_id", "07b71e492ccb9de623cfa8d151157b5452ad52eae7197fe85689a07876960f8f"),
        CLIENT_SECRET("client_secret", "2e716b657cd8d0a85ea632a915d0a3c699bd7bc2be326ecec167d26bba159a9b"),
        SCOPE("scope", "imsocl"),
        LOGV("", "false"),
        LOGD("", "true"),
        BROADCAST_ACTION("", "droid.samepinch.co.app.helpers.intent.BROADCAST_ACTION"),
        EXTENDED_DATA_STATUS("", "droid.samepinch.co.app.helpers.intent.STATUS"),
        EXTENDED_STATUS_LOG("", "droid.samepinch.co.app.helpers.intent.LOG"),
        STATE_ACTION_STARTED("", String.valueOf(0)),
        STATE_ACTION_COMPLETE("", String.valueOf(4))
        ;


        private final String key, value;

        private KV(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
