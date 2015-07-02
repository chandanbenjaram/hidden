package droid.samepinch.co.data.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class User implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    String uid;
    String fname;
    String lname;
    String photo;
    String prefName;
    String pinchHandle;

    public User() {
    }

    protected User(Parcel in) {
        uid = in.readString();
        fname = in.readString();
        lname = in.readString();
        photo = in.readString();
        prefName = in.readString();
        pinchHandle = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(fname);
        dest.writeString(lname);
        dest.writeString(photo);
        dest.writeString(prefName);
        dest.writeString(pinchHandle);
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
}