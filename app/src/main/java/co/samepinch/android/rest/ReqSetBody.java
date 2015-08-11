package co.samepinch.android.rest;

import java.util.Map;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class ReqSetBody extends RestBase<Map<String, String>> {
    @Override
    public Map<String, String> getBody() {
        return body;
    }

    @Override
    public void setBody(Map<String, String> body) {
        this.body = body;
    }
}
