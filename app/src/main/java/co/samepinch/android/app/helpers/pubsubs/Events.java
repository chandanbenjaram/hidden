package co.samepinch.android.app.helpers.pubsubs;

import java.util.Map;

/**
 * Created by cbenjaram on 7/15/15.
 */
public class Events {
    public static class EventBase {
        private final Map<String, String> metaData;

        public EventBase(Map<String, String> metaData) {
            this.metaData = metaData;
        }

        public Map<String, String> getMetaData() {
            return metaData;
        }
    }

    public static class PostsRefreshedEvent extends EventBase {
        public PostsRefreshedEvent(Map<String, String> metaData) {
            super(metaData);
        }
    }

    public static class TagRefreshedEvent extends EventBase {
        public TagRefreshedEvent(Map<String, String> metaData) {
            super(metaData);
        }
    }

    public static class PostDetailsRefreshEvent extends EventBase {
        public PostDetailsRefreshEvent(Map<String, String> metaData) {
            super(metaData);
        }
    }

    public static class AuthSuccessEvent extends EventBase {
        public AuthSuccessEvent(Map<String, String> metaData) {
            super(metaData);
        }
    }

    public static class AuthFailEvent extends EventBase {
        public AuthFailEvent(Map<String, String> metaData) {
            super(metaData);
        }
    }
}
