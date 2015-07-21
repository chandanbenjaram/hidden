package droid.samepinch.co.data.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import droid.samepinch.co.app.helpers.Utils;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class PostDetails {
    @SerializedName("uid")
    String uid;
    String content;
    Map<String, String> images;
    @SerializedName("large_images")
    Map<String, String> largeImages;
    @SerializedName("comment_count")
    Integer commentCount;
    @SerializedName("upvote_count")
    Integer upvoteCount;
    Integer views;
    List<String> tags;

    public List<CommentDetails> getComments() {
        return comments;
    }

    public void setComments(List<CommentDetails> comments) {
        this.comments = comments;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public void setImages(Map<String, String> images) {
        this.images = images;
    }

    public Map<String, String> getLargeImages() {
        return largeImages;
    }

    public void setLargeImages(Map<String, String> largeImages) {
        this.largeImages = largeImages;
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

    public String getAnonymousImage() {
        return anonymousImage;
    }

    public void setAnonymousImage(String anonymousImage) {
        this.anonymousImage = anonymousImage;
    }

    List<CommentDetails> comments;

    Boolean anonymous;
    @SerializedName("createdAt")
    String createdAtStr;
    @Expose(deserialize = false, serialize = false)
    @SerializedName("createdAtDB")
    Date createdAt;
    User owner;

    @SerializedName("anonymous_image")
    String anonymousImage;


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
}