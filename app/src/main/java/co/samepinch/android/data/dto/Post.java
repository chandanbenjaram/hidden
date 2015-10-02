package co.samepinch.android.data.dto;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import co.samepinch.android.app.helpers.Utils;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class Post {
    @SerializedName("uid")
    String uid;
    @SerializedName("comment_count")
    Integer commentCount;
    @SerializedName("upvote_count")
    Integer upvoteCount;
    Integer views;
    List<Commenter> commenters;
    List<String> tags;
    Boolean anonymous;
    @SerializedName("createdAt")
    String createdAtStr;
    @Expose(deserialize = false, serialize = false)
    @SerializedName("createdAtDB")
    Date createdAt;
    User owner;

    String wallContent;
    List<String> wallImages;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(Integer upvoteCount) {
        this.upvoteCount = upvoteCount;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public List<Commenter> getCommenters() {
        return commenters;
    }

    public void setCommenters(List<Commenter> commenters) {
        this.commenters = commenters;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getCreatedAtStr() {
        return createdAtStr;
    }

    public void setCreatedAtStr(String createdAtStr) {
        this.createdAtStr = createdAtStr;
    }

    public Date getCreatedAt() {
        if (createdAt == null) {
            //TODO
            setCreatedAt(Utils.string2Date(createdAtStr));
        }
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setCommentersFromDB(String commenters) {
        if (commenters != null) {
            Gson g = new Gson();
            Type token = new TypeToken<List<Commenter>>() {
            }.getType();
            List<Commenter> commentersList = g.fromJson(commenters, token);
            setCommenters(commentersList);
        } else {
            setCommenters(null);
        }
    }

    public String getCommentersForDB() {
        if (getCommenters() == null || getCommenters().isEmpty()) {
            return null;
        }
        JsonParser parser = new JsonParser();
        // as json string
        Gson g = new Gson();
        String commentsJsonStr = g.toJson(getCommenters());
        return commentsJsonStr;
    }

    public void setTagsFromDB(String tags) {
        if (tags != null) {
            setTags(Arrays.asList(tags.split(",")));
        } else {
            setTags(null);
        }
    }

    public String getTagsForDB() {
        if (getTags() == null || getTags().isEmpty()) {
            return null;
        }
        return StringUtils.arrayToDelimitedString(getTags().toArray(), ",");
    }

    public String getWallContent() {
        return wallContent;
    }

    public void setWallContent(String wallContent) {
        this.wallContent = wallContent;
    }

    public List<String> getWallImages() {
        return wallImages;
    }

    public String getWallImagesForDB() {
        if (getWallImages() == null || getWallImages().isEmpty()) {
            return null;
        }
        return StringUtils.arrayToDelimitedString(getWallImages().toArray(), ",");
    }


    public void setWallImages(List<String> wallImages) {
        this.wallImages = wallImages;
    }

    public void setWallImagesFromDB(String images) {
        if (images != null) {
            setWallImages(Arrays.asList(images.split(",")));
        } else {
            setWallImages(null);
        }
    }
}