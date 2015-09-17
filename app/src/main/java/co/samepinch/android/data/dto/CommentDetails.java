package co.samepinch.android.data.dto;

import com.google.gson.annotations.SerializedName;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import co.samepinch.android.app.helpers.Utils;

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
    Boolean upvoted;
    @SerializedName("can")
    List<String> permissions;
    @SerializedName("createdAtDB")
    Date createdAt;

    Commenter commenter;

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getPermissionsForDB() {
        if (getPermissions() == null) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        return StringUtils.arrayToDelimitedString(getPermissions().toArray(), ",");
    }

    public void setPermissionsFromDB(String permissions) {
        if (permissions == null) {
            setPermissions(null);
        } else {
            setPermissions(Arrays.asList(permissions.split(",")));
        }
    }

    public Boolean getUpvoted() {
        return upvoted;
    }

    public void setUpvoted(Boolean upvoted) {
        this.upvoted = upvoted;
    }

    public Commenter getCommenter() {
        return commenter;
    }

    public void setCommenter(Commenter commenter) {
        this.commenter = commenter;
    }
}
