package co.samepinch.android.data.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by imaginationcoder on 7/14/15.
 */
public class Commenter implements Serializable {
    String uid;
    private String fname;
    private String lname;
    private String photo;
    @SerializedName("pinch_handle")
    String pinchHandle;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPinchHandle() {
        return pinchHandle;
    }

    public void setPinchHandle(String pinchHandle) {
        this.pinchHandle = pinchHandle;
    }

    private Boolean anonymous;

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

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }
}
