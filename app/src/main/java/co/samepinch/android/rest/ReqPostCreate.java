package co.samepinch.android.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public class ReqPostCreate extends RestBase<ReqPostCreate.Body> {
    @Override
    public Body getBody() {
        return super.getBody();
    }

    @Override
    public void setBody(Body body) {
        super.setBody(body);
    }

    public static class Body {
        public Body() {
        }

        @SerializedName("content")
        private String content;

        @SerializedName("anonymous")
        private String anonymous;

        @SerializedName("tags_array")
        private List<String> tags;

        @SerializedName("img_keys")
        private List<String> imgKeys;


        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAnonymous() {
            return anonymous;
        }

        public void setAnonymous(String anonymous) {
            this.anonymous = anonymous;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getImgKeys() {
            return imgKeys;
        }

        public void setImgKeys(List<String> imgKeys) {
            this.imgKeys = imgKeys;
        }
    }
}
