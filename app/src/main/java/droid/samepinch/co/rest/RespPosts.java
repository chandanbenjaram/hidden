package droid.samepinch.co.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class RespPosts extends RestBase<RespPosts.Body> {

    public RespPosts() {
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
        @SerializedName("post_count")
        @Expose
        Integer postCount;

        @SerializedName("last_modified")
        @Expose(deserialize = false)
        String lastModified;

        @Expose
        String etag;

        @Expose
        @SerializedName("anonymous_image")
        String anonymousImage;

        public Body() {

        }

        public Integer getPostCount() {
            return postCount;
        }

        public void setPostCount(Integer postCount) {
            this.postCount = postCount;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public String getAnonymousImage() {
            return anonymousImage;
        }

        public void setAnonymousImage(String anonymousImage) {
            this.anonymousImage = anonymousImage;
        }
    }
}
