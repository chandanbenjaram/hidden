package droid.samepinch.co.rest;

import java.util.HashMap;
import java.util.Map;

import droid.samepinch.co.app.helpers.Utils;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class ReqPosts extends RestBase<Map<String, String>> {
    transient String postCount;
    transient String lastModified;
    transient String step;
    transient String etag;
    transient String key;
    transient String by;

    public String getPostCount() {
        return postCount;
    }

    public void setPostCount(String postCount) {
        this.postCount = postCount;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    @Override

    public Map<String, String> getBody() {
        return body;
    }

    @Override
    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public ReqPosts build() {
        Map<String, String> body = new HashMap<>();
        body.put("post_count", Utils.emptyIfNull(postCount));
        body.put("last_modified", Utils.emptyIfNull(lastModified));
        body.put("step", Utils.emptyIfNull(step));
        body.put("etag", Utils.emptyIfNull(etag));
        body.put("key", Utils.emptyIfNull(key));
        body.put("by", Utils.emptyIfNull(by));
        this.setBody(body);

        return this;
    }

}
