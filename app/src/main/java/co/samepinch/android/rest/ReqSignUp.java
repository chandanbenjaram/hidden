package co.samepinch.android.rest;

import java.util.HashMap;
import java.util.Map;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class ReqSignUp extends RestBase<Map<String, String>> {

    //    "phno":"",
//            "fname":"",
//            "lname":"",
//            "email":"",
//            "pinch_handle":"",
//            "password":"",
//            "password_confirmation":"",
//            "key":"",
//            "postal_code":"",
//            "blog":"",
//            "summary":"",
//            "device_token":"",
//            "platform": "{iOS or android}"
//
//
    transient String fname;
    transient String lname;
    transient String email;
    transient String password;
    transient String pinchHandle;

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPinchHandle() {
        return pinchHandle;
    }

    public void setPinchHandle(String pinchHandle) {
        this.pinchHandle = pinchHandle;
    }

    @Override
    public Map<String, String> getBody() {
        return body;
    }

    @Override
    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public ReqSignUp build() {
        Map<String, String> body = new HashMap<>();
        body.put("fname", Utils.emptyIfNull(fname));
        body.put("lname", Utils.emptyIfNull(lname));
        body.put("email", Utils.emptyIfNull(email));
        body.put("password", Utils.emptyIfNull(password));
        body.put("pinch_handle", Utils.emptyIfNull(pinchHandle));

        // const
        body.put(AppConstants.KV.PLATFORM.getValue(), AppConstants.KV.PLATFORM.getValue());
        this.setBody(body);

        return this;
    }
}
