package droid.samepinch.co.rest;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class ReqGroups extends RestBase<Map<String, String>> {
    @Expose(serialize = false, deserialize = false)
    private String name;


    @Override
    public Map<String, String> getBody() {
        return body;
    }

    @Override
    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(getBody() == null){
            setBody(new HashMap<String, String>());
        }
        getBody().put("name", name);
        this.name = name;
    }
}
