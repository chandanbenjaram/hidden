package droid.samepinch.co.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import droid.samepinch.co.app.helpers.Utils;

/**
 * Created by imaginationcoder on 7/21/15.
 */
public class CommentDetails {
    String uid;
    private String text;
    @SerializedName("createdAt")
    String createdAtStr;
    Boolean anonymous;
    @SerializedName("upvote_count")
    Integer upvoteCount;
    Commenter commenter;

    @SerializedName("createdAtDB")
    Date createdAt;

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Commenter getCommenter() {
        return commenter;
    }

    public void setCommenter(Commenter commenter) {
        this.commenter = commenter;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAtStr() {
        return createdAtStr;
    }

    public Date getCreatedAt() {
        if (createdAt == null) {
            //TODO
            setCreatedAt(Utils.string2Date(createdAtStr));
        }
        return createdAt;
    }

    public void setCreatedAtStr(String createdAtStr) {
        this.createdAtStr = createdAtStr;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Integer getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(Integer upvoteCount) {
        this.upvoteCount = upvoteCount;
    }
}
