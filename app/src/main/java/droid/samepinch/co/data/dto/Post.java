package droid.samepinch.co.data.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class Post implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @SerializedName("uid")
    String uid;
    String content;
    @SerializedName("comment_count")
    Integer commentCount;
    @SerializedName("upvote_count")
    Integer upvoteCount;
    Integer views;
    List<String> commenters;
    List<String> tags;
    Boolean anonymous;
    @SerializedName("createdAt")
    String createdAtStr;
    @Expose(deserialize = false, serialize = false)
    @SerializedName("createdAtDB")
    Date createdAt;
    User owner;

    public Post() {
    }

    protected Post(Parcel in) {
        uid = in.readString();
        content = in.readString();
        commentCount = in.readByte() == 0x00 ? null : in.readInt();
        upvoteCount = in.readByte() == 0x00 ? null : in.readInt();
        views = in.readByte() == 0x00 ? null : in.readInt();
        if (in.readByte() == 0x01) {
            commenters = new ArrayList<String>();
            in.readList(commenters, String.class.getClassLoader());
        } else {
            commenters = null;
        }
        if (in.readByte() == 0x01) {
            tags = new ArrayList<String>();
            in.readList(tags, String.class.getClassLoader());
        } else {
            tags = null;
        }
        byte anonymousVal = in.readByte();
        anonymous = anonymousVal == 0x02 ? null : anonymousVal != 0x00;
        createdAtStr = in.readString();
        owner = (User) in.readValue(User.class.getClassLoader());
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

    public List<String> getCommenters() {
        return commenters;
    }

    public void setCommenters(List<String> commenters) {
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
        if (createdAt == null && createdAtStr != null) {
            //TODO
            setCreatedAt(new Date());
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
            setCommenters(Arrays.asList(commenters.split(",")));
        } else {
            setCommenters(null);
        }
    }

    public String getCommentersForDB() {
        if (getCommenters() == null || getCommenters().isEmpty()) {
            return null;
        }
        return StringUtils.arrayToDelimitedString(getCommenters().toArray(), ",");
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(content);
        if (commentCount == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(commentCount);
        }
        if (upvoteCount == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(upvoteCount);
        }
        if (views == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(views);
        }
        if (commenters == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(commenters);
        }
        if (tags == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tags);
        }
        if (anonymous == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (anonymous ? 0x01 : 0x00));
        }
        dest.writeString(createdAtStr);
        dest.writeValue(owner);
    }
}