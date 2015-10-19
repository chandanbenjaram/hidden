package co.samepinch.android.data.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Created by imaginationcoder on 10/17/15.
 */
public class PushNotification {
    String uid;

    @SerializedName("alert")
    String message;

    @SerializedName("img")
    String alertImage;

    Context context;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAlertImage() {
        return alertImage;
    }

    public void setAlertImage(String alertImage) {
        this.alertImage = alertImage;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static class Context {
        String type;
        String uid;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }

}
