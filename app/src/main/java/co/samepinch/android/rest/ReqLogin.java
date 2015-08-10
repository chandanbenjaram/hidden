package co.samepinch.android.rest;

import java.util.HashMap;
import java.util.Map;

import co.samepinch.android.app.helpers.Utils;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class ReqLogin extends RestBase<Map<String, String>> {
    transient String email;
    transient String password;
    transient String deviceToken;
    transient String platform;

    @Override

    public Map<String, String> getBody() {
        return body;
    }

    @Override
    public void setBody(Map<String, String> body) {
        this.body = body;
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

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public ReqLogin build() {
        Map<String, String> body = new HashMap<>();
        body.put("email", Utils.emptyIfNull(email));
        body.put("password", Utils.emptyIfNull(password));
        body.put("platform", Utils.emptyIfNull(platform));
        body.put("deviceToken", Utils.emptyIfNull(deviceToken));

        this.setBody(body);

        return this;
    }
}
