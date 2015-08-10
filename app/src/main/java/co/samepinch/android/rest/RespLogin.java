package co.samepinch.android.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class RespLogin extends RestBase<Map<String, String>> {
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
    public Map<String, String> getBody() {
        return super.getBody();
    }

    @Override
    public void setBody(Map<String, String> body) {
        super.setBody(body);
    }

//    public static class Body {
//        @SerializedName("access_token")
//        @Expose
//        String accessToken;
//
//        @Expose
//        String fname;
//
//        @Expose
//        String lname;
//
//        @Expose
//        String email;
//
//        @Expose
//        String photo;
//
//        @SerializedName("pinch_handle")
//        @Expose
//        String pinchHandle;
//
//        @Expose
//        String summary;
//
//        @Expose
//        String blog;
//
//        @Expose
//        String uid;
//
//        public Body() {
//
//        }
//    }
}
