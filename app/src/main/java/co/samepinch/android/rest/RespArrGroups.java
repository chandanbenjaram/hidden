package co.samepinch.android.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class RespArrGroups extends RestBase<RespArrGroups.Body> {
    public RespArrGroups() {
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
        public Body() {
        }

        @SerializedName("groups")
        private List<BodyItem> groups;

        public List<BodyItem> getGroups() {
            return groups;
        }
        public void setGroups(List<BodyItem> groups) {
            this.groups = groups;
        }
    }

    public static class BodyItem {
        @SerializedName("uid")
        @Expose
        String uid;

        @SerializedName("name")
        @Expose
        String name;

        @SerializedName("image")
        @Expose
        String image;

        public BodyItem() {
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
