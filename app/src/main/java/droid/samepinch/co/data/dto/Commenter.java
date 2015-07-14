package droid.samepinch.co.data.dto;

import java.io.Serializable;

/**
 * Created by imaginationcoder on 7/14/15.
 */
public class Commenter implements Serializable{
    private String fname;
    private String lname;
    private String photo;
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
