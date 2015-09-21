package co.samepinch.android.data.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class User {
    String uid;
    String fname;
    String lname;
    String photo;
    String email;
    String prefName;
    @SerializedName("pinch_handle")
    String pinchHandle;
    @SerializedName("posts_count")
    Long postsCount;
    @SerializedName("followers_count")
    Long followersCount;
    String summary;
    String blog;
    Boolean follow;

    public Long getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(Long postsCount) {
        this.postsCount = postsCount;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public Boolean getFollow() {
        return follow;
    }

    public void setFollow(Boolean follow) {
        this.follow = follow;
    }

    public User() {

    }

    public String getPinchHandle() {
        return pinchHandle;
    }

    public void setPinchHandle(String pinchHandle) {
        this.pinchHandle = pinchHandle;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPrefName() {
        return prefName;
    }

    public void setPrefName(String prefName) {
        this.prefName = prefName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}