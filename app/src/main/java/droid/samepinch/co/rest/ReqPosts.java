package droid.samepinch.co.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class ReqPosts extends RestBase<Map<String, String>> {
    transient Integer postCount;
    transient Date lastModified;
    transient Integer step;
    transient String etag;

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    @Override
    public Map<String, String> getBody() {
        Map<String, String> body = new HashMap<>();
        body.put("post_count", Integer.toString(postCount == null ? 25 : postCount));
        //body.put("lastModified", System);
        body.put("lastModified", "");
        body.put("step", "");
        body.put("etag", "");

        return body;
    }

}
