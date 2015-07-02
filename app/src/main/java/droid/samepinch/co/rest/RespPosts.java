package droid.samepinch.co.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import droid.samepinch.co.data.dto.Post;

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
        String lastModifiedStr;

        @Expose(deserialize = false, serialize = false)
        Date lastModified;

        @Expose
        String etag;

        @Expose
        @SerializedName("anonymous_image")
        String anonymousImage;

        List<Post> posts;

        public Body() {

        }


        public Integer getPostCount() {
            return postCount;
        }

        public void setPostCount(Integer postCount) {
            this.postCount = postCount;
        }

        public String getLastModifiedStr() {
            return lastModifiedStr;
        }

        public void setLastModifiedStr(String lastModifiedStr) {
            this.lastModifiedStr = lastModifiedStr;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public void setLastModified(Date lastModified) {
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

        public List<Post> getPosts() {
            return posts;
        }

        public void setPosts(List<Post> posts) {
            this.posts = posts;
        }



    }
}
