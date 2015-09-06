package co.samepinch.android.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class RespTags extends RestBase<RespTags.Body> {

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

        @SerializedName("favourites")
        private List<RespArrGroups.BodyItem> favourites;

        @SerializedName("recommended")
        private List<RespArrGroups.BodyItem> recommended;

        public List<RespArrGroups.BodyItem> getFavourites() {
            return favourites;
        }

        public void setFavourites(List<RespArrGroups.BodyItem> favourites) {
            this.favourites = favourites;
        }

        public List<RespArrGroups.BodyItem> getRecommended() {
            return recommended;
        }

        public void setRecommended(List<RespArrGroups.BodyItem> recommended) {
            this.recommended = recommended;
        }
    }
}