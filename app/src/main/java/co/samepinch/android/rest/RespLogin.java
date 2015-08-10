package co.samepinch.android.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class RespLogin extends RestBase<RespLogin.Body> {
    public RespLogin() {
//                "fname": "",
//                "lname":"",
//                "email":"",
//                "photo":"",
//                "pinch_handle":"",
//                "summary":"",
//                "blog":"",
//                "uid":""
    }

    @Override
    public Body getBody() {
        return super.getBody();
    }

    @Override
    public void setBody(Body body) {
        super.setBody(body);
    }

    public static class Body {
        @SerializedName("access_token")
        @Expose
        String accessToken;

        @Expose
        String fname;

        @Expose
        String lname;

        @Expose
        String email;

        @Expose
        String photo;

        @SerializedName("pinch_handle")
        @Expose
        String pinchHandle;

        @Expose
        String summary;

        @Expose
        String blog;

        @Expose
        String uid;

        public Body() {

        }
    }
}
